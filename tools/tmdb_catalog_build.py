#!/usr/bin/env python3
"""
TMDB katalog üreticisi (derleme zamanı aracı).

1) Elle seçilmiş 53 filmlik kataloğa (MovieRepository.kt) resmi Türkçe adları ve resmi YouTube
   fragman anahtarlarını ekler.
2) TMDB'den tür, dönem ve Türk yapımı dengesi gözetilerek seçilen ek filmlerle
   GeneratedMovies.kt dosyasını üretir (Türkçe ad, özet, süre, puan, Türkiye platformları, fragman).

Kullanım:
  python3 tools/tmdb_catalog_build.py            # kataloğu üret / güncelle
  python3 tools/tmdb_catalog_build.py --limit 150

Kimlik doğrulama tmdb_poster_sync.py ile aynıdır (bulutta ağ geçidi token ekler; yerelde TMDB_TOKEN).
Platform verisi TMDB üzerinden JustWatch'a aittir ve uygulamada JustWatch'a atıf yapılır.
"""
import argparse
import json
import os
import re
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tmdb_poster_sync import CATALOG, ROOT, api_get, parse_catalog  # noqa: E402

GENERATED = os.path.join(ROOT, "app/src/main/java/com/example/data/repository/GeneratedMovies.kt")
CHUNK = 25  # JVM metot boyutu sınırına takılmamak için parça başına film

GENRE_TR = {
    28: "Aksiyon", 12: "Macera", 16: "Animasyon", 35: "Komedi", 80: "Suç", 18: "Dram",
    10751: "Aile", 14: "Fantastik", 36: "Tarih", 27: "Korku", 10402: "Müzik", 9648: "Gizem",
    10749: "Romantik", 878: "Bilim Kurgu", 53: "Gerilim", 10752: "Savaş", 37: "Western",
}
PLATFORM_MAP = {
    "Netflix": "Netflix",
    "Amazon Prime Video": "Prime Video",
    "Amazon Prime Video with Ads": "Prime Video",
    "Disney Plus": "Disney+",
    "MUBI": "Mubi",
    "MUBI Amazon Channel": "Mubi",
    "TOD TV": "TOD",
    "Apple TV Plus": "Apple TV+",
    "Apple TV+": "Apple TV+",
    "HBO Max": "BluTV",  # BluTV, Türkiye'de HBO Max olarak yeniden markalandı
    "BluTV": "BluTV",
    "Gain": "Gain",
}
GENRE_COLORS = {
    "Korku": 0xFF7F1D1D, "Bilim Kurgu": 0xFF1D4ED8, "Romantik": 0xFFBE185D, "Komedi": 0xFFD97706,
    "Aksiyon": 0xFFC2410C, "Gerilim": 0xFF6D28D9, "Animasyon": 0xFF059669, "Gizem": 0xFF4338CA,
    "Suç": 0xFF334155, "Dram": 0xFF9F1239, "Macera": 0xFF0F766E, "Fantastik": 0xFF7C3AED,
}


def mood_for(gids, rating):
    g = set(gids)
    if 27 in g:
        return "horror"
    if 16 in g:
        return "animation"
    if 9648 in g and (878 in g or 53 in g):
        return "mind_bender"
    if 10749 in g:
        return "romance"
    if 35 in g and not (g & {53, 80}):
        return "comedy"
    if g & {53, 80, 9648}:
        return "thriller"
    if rating >= 7.8 and g & {18, 36, 10752}:
        return "cult_award"
    return "easy_watch"


def first_sentences(text, limit=230):
    text = re.sub(r"\s+", " ", (text or "").strip())
    if len(text) <= limit:
        return text
    cut = text[:limit]
    end = max(cut.rfind(". "), cut.rfind("! "), cut.rfind("? "))
    return (cut[: end + 1] if end > 80 else cut.rsplit(" ", 1)[0] + "…").strip()


def pick_trailer(videos):
    yt = [v for v in videos if v.get("site") == "YouTube" and v.get("type") == "Trailer"]
    yt.sort(key=lambda v: (not v.get("official", False), v.get("iso_639_1") != "tr"))
    return yt[0]["key"] if yt else None


def details(tmdb_id):
    d = api_get(f"/movie/{tmdb_id}", language="tr-TR",
                append_to_response="videos,watch/providers", include_video_language="tr,en,null")
    return d


def why_watch(d, genres):
    tagline = (d.get("tagline") or "").strip()
    if tagline:
        return tagline
    va, vc = round(d.get("vote_average", 0), 1), d.get("vote_count", 0)
    year = (d.get("release_date") or "0000")[:4]
    lead = genres[0].lower() if genres else "sinema"
    if vc >= 10000:
        return f"TMDB'de {vc:,} oyla ★{va}: {lead} sevenlerin ortak kararı.".replace(",", ".")
    if year >= "2023":
        return f"{year} yapımı, ★{va} puanlı taze bir {lead} seçeneği."
    return f"★{va} puanıyla kendini kanıtlamış bir {lead} filmi."


def kotlin_str(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'


def to_movie(d):
    gids = [g["id"] for g in d.get("genres", [])]
    rating = round(float(d.get("vote_average") or 0), 1)
    # Ruh halini belirleyen tür başta olsun (tür sahnesi ve rozetler ilk türe bakar)
    lead = {"horror": 27, "animation": 16, "romance": 10749, "comedy": 35}.get(mood_for(gids, rating))
    if lead in gids:
        gids = [lead] + [g for g in gids if g != lead]
    genres = [GENRE_TR[i] for i in gids if i in GENRE_TR][:2]
    year = int((d.get("release_date") or "0")[:4] or 0)
    tr = (d.get("watch/providers", {}).get("results", {}) or {}).get("TR", {})
    platforms = []
    for p in tr.get("flatrate", []) or []:
        name = PLATFORM_MAP.get(p.get("provider_name"))
        if name and name not in platforms:
            platforms.append(name)
    is_family = bool(set(gids) & {16, 10751})
    audio = ["subtitled"]
    if is_family or d.get("original_language") == "tr" or d.get("vote_count", 0) >= 8000:
        audio = ["dubbed", "subtitled"]
    return {
        "id": f"t{d['id']}",
        "tmdbId": d["id"],
        "title": d.get("title") or d.get("original_title"),
        "originalTitle": d.get("original_title") or "",
        "year": year,
        "durationMins": int(d.get("runtime") or 100),
        "imdb": rating,
        "moodTag": mood_for(gids, rating),
        "platforms": platforms or ["Diğer"],
        "genre": " / ".join(genres) or "Dram",
        "logline": first_sentences(d.get("overview")),
        "whyWatch": why_watch(d, genres),
        "posterPath": d.get("poster_path") or "",
        "trailerKey": pick_trailer(d.get("videos", {}).get("results", [])),
        "colorHex": GENRE_COLORS.get(genres[0] if genres else "", 0xFF8B5CF6),
        "audio": audio,
        "isCult": year <= 2005 and rating >= 8.0,
    }


def candidates(limit, exclude):
    """Tür ve dönem dengesi gözeterek aday film kimlikleri toplar."""
    queries = []
    for gid in (27, 16, 10749, 35, 53, 9648, 878, 28, 18, 80, 12, 14):
        queries.append(dict(with_genres=gid, sort_by="vote_count.desc", **{"vote_count.gte": 1500}))
    queries.append(dict(sort_by="popularity.desc", **{"primary_release_date.gte": "2023-01-01", "vote_count.gte": 300}))
    queries.append(dict(with_original_language="tr", sort_by="vote_count.desc", **{"vote_count.gte": 80}))
    queries.append(dict(watch_region="TR", with_watch_monetization_types="flatrate", sort_by="popularity.desc",
                        **{"vote_count.gte": 500}))
    seen, order = set(exclude), []
    page = 1
    while len(order) < limit and page <= 4:
        for q in queries:
            data = api_get("/discover/movie", language="tr-TR", include_adult="false", page=page, **q)
            for r in data.get("results", []):
                if r["id"] in seen or 99 in r.get("genre_ids", []) or 10770 in r.get("genre_ids", []):
                    continue
                if not r.get("poster_path") or not (r.get("overview") or "").strip():
                    continue
                seen.add(r["id"])
                order.append(r["id"])
        page += 1
    return order


def write_generated(movies):
    parts = [movies[i:i + CHUNK] for i in range(0, len(movies), CHUNK)]
    out = ["package com.example.data.repository", "",
           "import com.example.data.model.AudioLanguageOption",
           "import com.example.data.model.Movie", "",
           "// BU DOSYA OTOMATİK ÜRETİLMİŞTİR: python3 tools/tmdb_catalog_build.py",
           "// Elle düzenlemeyin; değişiklikler bir sonraki üretimde kaybolur.",
           "// Film bilgileri ve posterler: TMDB. Platform bilgileri: JustWatch (TMDB üzerinden).",
           "internal object GeneratedMovies {",
           "    val movies: List<Movie> by lazy {",
           "        " + " + ".join(f"part{i + 1}()" for i in range(len(parts))),
           "    }", ""]
    for i, part in enumerate(parts):
        out.append(f"    private fun part{i + 1}(): List<Movie> = listOf(")
        rows = []
        for m in part:
            audio = ", ".join(f"AudioLanguageOption.{'DUBBED' if a == 'dubbed' else 'SUBTITLED'}.id" for a in m["audio"])
            trailer = kotlin_str(m["trailerKey"]) if m["trailerKey"] else "null"
            rows.append(
                "        Movie(\n"
                f"            id = {kotlin_str(m['id'])},\n"
                f"            title = {kotlin_str(m['title'])},\n"
                f"            originalTitle = {kotlin_str(m['originalTitle'])},\n"
                f"            year = {m['year']},\n"
                f"            durationMins = {m['durationMins']},\n"
                f"            imdb = {m['imdb']},\n"
                f"            moodTag = {kotlin_str(m['moodTag'])},\n"
                f"            platforms = listOf({', '.join(kotlin_str(p) for p in m['platforms'])}),\n"
                f"            genre = {kotlin_str(m['genre'])},\n"
                f"            logline = {kotlin_str(m['logline'])},\n"
                f"            whyWatch = {kotlin_str(m['whyWatch'])},\n"
                f"            posterPath = {kotlin_str(m['posterPath'])},\n"
                f"            tmdbId = {m['tmdbId']},\n"
                f"            trailerKey = {trailer},\n"
                f"            colorHex = 0x{m['colorHex']:08X},\n"
                f"            audioOptions = listOf({audio}),\n"
                f"            isCultOrMostWatched = {'true' if m['isCult'] else 'false'}\n"
                "        )")
        out.append(",\n".join(rows))
        out.append("    )")
        out.append("")
    out.append("}")
    open(GENERATED, "w", encoding="utf-8").write("\n".join(out) + "\n")


curated_collections = set()


def patch_curated():
    """Elle seçilmiş filmlere resmi Türkçe ad ve fragman anahtarı ekler."""
    report = {r["id"]: r for r in json.load(open(os.path.join(ROOT, "tools/tmdb_poster_report.json"), encoding="utf-8"))}
    src = open(CATALOG, encoding="utf-8").read()
    changed_titles = []
    curated_collections.clear()

    def patch(m):
        indent, body = m.group(1), m.group(2)
        mid = re.search(r'\bid\s*=\s*"([^"]+)"', body)
        row = report.get(mid.group(1)) if mid else None
        if not row or not row.get("tmdbId"):
            return m.group(0)
        d = details(row["tmdbId"])
        if d.get("belongs_to_collection"):
            curated_collections.add(d["belongs_to_collection"]["id"])
        tr_title = (d.get("title") or "").strip()
        old = re.search(r'\btitle\s*=\s*"([^"]*)"', body).group(1)
        if tr_title and tr_title != old:
            body = re.sub(r'\btitle\s*=\s*"[^"]*"', lambda _: "title = " + kotlin_str(tr_title), body, count=1)
            changed_titles.append((old, tr_title))
        key = pick_trailer(d.get("videos", {}).get("results", []))
        field_indent = re.match(r"(\s*)", body).group(1)
        body = re.sub(rf"\n{field_indent}trailerKey\s*=\s*[^\n]*,", "", body)
        if key:
            body = re.sub(r"(\n(\s*)tmdbId\s*=\s*\d+,)", rf'\1\n\2trailerKey = "{key}",', body)
        time.sleep(0.05)
        return f"\n{indent}Movie(\n{body}\n{indent})"

    src = re.sub(r"\n(\s*)Movie\(\n(.*?)\n\1\)", patch, src, flags=re.S)
    open(CATALOG, "w", encoding="utf-8").write(src)
    return changed_titles


def sync_trend_titles():
    """Haftalık trend örnek listesindeki adları eşleştikleri katalog filminin adıyla aynı yapar."""
    from tmdb_poster_sync import TRENDS
    catalog = open(CATALOG, encoding="utf-8").read()
    titles = {}
    for m in re.finditer(r"\n\s*Movie\(\n(.*?)\n\s*\)", catalog, re.S):
        mid = re.search(r'\bid\s*=\s*"([^"]+)"', m.group(1))
        t = re.search(r'\btitle\s*=\s*("(?:[^"\\]|\\.)*")', m.group(1))
        if mid and t:
            titles[mid.group(1)] = t.group(1)
    src = open(TRENDS, encoding="utf-8").read()
    changed = []

    def patch(m):
        block = m.group(0)
        cid = re.search(r'matchedCatalogMovieId\s*=\s*"([^"]+)"', block)
        new = titles.get(cid.group(1)) if cid else None
        if new:
            old = re.search(r'\btitle\s*=\s*("(?:[^"\\]|\\.)*")', block).group(1)
            if old != new:
                changed.append((old, new))
                block = block.replace(f"title = {old}", f"title = {new}", 1)
        return block

    src = re.sub(r"TrendingCinemaItem\((.*?)\n\s*\)", patch, src, flags=re.S)
    open(TRENDS, "w", encoding="utf-8").write(src)
    return changed


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--limit", type=int, default=200)
    args = ap.parse_args()

    titles = patch_curated()
    for old, new in sync_trend_titles():
        print(f"  Trend listesi: {old} -> {new}")
    print(f"Elle seçilen katalog: {len(titles)} film resmi Türkçe adına güncellendi")
    for old, new in titles:
        print(f"  {old}  ->  {new}")

    curated_ids = {r["tmdbId"] for r in json.load(open(os.path.join(ROOT, "tools/tmdb_poster_report.json"), encoding="utf-8"))}
    ids = candidates(args.limit + 160, curated_ids)
    movies = []
    by_collection = {}
    for tid in ids:
        if len(movies) >= args.limit:
            break
        d = details(tid)
        if not d.get("runtime") or d["runtime"] < 70 or len((d.get("overview") or "").strip()) < 60:
            continue
        coll = (d.get("belongs_to_collection") or {}).get("id")
        if coll:
            # Seriler çarkı tek tipleştirmesin: her seriden yalnızca en yüksek puanlı tek film
            if coll in curated_collections:
                continue
            prev = by_collection.get(coll)
            if prev is not None:
                if d.get("vote_average", 0) > prev["imdb"]:
                    movies[movies.index(prev)] = to_movie(d)
                    by_collection[coll] = movies[-1] if False else next(m for m in movies if m["tmdbId"] == d["id"])
                continue
        movie = to_movie(d)
        movies.append(movie)
        if coll:
            by_collection[coll] = movie
        time.sleep(0.05)
    movies.sort(key=lambda m: (-m["year"], m["title"]))
    write_generated(movies)
    moods = {}
    for m in movies:
        moods[m["moodTag"]] = moods.get(m["moodTag"], 0) + 1
    print(f"Üretilen ek film: {len(movies)}  | ruh hali dağılımı: {moods}")
    print(f"Fragmanı olan: {sum(1 for m in movies if m['trailerKey'])}  | "
          f"Türkiye'de platformu bilinen: {sum(1 for m in movies if m['platforms'] != ['Diğer'])}")


if __name__ == "__main__":
    main()
