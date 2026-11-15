#!/usr/bin/env python3
"""
TMDB poster senkronizasyonu (derleme zamanı aracı).

Katalogdaki (MovieRepository.kt) her film için TMDB arama API'sinden doğru filmi bulur ve
`posterPath` ile `tmdbId` alanlarını günceller. Uygulama (APK) TMDB anahtarı İÇERMEZ;
telefonda yalnızca image.tmdb.org üzerinden görseller yüklenir.

Kullanım:
  python3 tools/tmdb_poster_sync.py report   # eşleşmeleri bul, raporu yaz (dosyalara dokunmaz)
  python3 tools/tmdb_poster_sync.py apply    # rapordaki eşleşmeleri Kotlin kataloğuna yaz

Kimlik doğrulama: TMDB "API Read Access Token" değeri `Authorization: Bearer` başlığıyla gönderilir.
- Bulut ortamında bu başlığı ağ geçidi otomatik ekler (TMDB_TOKEN gerekmez).
- Yerel makinede: TMDB_TOKEN=<token> python3 tools/tmdb_poster_sync.py report

Bu ürün TMDB API'sini kullanır ancak TMDB tarafından onaylanmamış veya sertifikalandırılmamıştır.
"""
import json
import os
import re
import sys
import time
import unicodedata
import urllib.parse
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CATALOG = os.path.join(ROOT, "app/src/main/java/com/example/data/repository/MovieRepository.kt")
TRENDS = os.path.join(ROOT, "app/src/main/java/com/example/data/repository/TrendingCinemaRepository.kt")
REPORT = os.path.join(ROOT, "tools/tmdb_poster_report.json")
API = "https://api.themoviedb.org/3"


def norm(text: str) -> str:
    text = unicodedata.normalize("NFKD", text.lower())
    text = "".join(c for c in text if not unicodedata.combining(c))
    return re.sub(r"[^a-z0-9]+", "", text)


def api_get(path: str, **params) -> dict:
    url = f"{API}{path}?{urllib.parse.urlencode(params)}"
    headers = {"Accept": "application/json"}
    token = os.environ.get("TMDB_TOKEN")
    if token:
        headers["Authorization"] = f"Bearer {token}"
    for attempt in range(4):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=20) as r:
                return json.load(r)
        except urllib.error.HTTPError as e:
            if e.code == 429 and attempt < 3:
                time.sleep(2 * (attempt + 1))
                continue
            raise


def parse_catalog() -> list:
    src = open(CATALOG, encoding="utf-8").read()
    movies = []
    for m in re.finditer(r"\n(\s*)Movie\(\n(.*?)\n\1\)", src, re.S):
        body = m.group(2)
        def field(name):
            f = re.search(rf'\b{name}\s*=\s*"([^"]*)"', body)
            return f.group(1) if f else ""
        year = re.search(r"\byear\s*=\s*(\d{4})", body)
        movies.append({
            "id": field("id"),
            "title": field("title"),
            "originalTitle": field("originalTitle"),
            "year": int(year.group(1)) if year else None,
            "oldPosterPath": field("posterPath"),
        })
    return [m for m in movies if m["id"]]


def score(candidate: dict, queries: list, year: int) -> tuple:
    date = candidate.get("release_date") or ""
    cy = int(date[:4]) if date[:4].isdigit() else None
    names = {norm(candidate.get("title", "")), norm(candidate.get("original_title", ""))}
    title_hit = any(norm(q) in names for q in queries if q)
    year_gap = abs(cy - year) if (cy and year) else 99
    votes = candidate.get("vote_count", 0)
    # Aynı adlı yapımlar arasında: önce ad eşleşmesi, sonra ±1 yıl, sonra hiç oy almamış (henüz
    # yayınlanmamış/belirsiz) kayıtları geri plana at, en son yıl yakınlığı ve popülerlik.
    return (title_hit, year_gap <= 1, votes > 0, -min(year_gap, 5), votes), cy, title_hit, year_gap


def find(movie: dict) -> dict:
    queries = [q for q in (movie["originalTitle"], movie["title"]) if q]
    seen = {}
    for q in dict.fromkeys(queries):
        for extra in ({"primary_release_year": movie["year"]}, {}):
            data = api_get("/search/movie", query=q, language="tr-TR", include_adult="false", **extra)
            for c in data.get("results", [])[:10]:
                seen.setdefault(c["id"], c)
            if seen:
                break
    best, best_key = None, None
    for c in seen.values():
        if not c.get("poster_path"):
            continue
        key, cy, title_hit, gap = score(c, queries, movie["year"])
        if best_key is None or key > best_key:
            best, best_key = (c, cy, title_hit, gap), key
    if not best:
        return {"status": "bulunamadı"}
    c, cy, title_hit, gap = best
    if title_hit and gap == 0:
        status = "kesin"
    elif title_hit and gap <= 1:
        status = "yıl ±1"
    else:
        status = "KONTROL ET"
    return {
        "status": status,
        "tmdbId": c["id"],
        "tmdbTitle": c.get("title"),
        "tmdbOriginalTitle": c.get("original_title"),
        "tmdbYear": cy,
        "posterPath": c["poster_path"],
    }


def report():
    rows = []
    for movie in parse_catalog():
        result = find(movie)
        rows.append({**movie, **result})
        print(f'{result["status"]:<11} {movie["id"]:<5} {movie["title"][:34]:<34} {movie["year"]} -> '
              f'{result.get("tmdbOriginalTitle") or "-"} ({result.get("tmdbYear") or "-"}) #{result.get("tmdbId", "-")}')
        time.sleep(0.15)
    json.dump(rows, open(REPORT, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    print(f"\nRapor: {os.path.relpath(REPORT, ROOT)}")


def apply():
    rows = {r["id"]: r for r in json.load(open(REPORT, encoding="utf-8"))
            if r.get("posterPath") and r.get("status") != "ATLA"}
    src = open(CATALOG, encoding="utf-8").read()

    def patch(m):
        indent, body = m.group(1), m.group(2)
        mid = re.search(r'\bid\s*=\s*"([^"]+)"', body)
        row = rows.get(mid.group(1)) if mid else None
        if not row:
            return m.group(0)
        field_indent = re.match(r"(\s*)", body).group(1)
        body = re.sub(rf'\n{field_indent}tmdbId\s*=\s*\d+,', "", body)
        if re.search(r'\bposterPath\s*=\s*"[^"]*"', body):
            body = re.sub(r'\bposterPath\s*=\s*"[^"]*"', f'posterPath = "{row["posterPath"]}"', body)
        else:
            body = re.sub(r'(\n(\s*)whyWatch\s*=\s*"[^"]*",)', rf'\1\n\2posterPath = "{row["posterPath"]}",', body)
        body = re.sub(r'(\n(\s*)posterPath\s*=\s*"[^"]*",)', rf'\1\n\2tmdbId = {row["tmdbId"]},', body)
        return f"\n{indent}Movie(\n{body}\n{indent})"

    src = re.sub(r"\n(\s*)Movie\(\n(.*?)\n\1\)", patch, src, flags=re.S)
    open(CATALOG, "w", encoding="utf-8").write(src)

    # Haftalık trend örnek listesi: eşleştiği katalog filminin posterini kullanır
    trends = open(TRENDS, encoding="utf-8").read()
    def patch_trend(m):
        block = m.group(0)
        cid = re.search(r'matchedCatalogMovieId\s*=\s*"([^"]+)"', block)
        row = rows.get(cid.group(1)) if cid else None
        if row:
            block = re.sub(r'posterPath\s*=\s*"[^"]*"', f'posterPath = "{row["posterPath"]}"', block)
        return block
    trends = re.sub(r"TrendingCinemaItem\((.*?)\n\s*\)", patch_trend, trends, flags=re.S)
    open(TRENDS, "w", encoding="utf-8").write(trends)
    print(f"{len(rows)} film güncellendi.")


if __name__ == "__main__":
    {"report": report, "apply": apply}.get(sys.argv[1] if len(sys.argv) > 1 else "", lambda: print(__doc__))()
