# 24x24 ızgara, 1.8 çizgi, yuvarlak uç/birleşim. Her ikon: (yollar, dolgu vurgusu için yol, neon renk)
ICONS = {
 "cerezlik":   dict(label="Çerezlik", paths=["M6 10.5H18", "M6.5 10.5L8 20.5H16L17.5 10.5", "M10.2 10.5L10.7 20.5", "M13.8 10.5L13.3 20.5",
                    "M6.6 10.5A2.4 2.4 0 0 1 8.2 6.4A2.7 2.7 0 0 1 12 4.6A2.7 2.7 0 0 1 15.8 6.4A2.4 2.4 0 0 1 17.4 10.5"],
                    fill="M6.6 10.5A2.4 2.4 0 0 1 8.2 6.4A2.7 2.7 0 0 1 12 4.6A2.7 2.7 0 0 1 15.8 6.4A2.4 2.4 0 0 1 17.4 10.5Z", neon="#FACC15"),
 "gerilim":    dict(label="Gerilim", paths=["M2.5 12.5H7L9 7.5L12.2 17.5L14.8 10L16.2 12.5H21.5"], fill=None, neon="#8B5CF6"),
 "komedi":     dict(label="Komedi", paths=["M5 4.5H19V11A7 7 0 0 1 5 11Z", "M8.2 9.2C8.8 8.3 10 8.3 10.6 9.2", "M13.4 9.2C14 8.3 15.2 8.3 15.8 9.2", "M8.8 13.2A3.4 3.4 0 0 0 15.2 13.2Z"],
                    fill="M5 4.5H19V11A7 7 0 0 1 5 11Z", neon="#F59E0B"),
 "dram":       dict(label="Dram", paths=["M5 4.5H19V11A7 7 0 0 1 5 11Z", "M8.2 8.6C8.8 9.5 10 9.5 10.6 8.6", "M13.4 8.6C14 9.5 15.2 9.5 15.8 8.6", "M9 15.4A3.4 3.4 0 0 1 15 15.4"],
                    fill="M5 4.5H19V11A7 7 0 0 1 5 11Z", neon="#E9B949"),
 "romantik":   dict(label="Romantik", paths=["M12 20.2S4.5 15.6 4.5 9.8A4.2 4.2 0 0 1 12 7.1A4.2 4.2 0 0 1 19.5 9.8C19.5 15.6 12 20.2 12 20.2Z"],
                    fill="M12 20.2S4.5 15.6 4.5 9.8A4.2 4.2 0 0 1 12 7.1A4.2 4.2 0 0 1 19.5 9.8C19.5 15.6 12 20.2 12 20.2Z", neon="#F43F5E", accent="crimson"),
 "akil":       dict(label="Akıl Oyunları", paths=["M10.75 12A1.25 1.25 0 0 1 13.25 12A2.5 2.5 0 0 1 8.25 12A3.75 3.75 0 0 1 15.75 12A5 5 0 0 1 5.75 12A6.25 6.25 0 0 1 18.25 12"], fill=None, neon="#38BDF8"),
 "kult":       dict(label="Kült & Ödüllü", paths=["M8 4.5H16V9.3A4 4 0 0 1 8 9.3Z", "M8 6H5.6A2.6 2.6 0 0 0 8.5 9.9", "M16 6H18.4A2.6 2.6 0 0 1 15.5 9.9", "M12 13.3V16.8", "M9.5 16.8H14.5L15.3 20H8.7Z"],
                    fill="M8 4.5H16V9.3A4 4 0 0 1 8 9.3Z", neon="#E9B949"),
 "korku":      dict(label="Korku", paths=["M6 20.5V10.8A6 6 0 0 1 18 10.8V20.5L16 18.9L14 20.5L12 18.9L10 20.5L8 18.9Z", "M10 11V11.2", "M14 11V11.2"],
                    fill="M6 20.5V10.8A6 6 0 0 1 18 10.8V20.5L16 18.9L14 20.5L12 18.9L10 20.5L8 18.9Z", neon="#EF4444", accent="crimson"),
 "animasyon":  dict(label="Animasyon", paths=["M11 3.5L12.7 8.4L17.5 10L12.7 11.7L11 16.5L9.3 11.7L4.5 10L9.3 8.4Z", "M18 14.5L18.7 16.3L20.5 17L18.7 17.7L18 19.5L17.3 17.7L15.5 17L17.3 16.3Z"],
                    fill="M11 3.5L12.7 8.4L17.5 10L12.7 11.7L11 16.5L9.3 11.7L4.5 10L9.3 8.4Z", neon="#10B981"),
 "aksiyon":    dict(label="Aksiyon", paths=["M13.5 2.8L5.5 13.5H11.5L10.5 21.2L18.5 10.5H12.5Z"], fill="M13.5 2.8L5.5 13.5H11.5L10.5 21.2L18.5 10.5H12.5Z", neon="#EA580C"),
 "bilimkurgu": dict(label="Bilim Kurgu", paths=["M17 12A5 5 0 1 1 7 12A5 5 0 1 1 17 12Z", "M3.4 16A9.5 3 -25 1 0 20.6 8A9.5 3 -25 1 0 3.4 16"], fill="M17 12A5 5 0 1 1 7 12A5 5 0 1 1 17 12Z", neon="#06B6D4"),
 "macera":     dict(label="Macera", paths=["M20.5 12A8.5 8.5 0 1 1 3.5 12A8.5 8.5 0 1 1 20.5 12Z", "M15.2 8.8L13.3 13.3L8.8 15.2L10.7 10.7Z"], fill="M15.2 8.8L13.3 13.3L8.8 15.2L10.7 10.7Z", neon="#22C55E"),
 "gizem":      dict(label="Gizem", paths=["M16 10.5A5.5 5.5 0 1 1 5 10.5A5.5 5.5 0 1 1 16 10.5Z", "M14.5 14.5L20 20"], fill="M16 10.5A5.5 5.5 0 1 1 5 10.5A5.5 5.5 0 1 1 16 10.5Z", neon="#818CF8"),
 "suc":        dict(label="Suç", paths=["M7.2 7.9A7.9 7.9 0 0 1 19.9 12", "M5.6 16.6C6.1 15.4 6.3 14.1 6.3 12.7A5.7 5.7 0 0 1 17.7 12.7C17.7 14.3 17.6 15.8 17.3 17.3",
                    "M8 19C8.8 17.4 9.2 15.6 9.2 13.7A2.8 2.8 0 0 1 14.8 13.7C14.8 16.1 14.4 18.3 13.6 20.3", "M12 13.7C12 16.3 11.4 18.7 10.3 20.8"], fill=None, neon="#94A3B8"),
}
MOODS = [("cerezlik","Beyin Yormayan / Çerezlik","Ağır dram istemeyenlere, rahatça arkaya yaslanmalık"),
         ("gerilim","Soluksuz Gerilim","Tırnak yedirten, nabzı 140'a fırlatan heyecan"),
         ("komedi","Gözlerden Yaş Getiren Komedi","Günün tüm stresini unutturacak kahkaha dozu"),
         ("romantik","Aşk & Romantizm","Battaniye altına girip sarılmalık romantik filmler"),
         ("akil","Akıl Oyunları / Zihin Bükücü","Sonunda 'Nasıl yani?!' dedirten şok finaller"),
         ("kult","Kült & Ödüllü Sinema","Festival ödüllü, sinema sanatının zirvesi"),
         ("korku","Korku & Adrenalin","Işıkları kapattıracak karanlık hikayeler"),
         ("animasyon","Animasyon & Masalsı","Görsel şölen sunan her yaşa uygun başyapıtlar")]
GOLD, CRIMSON, IVORY = "#E9B949", "#D63447", "#F6F2EA"

def svg(key, style, size=48, light=False):
    ic = ICONS[key]
    bg = "#FFF9EE" if light else "#15131A"
    border = "rgba(180,83,9,0.35)" if light else "rgba(233,185,73,0.28)"
    parts = [f'<rect x="0.5" y="0.5" width="35" height="35" rx="10" fill="{bg}" stroke="{border}"/>']
    g = []
    if style == "A":
        col = "#B45309" if light else GOLD
        g += [f'<path d="{p}" fill="none" stroke="{col}" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' for p in ic["paths"]]
    elif style == "B":
        acc = CRIMSON if ic.get("accent") == "crimson" else GOLD
        line = "#1C1917" if light else IVORY
        if ic["fill"]:
            g.append(f'<path d="{ic["fill"]}" fill="{acc}" fill-opacity="0.85" transform="translate(1.2 1.2)"/>')
        else:
            g.append(f'<circle cx="13.2" cy="13.2" r="7" fill="{acc}" fill-opacity="0.55"/>')
        g += [f'<path d="{p}" fill="none" stroke="{line}" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' for p in ic["paths"]]
    else:
        col = ic["neon"]
        glow = "" if light else f'<filter id="gl{key}"><feGaussianBlur stdDeviation="1.1"/></filter>'
        if glow:
            g.append(glow)
            g += [f'<path d="{p}" fill="none" stroke="{col}" stroke-width="3" stroke-opacity="0.65" stroke-linecap="round" stroke-linejoin="round" filter="url(#gl{key})"/>' for p in ic["paths"]]
        g += [f'<path d="{p}" fill="none" stroke="{"#FFFFFF" if not light else col}" stroke-opacity="{0.9 if not light else 1}" stroke-width="{1.3 if not light else 1.8}" stroke-linecap="round" stroke-linejoin="round"/>' for p in ic["paths"]]
        if not light:
            g += [f'<path d="{p}" fill="none" stroke="{col}" stroke-width="1.8" stroke-opacity="0.55" stroke-linecap="round" stroke-linejoin="round"/>' for p in ic["paths"]]
    parts.append(f'<g transform="translate(6 6)">{"".join(g)}</g>')
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 36 36" width="{size}" height="{size}">{"".join(parts)}</svg>'
