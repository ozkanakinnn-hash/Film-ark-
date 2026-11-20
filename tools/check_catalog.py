#!/usr/bin/env python3
"""catalog/catalog.json için yayın öncesi denetim (haftalık GitHub Actions işi kullanır).
Uygulamanın doğrulamasıyla uyumlu kurallar; sorun varsa çıkış kodu 1 olur ve hiçbir şey yayınlanmaz."""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
doc = json.load(open(os.path.join(ROOT, "catalog/catalog.json"), encoding="utf-8"))
errors = []
if doc.get("format") != "ne-izlesek-catalog" or doc.get("version") != 1:
    errors.append("biçim/sürüm hatalı")
items = doc.get("items", [])
movies = [i for i in items if i.get("kind") == "movie"]
series = [i for i in items if i.get("kind") == "series"]
if len(movies) < 250:
    errors.append(f"film sayısı çok az: {len(movies)}")
if len(series) < 120:
    errors.append(f"dizi sayısı çok az: {len(series)}")
sections = doc.get("sections") or {}
for key, minimum in (("popularMovies", 10), ("popularSeries", 10), ("worstMovies", 10)):
    if len(sections.get(key, [])) < minimum:
        errors.append(f"{key} listesi çok kısa: {len(sections.get(key, []))}")
bad_ids = [i.get("id") for i in items if not re.fullmatch(r"(t|tv)[0-9]{1,9}", str(i.get("id")))]
if bad_ids:
    errors.append(f"geçersiz kimlikler: {bad_ids[:5]}")
size = os.path.getsize(os.path.join(ROOT, "catalog/catalog.json"))
if size > 3 * 1024 * 1024:
    errors.append(f"dosya 3 MB sınırını aşıyor: {size}")
if errors:
    print("Katalog denetimi BAŞARISIZ:", *errors, sep="\n  - ")
    sys.exit(1)
print(f"Katalog denetimi tamam: {len(movies)} film, {len(series)} dizi, {size // 1024} KB")
