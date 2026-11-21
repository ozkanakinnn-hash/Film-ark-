#!/usr/bin/env python3
"""
TMDB "vitrin" listeleri (derleme zamanı aracı). tools/highlights.json yazar:

- popularMovies : Türkiye'de platformlarda şu an popüler filmler (TMDB tmdbId listesi)
- popularSeries : Türkiye'de platformlarda şu an popüler diziler
- worstMovies   : çok oy almış ama en düşük puanlı filmler ("Bunu Niye İzledim?" çarkı)
- upcoming      : Türkiye'de yakında vizyona girecek / yeni giren filmler (tam bilgiyle)

Film ve dizi üreticileri bu dosyayı okuyup popüler ve "en kötü" yapımları kataloğa öncelikle alır;
export_catalog_json.py de listeleri catalog/catalog.json'a "sections" ve "upcoming" olarak ekler.
Kullanım: python3 tools/tmdb_highlights_build.py   (önce bu, sonra film ve dizi üreticileri)
"""
import datetime
import json
import os
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tmdb_poster_sync import ROOT, api_get  # noqa: E402
from tmdb_catalog_build import GENRE_TR, GENRE_COLORS, first_sentences, pick_trailer  # noqa: E402

OUT = os.path.join(ROOT, "tools/highlights.json")
EXCLUDED_TV = {99, 10762, 10763, 10764, 10766, 10767}


def discover(path, pages, **q):
    ids = []
    for page in range(1, pages + 1):
        data = api_get(path, language="tr-TR", page=page, **q)
        for r in data.get("results", []):
            if not r.get("poster_path") or not (r.get("overview") or "").strip():
                continue
            if 99 in r.get("genre_ids", []) or (path.endswith("/tv") and EXCLUDED_TV & set(r.get("genre_ids", []))):
                continue
            if r["id"] not in ids:
                ids.append(r["id"])
    return ids


def upcoming_items(today):
    start = today - datetime.timedelta(days=21)
    end = today + datetime.timedelta(days=150)
    seen, items = set(), []
    for path in ("/movie/upcoming", "/movie/now_playing"):
        for page in (1, 2, 3):
            data = api_get(path, language="tr-TR", region="TR", page=page)
            for r in data.get("results", []):
                if r["id"] in seen or not r.get("poster_path"):
                    continue
                try:
                    rd = datetime.date.fromisoformat(r.get("release_date") or "")
                except ValueError:
                    continue
                if not (start <= rd <= end) or len((r.get("overview") or "").strip()) < 40:
                    continue
                seen.add(r["id"])
                d = api_get(f"/movie/{r['id']}", language="tr-TR", append_to_response="videos,release_dates",
                            include_video_language="tr,en,null")
                tr_date = rd.isoformat()
                for c in (d.get("release_dates") or {}).get("results", []):
                    if c.get("iso_3166_1") == "TR":
                        theatrical = [x for x in c.get("release_dates", []) if x.get("type") in (2, 3)]
                        if theatrical:
                            tr_date = theatrical[0]["release_date"][:10]
                gids = [g["id"] for g in d.get("genres", [])]
                genres = [GENRE_TR[i] for i in gids if i in GENRE_TR][:2]
                items.append({
                    "id": f"u{d['id']}",
                    "tmdbId": d["id"],
                    "title": d.get("title") or d.get("original_title"),
                    "originalTitle": d.get("original_title") or "",
                    "releaseDate": tr_date,
                    "durationMins": int(d.get("runtime") or 0),
                    "genre": " / ".join(genres) or "Dram",
                    "logline": first_sentences(d.get("overview")),
                    "posterPath": d.get("poster_path") or "",
                    "trailerKey": pick_trailer(d.get("videos", {}).get("results", [])),
                    "colorHex": GENRE_COLORS.get(genres[0] if genres else "", 0xFF8B5CF6),
                    "popularity": round(float(d.get("popularity") or 0), 1),
                })
                time.sleep(0.05)
    items.sort(key=lambda i: (i["releaseDate"], -i["popularity"]))
    return items[:40]


def recent_backdrops(path, ids, date_key, today, want=4):
    """Hazır çark kutuları için son zamanlarda popüler yapımların YAZISIZ sahne görüntüleri."""
    limit = (today - datetime.timedelta(days=540)).isoformat()
    out = []
    for tid in ids:
        if len(out) >= want:
            break
        d = api_get(f"{path}{tid}", language="tr-TR")
        if (d.get(date_key) or "") < limit:
            continue
        imgs = api_get(f"{path}{tid}/images", include_image_language="null")
        bd = [b for b in imgs.get("backdrops", []) if b.get("iso_639_1") is None]
        if bd:
            out.append(bd[0]["file_path"])
        time.sleep(0.05)
    return out


def main():
    today = datetime.date.today()
    tr_flat = dict(watch_region="TR", with_watch_monetization_types="flatrate", sort_by="popularity.desc")
    popular_movies = discover("/discover/movie", 3, **tr_flat, **{"vote_count.gte": 80})[:48]
    popular_series = discover("/discover/tv", 3, **tr_flat, **{"vote_count.gte": 60})[:48]
    worst = []
    for page in range(1, 6):
        data = api_get("/discover/movie", language="tr-TR", page=page, sort_by="vote_average.asc",
                       include_adult="false", **{"vote_count.gte": 1500, "vote_average.lte": 5.6})
        for r in data.get("results", []):
            if r.get("poster_path") and (r.get("overview") or "").strip() and r["id"] not in worst:
                worst.append(r["id"])
    worst = worst[:40]
    upcoming = upcoming_items(today)
    movie_backdrops = recent_backdrops("/movie/", popular_movies, "release_date", today)
    series_backdrops = recent_backdrops("/tv/", popular_series, "last_air_date", today)
    doc = {"generatedAt": today.isoformat(), "popularMovies": popular_movies, "popularSeries": popular_series,
           "worstMovies": worst, "upcoming": upcoming,
           "popularMovieBackdrops": movie_backdrops, "popularSeriesBackdrops": series_backdrops}
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(doc, f, ensure_ascii=False, indent=1)
        f.write("\n")
    print(f"Kutu görüntüleri: {len(movie_backdrops)} film, {len(series_backdrops)} dizi")
    print(f"Popüler film: {len(popular_movies)} | popüler dizi: {len(popular_series)} | "
          f"en kötü: {len(worst)} | yakında: {len(upcoming)}")


if __name__ == "__main__":
    main()
