#!/usr/bin/env python3
"""
Uzaktan katalog JSON'unu üretir: catalog/catalog.json

Kaynak: TMDB üreticilerinin yazdığı GeneratedMovies.kt ve GeneratedSeries.kt (aynı veri, APK içinde de var).
Uygulama bu dosyayı haftada bir HTTPS ile indirir (RemoteCatalogRepository), doğrular ve APK içi
kataloğun yerine kullanır. Böylece yeni film/dizi eklemek için uygulama güncellemesi gerekmez:

  python3 tools/tmdb_catalog_build.py --limit 200   # filmleri yenile
  python3 tools/tmdb_series_build.py --limit 100    # dizileri yenile
  python3 tools/export_catalog_json.py              # JSON'u yaz, sonra commit + push (main)
"""
import datetime
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCES = [
    os.path.join(ROOT, "shared/src/commonMain/kotlin/com/example/data/repository/GeneratedMovies.kt"),
    os.path.join(ROOT, "shared/src/commonMain/kotlin/com/example/data/repository/GeneratedSeries.kt"),
]
OUT = os.path.join(ROOT, "catalog/catalog.json")
HIGHLIGHTS = os.path.join(ROOT, "tools/highlights.json")
HIGHLIGHTS_KT = os.path.join(ROOT, "shared/src/commonMain/kotlin/com/example/data/repository/GeneratedHighlights.kt")


def kstr(s):
    return '"' + (s or "").replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'


def write_highlights_kotlin(hl):
    """APK içi yedek: internet yokken de Popüler / Yakında / En Kötüler çalışsın."""
    def ints(xs):
        return ", ".join(str(int(x)) for x in xs)
    rows = []
    for u in hl.get("upcoming", []):
        trailer = kstr(u["trailerKey"]) if u.get("trailerKey") else "null"
        rows.append(
            "        UpcomingRelease(\n"
            f"            id = {kstr(u['id'])},\n"
            f"            tmdbId = {int(u['tmdbId'])},\n"
            f"            title = {kstr(u['title'])},\n"
            f"            originalTitle = {kstr(u.get('originalTitle'))},\n"
            f"            releaseDate = {kstr(u['releaseDate'])},\n"
            f"            durationMins = {int(u.get('durationMins') or 0)},\n"
            f"            genre = {kstr(u.get('genre'))},\n"
            f"            logline = {kstr(u.get('logline'))},\n"
            f"            posterPath = {kstr(u.get('posterPath'))},\n"
            f"            trailerKey = {trailer},\n"
            f"            colorHex = 0x{int(u.get('colorHex') or 0xFF8B5CF6):08X}\n"
            "        )")
    out = [
        "package com.example.data.repository", "",
        "import com.example.data.model.UpcomingRelease", "",
        "// BU DOSYA OTOMATİK ÜRETİLMİŞTİR: python3 tools/export_catalog_json.py (kaynak: tools/highlights.json)",
        "// Elle düzenlemeyin. Bilgiler ve posterler: TMDB.",
        "object GeneratedHighlights {",
        f"    const val GENERATED_AT = {kstr(hl.get('generatedAt'))}",
        f"    val popularMovieTmdbIds: List<Int> = listOf({ints(hl.get('popularMovies', []))})",
        f"    val popularSeriesTmdbIds: List<Int> = listOf({ints(hl.get('popularSeries', []))})",
        f"    val worstMovieTmdbIds: List<Int> = listOf({ints(hl.get('worstMovies', []))})",
        "    /** Hazır çark kutuları: son zamanlarda popüler yapımların yazısız sahne görüntüleri (TMDB yolları). */",
        f"    val popularMovieBackdrops: List<String> = listOf({', '.join(kstr(x) for x in hl.get('popularMovieBackdrops', []))})",
        f"    val popularSeriesBackdrops: List<String> = listOf({', '.join(kstr(x) for x in hl.get('popularSeriesBackdrops', []))})",
        "    val upcoming: List<UpcomingRelease> by lazy {",
        "        listOf(",
        ",\n".join(rows),
        "        )",
        "    }",
        "}",
    ]
    open(HIGHLIGHTS_KT, "w", encoding="utf-8").write("\n".join(out) + "\n")


def unquote(s):
    s = s[1:-1]
    return s.replace('\\"', '"').replace("\\$", "$").replace("\\\\", "\\")


STR = r'"(?:[^"\\]|\\.)*"'


def parse_block(body):
    def s(name):
        m = re.search(rf"\b{name}\s*=\s*({STR})", body)
        return unquote(m.group(1)) if m else ""

    def num(name, cast=int, default=None):
        m = re.search(rf"\b{name}\s*=\s*(0x[0-9A-Fa-f]+|-?[0-9.]+)", body)
        if not m:
            return default
        v = m.group(1)
        return int(v, 16) if v.startswith("0x") else cast(v)

    platforms = re.search(r"platforms\s*=\s*listOf\((.*?)\)", body, re.S)
    audio = re.search(r"audioOptions\s*=\s*listOf\((.*?)\)", body, re.S)
    trailer = re.search(rf"trailerKey\s*=\s*({STR}|null)", body)
    item = {
        "id": s("id"),
        "kind": "series" if "ContentKind.SERIES" in body else "movie",
        "title": s("title"),
        "originalTitle": s("originalTitle"),
        "year": num("year"),
        "durationMins": num("durationMins"),
        "imdb": num("imdb", float),
        "moodTag": s("moodTag"),
        "platforms": [unquote(p) for p in re.findall(STR, platforms.group(1))] if platforms else [],
        "genre": s("genre"),
        "logline": s("logline"),
        "whyWatch": s("whyWatch"),
        "posterPath": s("posterPath"),
        "tmdbId": num("tmdbId"),
        "trailerKey": unquote(trailer.group(1)) if trailer and trailer.group(1) != "null" else None,
        "colorHex": num("colorHex"),
        "audio": (["dubbed"] if audio and "DUBBED" in audio.group(1) else [])
        + (["subtitled"] if audio and "SUBTITLED" in audio.group(1) else []),
        "isCult": bool(re.search(r"isCultOrMostWatched\s*=\s*true", body)),
    }
    seasons = num("seasonCount")
    if seasons:
        item["seasonCount"] = seasons
    return item


def main():
    items = []
    for path in SOURCES:
        src = open(path, encoding="utf-8").read()
        for m in re.finditer(r"\n(\s*)Movie\(\n(.*?)\n\1\)", src, re.S):
            items.append(parse_block(m.group(2)))
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    doc = {
        "format": "ne-izlesek-catalog",
        "version": 1,
        "generatedAt": datetime.date.today().isoformat(),
        "attribution": "Film/dizi bilgileri ve posterler: TMDB. Platform bilgileri: JustWatch. "
                       "This product uses the TMDB API but is not endorsed or certified by TMDB.",
        "items": items,
    }
    if os.path.exists(HIGHLIGHTS):
        hl = json.load(open(HIGHLIGHTS, encoding="utf-8"))
        doc["sections"] = {
            "popularMovies": hl.get("popularMovies", []),
            "popularSeries": hl.get("popularSeries", []),
            "worstMovies": hl.get("worstMovies", []),
            "popularMovieBackdrops": hl.get("popularMovieBackdrops", []),
            "popularSeriesBackdrops": hl.get("popularSeriesBackdrops", []),
        }
        doc["upcoming"] = hl.get("upcoming", [])
        write_highlights_kotlin(hl)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(doc, f, ensure_ascii=False, indent=1)
        f.write("\n")
    kinds = {}
    for i in items:
        kinds[i["kind"]] = kinds.get(i["kind"], 0) + 1
    print(f"{OUT}: {len(items)} kayıt {kinds}, {os.path.getsize(OUT) // 1024} KB")


if __name__ == "__main__":
    main()
