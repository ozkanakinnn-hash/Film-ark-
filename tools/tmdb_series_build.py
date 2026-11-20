#!/usr/bin/env python3
"""
TMDB dizi kataloğu üreticisi (derleme zamanı aracı).

TMDB'den Türkiye'de izlenebilen, puanı ve oy sayısı yüksek dizileri seçer ve
app/src/main/java/com/example/data/repository/GeneratedSeries.kt dosyasını üretir
(Türkçe ad, özet, sezon sayısı, bölüm süresi, puan, Türkiye platformları, resmi fragman).

Kullanım:
  python3 tools/tmdb_series_build.py --limit 100
Ardından uzaktan katalog JSON'unu yenile:
  python3 tools/export_catalog_json.py

Kimlik doğrulama tmdb_poster_sync.py ile aynıdır (bulutta ağ geçidi token ekler; yerelde TMDB_TOKEN).
"""
import argparse
import os
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tmdb_poster_sync import ROOT, api_get  # noqa: E402
from tmdb_catalog_build import PLATFORM_MAP, GENRE_COLORS, first_sentences, pick_trailer, kotlin_str  # noqa: E402

GENERATED = os.path.join(ROOT, "app/src/main/java/com/example/data/repository/GeneratedSeries.kt")
CHUNK = 25

TV_GENRE_TR = {
    10759: "Aksiyon", 16: "Animasyon", 35: "Komedi", 80: "Suç", 18: "Dram", 10751: "Aile",
    9648: "Gizem", 10765: "Bilim Kurgu", 10768: "Savaş", 37: "Western",
}
EXCLUDED = {99, 10762, 10763, 10764, 10766, 10767}  # belgesel, çocuk, haber, reality, pembe dizi, talk show


def mood_for(gids, rating):
    g = set(gids)
    if 16 in g:
        return "animation"
    if 9648 in g and (10765 in g or 80 in g):
        return "mind_bender"
    if 35 in g and not (g & {80, 9648}):
        return "comedy"
    if g & {80, 9648}:
        return "thriller"
    if 10765 in g:
        return "mind_bender"
    if rating >= 8.3 and 18 in g:
        return "cult_award"
    return "easy_watch"


def why_watch(d, genres):
    tagline = (d.get("tagline") or "").strip()
    if tagline:
        return tagline
    va, seasons = round(d.get("vote_average", 0), 1), d.get("number_of_seasons") or 0
    lead = genres[0].lower() if genres else "dizi"
    if seasons <= 1:
        return f"Tek sezonluk, ★{va} puanlı bir {lead} dizisi: bir hafta sonunda bitir."
    return f"{seasons} sezonluk, ★{va} puanlı bir {lead} dizisi."


def to_series(d):
    gids = [g["id"] for g in d.get("genres", [])]
    rating = round(float(d.get("vote_average") or 0), 1)
    genres = [TV_GENRE_TR[i] for i in gids if i in TV_GENRE_TR][:2]
    year = int((d.get("first_air_date") or "0")[:4] or 0)
    tr = (d.get("watch/providers", {}).get("results", {}) or {}).get("TR", {})
    platforms = []
    for p in tr.get("flatrate", []) or []:
        name = PLATFORM_MAP.get(p.get("provider_name"))
        if name and name not in platforms:
            platforms.append(name)
    runtimes = [r for r in (d.get("episode_run_time") or []) if r]
    last_ep = (d.get("last_episode_to_air") or {}).get("runtime")
    runtime = int(runtimes[0] if runtimes else (last_ep or 45))
    audio = ["subtitled"]
    if d.get("original_language") == "tr" or d.get("vote_count", 0) >= 8000 or 16 in gids:
        audio = ["dubbed", "subtitled"]
    return {
        "id": f"tv{d['id']}",
        "tmdbId": d["id"],
        "title": d.get("name") or d.get("original_name"),
        "originalTitle": d.get("original_name") or "",
        "year": year,
        "durationMins": runtime,
        "seasonCount": int(d.get("number_of_seasons") or 1),
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
        "isCult": year <= 2010 and rating >= 8.3,
    }


def candidates(limit):
    queries = [
        dict(watch_region="TR", with_watch_monetization_types="flatrate", sort_by="vote_count.desc", **{"vote_count.gte": 800}),
        dict(watch_region="TR", with_watch_monetization_types="flatrate", sort_by="popularity.desc",
             **{"vote_count.gte": 200, "first_air_date.gte": "2022-01-01"}),
        dict(with_original_language="tr", sort_by="vote_count.desc", **{"vote_count.gte": 60}),
    ]
    seen, order = set(), []
    for page in range(1, 9):
        for q in queries:
            data = api_get("/discover/tv", language="tr-TR", page=page, **q)
            for r in data.get("results", []):
                if r["id"] in seen or EXCLUDED & set(r.get("genre_ids", [])):
                    continue
                if not r.get("poster_path") or not (r.get("overview") or "").strip():
                    continue
                seen.add(r["id"])
                order.append(r["id"])
        if len(order) >= limit * 2:
            break
    return order


def write_generated(items):
    parts = [items[i:i + CHUNK] for i in range(0, len(items), CHUNK)]
    out = ["package com.example.data.repository", "",
           "import com.example.data.model.AudioLanguageOption",
           "import com.example.data.model.ContentKind",
           "import com.example.data.model.Movie", "",
           "// BU DOSYA OTOMATİK ÜRETİLMİŞTİR: python3 tools/tmdb_series_build.py",
           "// Elle düzenlemeyin; değişiklikler bir sonraki üretimde kaybolur.",
           "// Dizi bilgileri ve posterler: TMDB. Platform bilgileri: JustWatch (TMDB üzerinden).",
           "internal object GeneratedSeries {",
           "    val series: List<Movie> by lazy {",
           "        " + (" + ".join(f"part{i + 1}()" for i in range(len(parts))) or "emptyList()"),
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
                f"            isCultOrMostWatched = {'true' if m['isCult'] else 'false'},\n"
                f"            kind = ContentKind.SERIES,\n"
                f"            seasonCount = {m['seasonCount']}\n"
                "        )")
        out.append(",\n".join(rows))
        out.append("    )")
        out.append("")
    out.append("}")
    open(GENERATED, "w", encoding="utf-8").write("\n".join(out) + "\n")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--limit", type=int, default=200)
    args = ap.parse_args()
    items = []
    ids = candidates(args.limit)
    hl_path = os.path.join(ROOT, "tools/highlights.json")
    if os.path.exists(hl_path):
        import json
        ids = list(dict.fromkeys(json.load(open(hl_path, encoding="utf-8")).get("popularSeries", []) + ids))
    for tid in ids:
        if len(items) >= args.limit:
            break
        d = api_get(f"/tv/{tid}", language="tr-TR", append_to_response="videos,watch/providers",
                    include_video_language="tr,en,null")
        if len((d.get("overview") or "").strip()) < 60 or not d.get("first_air_date"):
            continue
        if EXCLUDED & {g["id"] for g in d.get("genres", [])}:
            continue
        items.append(to_series(d))
        time.sleep(0.05)
    items.sort(key=lambda m: (-m["year"], m["title"]))
    write_generated(items)
    moods = {}
    for m in items:
        moods[m["moodTag"]] = moods.get(m["moodTag"], 0) + 1
    print(f"Üretilen dizi: {len(items)} | ruh hali dağılımı: {moods}")
    print(f"Fragmanı olan: {sum(1 for m in items if m['trailerKey'])} | "
          f"Türkiye'de platformu bilinen: {sum(1 for m in items if m['platforms'] != ['Diğer'])}")
    import export_catalog_json
    export_catalog_json.main()  # uzaktan katalog JSON'unu da yenile


if __name__ == "__main__":
    main()
