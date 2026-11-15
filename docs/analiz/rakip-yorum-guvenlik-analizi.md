# Ne İzlesek? — Rakip, Kullanıcı Yorumu ve Güvenlik Analizi

_Kasım 2026 • kod tabanı `ccr-86f8678f-sm26xa` dalı (commit `85a488b`) üzerinden_

---

## 1. Rakipler ve konumlanma

| Uygulama | Ne yapıyor | Güçlü yanı | Zayıf yanı |
|---|---|---|---|
| **JustWatch** | Hangi film/dizi hangi platformda (Türkiye dahil) | Dev, güncel katalog ve platform verisi | Karar vermez; liste kalabalığı. Yorumlarda kararlılık/çökme şikâyetleri |
| **Letterboxd** | Film günlüğü + sosyal ağ | Topluluk, puan, listeler | "Bu akşam ne izleyelim?" sorusuna doğrudan cevap vermez |
| **Matched (Taste Labs)** | Çiftler için Tinder tarzı eşleşme | Gerçek iki-telefon eşleşmesi | Ortalama ~3.6 puan; öneriler "trend" ağırlıklı |
| **CineDuo, WatchPick, FlickPicker, NxtUp, KiSwipe…** | Kaydır-eşleş film seçimi | Yeni, odaklı | Çoğu yeni/az puanlı; bu türdeki birçok uygulama kapanmış |
| **Spin the Wheel / Random Movie Picker / Spinflix** | Çark/rastgele seçim | Basit, hızlı | En büyük şikâyet: aşırı reklam; tekrar eden öneriler; takılan animasyonlar |
| **TV Time** | İzleme takibi | Büyük kullanıcı tabanı | Kapanma süreci; verilerini dışa aktaramayan kullanıcılar |
| **Türkiye'de** ("Ne izlesek" adlı iOS uygulaması, Justin Tv, Gnovies, "Nerede izlenir") | Öneri / platform bulma | Yerel | Çoğu tek özellikli; dublaj/altyazı ve yerel platform odağı zayıf |

**Konumlanma:** Ne İzlesek?, "listeleyen" (JustWatch), "kaydeden" (Letterboxd) ve "eşleştiren" (Matched) uygulamaların arasında **kararı oyunlaştıran ve Türkiye'ye özel** tek uygulama olarak konumlanabilir.

## 2. Artılar (bizde olup rakiplerde zayıf olanlar)

1. **Kararı bitiren mekanik:** Çark + tek veto + "kader kilitlendi" akışı. Rakiplerin çoğu sadece liste ya da kaydırma sunuyor.
2. **Türkiye odaklı:** Türkçe arayüz, resmi Türkçe film adları, Gain/TOD/Mubi/HBO Max, **Türkçe dublaj/altyazı filtresi** (rakiplerde yok denecek kadar az).
3. **Hesapsız ve gizlilik dostu:** Kayıt yok, analiz/reklam SDK'sı yok, veriler cihazda.
4. **Görsel kalite:** Türe özel sinematik açılış sahneleri, logoya uygun çark, neon ikonlar, Neon Bilet kartları, hikâye boyutunda paylaşım.
5. **Eğlenceli modlar:** VS düellosu (tam ekran kaydırma), salla-çevir, özel listeler, geçmiş istatistikleri, widget.
6. **Fragman ve "Daha Sonra İzle":** Karar sonrası akış uygulamanın içinde tamamlanıyor.

## 3. Eksikler (rakiplerin önde olduğu ya da yanıltıcı olabilecek yerler)

| # | Eksik | Neden önemli | Ciddiyet |
|---|---|---|---|
| E1 | **"Ortak oda" gerçek değil.** Oda kodu yalnızca aynı telefonda çalışıyor, partner "simüle" ediliyor. Tanıtımda ise "eşli senkronize ortak oda" yazıyor | Kullanıcı ikinci telefonda kodu girince çalışmaz → 1 yıldızlı yorum riski; mağaza tanıtımında yanıltıcı ifade | **Yüksek** |
| E2 | **"Yeni Çıkanlar • Günlük Güncel" gerçekte sabit katalogdan dönüşümlü seçiliyor** | "Güncel" vaadi tutmuyor; yeni filmler APK güncellemesiyle geliyor | Orta |
| E3 | **"Haftanın Trendleri" örnek liste + "Firebase gerekli" uyarısı** | Kullanıcıya yarım kalmış özellik gibi görünüyor | Orta |
| E4 | **Sabit katalog (253 film), dizi yok** | JustWatch/Matched binlerce film + dizi sunuyor; platform bilgisi zamanla eskir | Orta |
| E5 | **Uygulama genelinde film arama yok** (yalnızca özel liste düzenleyicide ve geçmişte) | "X filmi çarka eklemek istiyorum" isteği karşılanmıyor | Orta |
| E6 | **Kişiselleştirme yok** (veto/izlenen/favori önerileri etkilemiyor) | Öneriler zamanla "beni tanımıyor" hissi verir | Düşük–Orta |
| E7 | **Yedekleme / dışa aktarma yok** | TV Time kapanışında en çok şikâyet edilen konu bu | Düşük |
| E8 | **Animasyonları azaltma seçeneği yok** | Erişilebilirlik; düşük donanımlı telefonlarda takılma | Düşük |

## 4. Olumsuz yorum analizi (kategori genelinde) ve bize dersleri

| Rakiplerde sık şikâyet | Örnek kaynak | Bizdeki durum | Ders / aksiyon |
|---|---|---|---|
| **Aşırı reklam** ("ekranın yarısı reklam", "her 10 saniyede reklam") | Spin the Wheel türü uygulamalar | Şu an reklam yok | AdMob gelince: çark dönerken ve sonuç ekranında **asla** tam ekran reklam yok; yalnızca liste aralarında banner ya da isteğe bağlı ödüllü reklam. "Reklamsız" tek seferlik satın alma seçeneği |
| **Hep aynı öneriler** | Rastgele seçici uygulamaları | Düzeltildi (12 dilimlik örnekleyici + 253 film) | Katalog tazeliği (E2, E4) çözülmezse şikâyet geri gelir |
| **Takılan/zıplayan çark animasyonu** | Çark uygulamaları | Akıcı ama yoğun efektler var | "Animasyonları azalt" ayarı (E8); düşük donanım testi |
| **Yanlış platform bilgisi** | JustWatch/izleme uygulamaları | JustWatch verisi + "değişebilir" uyarısı var | Periyodik katalog güncellemesi |
| **Çökme / açılmama** | JustWatch, TV Time | Yaşandı (izin hatası) → düzeltildi, çökme raporlayıcı eklendi | Her sürümden önce Lint + cihaz testi |
| **Partnerin de uygulamayı indirmesi gerekiyor / eşleşme senkronu** | Kaydır-eşleş uygulamaları | Gerçek senkron yok (E1) | Kısa vadede "tek telefonda birlikte" moduna dönüştür; uzun vadede gerçek senkron |
| **Verimi alamıyorum / kapanınca kayboldu** | TV Time | Yedekleme yok (E7) | JSON dışa/içe aktarma |
| **Öneriler trend ağırlıklı, zevkime uymuyor** | Matched | Kişiselleştirme yok (E6) | Veto ve izlenenlerden basit tür ağırlıklandırma |

## 5. Eklenebilecekler (öncelik sırasıyla)

1. **Ortak odayı dürüstleştir** (E1) — iki seçenek:
   - *Hızlı:* "Tek telefonda birlikte seçin" moduna dönüştür (telefon elden ele geçer; her kişi kendi ruh halini seçer). "Partneri simüle et" düğmesini kaldır, tanıtım metnini düzelt.
   - *Kapsamlı:* Gerçek iki telefon eşleşmesi (sunucu gerekir; gizlilik politikası güncellenir).
2. **Uzaktan katalog güncellemesi** (E2, E4): Katalog üreticimiz zaten var. Çıktısını GitHub'da bir JSON dosyası olarak yayınlarız; uygulama haftada bir indirir. API anahtarı gerekmez, APK güncellemesi olmadan yeni filmler ve güncel platformlar gelir.
3. **Uygulama geneli film arama** (E5): Katalog içi arama + "Çarka Ekle".
4. **Dizi desteği** (E4): Aynı üretici ile TMDB dizileri; "Film / Dizi" anahtarı.
5. **Akıllı öneri** (E6): Veto edilen türlerin ağırlığını azalt, favori/izlenen türleri artır; kartta "Neden bu film?" satırı.
6. **Yedekle / Geri yükle** (E7): Geçmiş, izlenenler, listeler, "Daha Sonra İzle" → tek dosya.
7. **Erişilebilirlik:** "Animasyonları azalt" ayarı, ekran okuyucu etiketleri, büyük yazı desteği testi.
8. **Reklam planı (lisans sonrası):** Kurallar yukarıdaki tabloda; ayrıca reklamsız satın alma.

## 6. Çıkarılabilecekler / sadeleştirilecekler

| Öğe | Neden |
|---|---|
| **"Partneri Simüle Et" düğmesi** | Sahte partner kafa karıştırır (E1) |
| **"Haftanın Trendleri" örnek liste + "Firebase gerekli" uyarısı** | Gerçek veri yok; gerçek veri gelene kadar gizle ya da "Editörün Seçimi" yap (E3) |
| **"Günlük Güncel" ifadesi** | Uzaktan güncelleme gelene kadar "Öne Çıkanlar" yaz (E2) |
| **Kullanılmayan kod: `AnimatedDecisionWheel.kt` (698 satır)** | Hiçbir yerde kullanılmıyor |
| **Kullanılmayan kütüphaneler: Retrofit, Moshi, OkHttp, logging-interceptor** | Kodda hiç kullanılmıyor; APK boyutu ve saldırı yüzeyi |
| **Çark ekranındaki sensör tanılama kartı** (ivmeölçer yoğunluk çubuğu vb.) | Ana akışı kalabalıklaştırıyor; ayarlara taşınabilir |
| **Birbirine benzeyen paylaşım ekranları** (Flex hikâye kartı, sosyal paylaşım sayfası, VS hikâye kartı) | Tek, tutarlı bir "Paylaş" deneyiminde birleştirilebilir |

## 7. Güvenlik taraması

**Kapsam:** manifest ve dışa açık bileşenler, tüm git geçmişinde gizli anahtar taraması, ağ güvenliği, yedekleme kuralları, veritabanı sorguları, dosya paylaşımı, PendingIntent'ler, bağımlılıklar, Android Lint.

### Bulgular

| # | Bulgu | Risk | Öneri |
|---|---|---|---|
| G1 | **Yedekleme açık (`allowBackup=true`) ve kurallar boş:** izleme geçmişi, notlar, listeler ve çökme raporu Google yedeğine ve cihaz taşımaya dahil. Gizlilik politikası "yalnızca cihazda" diyor | **Orta** (gizlilik beyanıyla çelişki) | Ya yedeklemeyi kapat ya da politikaya "Google yedeklemeniz açıksa uygulama verileri yedeklenebilir" ekle; çökme raporunu her durumda hariç tut |
| G2 | **Test APK'ları debug derleme:** `debuggable`, geçici anahtarla imzalı, kod küçültme (R8) kapalı | Orta (yalnızca dağıtımda) | Yayın için `release` derleme: kendi yükleme anahtarın, `minifyEnabled + shrinkResources`, Play App Signing |
| G3 | **Widget alıcısı dışa açık** ve özel eylemleri (çevir / günün filmi) **her uygulamadan** kabul ediyor | Düşük (yalnızca widget içeriği değişir) | Özel eylemleri dışa kapalı ayrı bir alıcıya taşı; dışa açık alıcıda yalnızca `APPWIDGET_UPDATE` |
| G4 | **Ağ güvenliği yapılandırması yok:** Android 7–8.1'de (minSdk 24) şifresiz HTTP varsayılan olarak serbest | Düşük (tüm adresler zaten HTTPS) | `network_security_config` ile `cleartextTrafficPermitted="false"` |
| G5 | **Kullanılmayan ağ kütüphaneleri APK'da** (OkHttp 4.10 — eski sürüm, Retrofit, logging-interceptor) | Düşük | Bağımlılıkları kaldır |
| G6 | **Eski bağımlılıklar:** Compose BOM 2024.09 (2 yıllık), Room 2.7.0, Lifecycle 2.8.7, Activity 1.10.1, Coroutines 1.10.2 | Düşük–Orta (hata/güvenlik yamaları kaçıyor) | Kontrollü toplu güncelleme + tam test |
| G7 | **Paket adı `com.aistudio…`** | Bilgi (güven/marka) | Yayından önce kalıcı paket adı seç |
| G8 | **`MainActivity` dışa açık ve widget film kimliğini kabul ediyor** | Çok düşük | Kimlik katalogda aranıyor; bilinmeyen kimlik yok sayılıyor. İşlem gerekmez |

### Temiz çıkan kontroller ✅
- **Gizli anahtar yok:** Tüm git geçmişinde API anahtarı, token, keystore, `.env`, `google-services.json` bulunmadı. TMDB token'ı yalnızca bulut ortamının ağ geçidinde.
- **APK'da TMDB anahtarı yok.** Telefon sadece `image.tmdb.org`'dan görsel indiriyor.
- **İzinler doğru ve minimal:** INTERNET, ACCESS_NETWORK_STATE, VIBRATE (bir test bunu koruyor). Lint: 0 hata.
- **WebView yok, ham SQL yok** (Room parametreli sorgular). Hassas günlük kaydı yok.
- **FileProvider** dışa kapalı ve yalnızca `cache/shared_posters` klasörünü paylaşıyor; izin her paylaşımda tek seferlik.
- **PendingIntent'ler `FLAG_IMMUTABLE`.**
- **Çökme raporu ekranı** dışa kapalı, ayrı süreçte; rapor kişisel veri içermiyor ve yalnızca kullanıcı isterse paylaşılıyor.
- **Fragmanlar ve TMDB bağlantısı** sabit HTTPS adresleri; kullanıcı girdisinden URL üretilmiyor.

## Kaynaklar
- [FlickPicker (App Store)](https://apps.apple.com/us/app/flickpicker/id6759133156) · [WatchPick](https://apps.apple.com/app/id6759346000) · [CineDuo](https://apps.apple.com/us/app/-/id6758887873) · [MoviePicker](https://apps.apple.com/us/app/-/id6757856312) · [NxtUp (Google Play)](https://play.google.com/store/apps/details?id=com.nxtup.app) · [KiSwipe](https://hunted.space/product/kiswipe) · [Shared Watchlist](https://apps.apple.com/us/app/shared-watchlist/id6755390014) · [Cinetic](https://www.producthunt.com/p/cinetic-what-to-watch/cinetic-what-to-watch) · [ReelMatch](https://apps.apple.com/app/id6457263386)
- [Tasteray — çiftler için film uygulamaları](https://www.tasteray.com/review/best-movie-apps-for-couples) · [Matched (App Store)](https://apps.apple.com/us/app/-/id1623287922) · [Matched — AppGoblin](https://appgoblin.info/apps/io.taste.matched) · [Netflip](https://apps.apple.com/app/netflip/id1558281282) · [Movie Match](https://www.producthunt.com/products/movie-match) · [MatchWatch](https://www.trustpilot.com/review/matchwatch.tv)
- [JustWatch — android-stats](https://android-stats.com/d/com.justwatch.justwatch) · [JustWatch — Kimola raporu](https://kimola.com/reports/unveil-insights-with-our-justwatch-app-user-feedback-report-app-store-us-156984)
- [Spin The Wheel — Number Picker yorumları](https://justuseapp.com/en/app/6670243063/spin-the-wheel-number-picker/reviews) · [Wheel Spinner yorumları](https://marlvel.ai/apps/wheel-spinner-name-drawing-app/reviews) · [Random Movie Picker](https://appshunter.io/ios/app/6444808440) · [Spinflix](https://apps.apple.com/de/app/spinflix/id6741057161)
- [TV Time kapanışı — TechCrunch](https://techcrunch.com/2026/07/02/popular-tv-tracking-app-tv-time-is-shutting-down-as-company-focuses-on-ai) · [TV Time — Neowin](https://www.neowin.net/amp/tv-time-is-shutting-down-in-a-couple-of-weeks/) · [TV Time — AlternativeTo](https://alternativeto.net/software/tvshow-time/about) · [TV Time — Kimola](https://kimola.com/reports/unlock-insights-tv-time-app-user-feedback-analysis-app-store-fr-144807)
- [Ne izlesek (App Store TR)](https://apps.apple.com/tr/app/id6757267284) · [Gnovies](https://www.tamindir.com/indir/gnovies/) · [Nerede izlenir](https://apps.apple.com/us/app/id1556727192) · [What to Watch (Product Hunt)](https://www.producthunt.com/p/undefined/what-to-watch-ai-powered-movie-tv-recommendations-based-on-your-mood)


---

## Uygulama durumu (Kasım 2026, sürüm 1.1.0)

Bu analizdeki öneriler uygulandı: ortak oda kaldırıldı; sahte "Haftanın Trendleri" ve sahte örnek geçmiş
kaldırıldı; otomatik yedekleme kapatıldı (yerine kullanıcının kendi dosyasına yedek/geri yükleme geldi);
widget eylemleri dışa kapalı alıcıya taşındı; yalnızca HTTPS ağ yapılandırması eklendi; arama, akıllı öneri,
"Animasyonları azalt", tek paylaşım menüsü, dizi desteği ve haftalık uzaktan katalog eklendi; bağımlılıklar
güncellendi; yayın derlemesinde R8 açıldı; paket adı `com.ozkanakin.neizlesek` oldu (G7).
