# Ne İzlesek? — iOS / App Store Yol Haritası

_Hazırlanma: 20 Kasım 2026 • Android sürümü: 1.1.0_

## 1. Durum

Uygulama şu an **yalnızca Android** için yazılı: Kotlin + Jetpack Compose. iOS'ta doğrudan çalışmaz.
iOS uygulaması **sadece macOS + Xcode** ile derlenip imzalanabilir ve App Store'a yüklenebilir.
Bu proje bulut ortamı Linux olduğu için iOS derlemesi burada yapılamaz. Mac'iniz yoksa GitHub Actions'ın
macOS sunucuları (ücretli dakika) kullanılabilir.

## 2. Yaklaşım seçenekleri

| | **A) Compose Multiplatform (önerilen)** | **B) SwiftUI ile yeniden yazım** |
|---|---|---|
| Kod | Android kodunun çoğu (ekranlar, çark, filtreler, katalog, geçmiş) iOS'ta da kullanılır | iOS için ayrı ve tamamen yeni kod |
| Bakım | Tek kod: her özellik iki platforma birden gelir | Her özellik iki kez yazılır |
| Görünüm | Android ile birebir aynı tasarım | En "iPhone'a özgü" his |
| Süre (tahmini) | Altyapı taşıma 2–4 hafta, sonra ortak | 6–10 hafta, sonra çift bakım |
| Risk | Bazı Android'e özel parçalar (aşağıda) iOS karşılığıyla yeniden yazılır | Davranış farkları, çift test |

**Öneri: A.** Uygulama zaten Compose ile yazıldığı için ekranların çoğu Compose Multiplatform'a taşınabilir.

## 3. A seçeneğinde taşınacak parçalar

| Android'e özel parça | iOS karşılığı / çözüm |
|---|---|
| Room veritabanı (geçmiş, izlenenler, listeler) | Room 2.8 KMP destekler (iOS'ta SQLite) — tablo yapısı aynı kalır |
| SharedPreferences (tema, favoriler, ayarlar) | `multiplatform-settings` ya da DataStore KMP |
| Coil 2 (poster yükleme) | Coil 3 (KMP) |
| `org.json` (yedek dosyası, uzaktan katalog) | `kotlinx.serialization` |
| `HttpURLConnection` (haftalık katalog) | Ktor client (iOS'ta NSURLSession) |
| Titreşim, ses, sallayarak çevirme | `expect/actual`: iOS'ta UIImpactFeedbackGenerator, AVAudio, CoreMotion |
| Paylaşım, YouTube fragmanı, dosyaya yedek | iOS'ta UIActivityViewController, `openURL`, UIDocumentPicker |
| Ana ekran widget'ı | iOS'ta WidgetKit (SwiftUI ile ayrı küçük hedef) — ilk sürümde çıkarılabilir |
| Çökme raporu ekranı | iOS'ta Xcode Organizer / TestFlight çökme raporları yeterli |

Önerilen sıra: 1) `shared` modülüne model + katalog + filtre + çark mantığı, 2) veri katmanı (Room KMP,
ayarlar), 3) ekranlar, 4) platforma özel parçalar, 5) iOS kabuk projesi (Xcode), 6) TestFlight.

## 4. Sizin yapmanız gerekenler (hesap tarafı)

1. **Apple Developer Program** üyeliği (yıllık 99 USD) — açtığınızı belirttiniz ✓
2. App Store Connect → **Uygulamalar → +** → Yeni uygulama:
   - Platform: iOS • Ad: `Ne İzlesek?` • Birincil dil: Türkçe
   - **Bundle ID:** `com.ozkanakin.neizlesek` (Android ile aynı; Certificates, Identifiers & Profiles'tan oluşturulur)
   - SKU: `neizlesek-ios`
3. Bir **Mac** (ya da macOS CI) ve Xcode'un son sürümü.
4. Otomatik yükleme isterseniz: App Store Connect → Kullanıcılar ve Erişim → **Entegrasyonlar → App Store
   Connect API anahtarı** (Developer rolü). Anahtar dosyasını (.p8) **sohbete yapıştırmayın**; GitHub deposunda
   Settings → Secrets bölümüne eklenir.

## 5. App Store başvuru kontrol listesi

- [x] Uygulama ikonu 1024×1024, köşesiz, saydamlıksız: `ios/AppStore/AppIcon-1024.png`
- [x] Mağaza metinleri (ad, alt başlık, açıklama, anahtar kelimeler): `ios/AppStore/magaza-metni.md`
- [x] Gizlilik politikası (Türkçe + İngilizce): `PRIVACY.md` → URL olarak
      `https://github.com/ozkanakinnn-hash/Film-ark-/blob/main/PRIVACY.md` verilebilir
- [x] Gizlilik etiketi cevapları (aşağıda)
- [ ] Ekran görüntüleri: 6.9" (1320×2868) zorunlu; iPad desteklenirse 13" iPad — iOS sürümü hazır olunca alınır
- [ ] Destek URL'si (GitHub sayfası ya da basit bir web sayfası)
- [ ] TestFlight ile iç test, sonra incelemeye gönderim

### Gizlilik etiketi ("App Privacy") cevapları
- **Veri toplanıyor mu?** Hayır ("Data Not Collected"). Hesap yok, analiz/reklam SDK'sı yok; geçmiş, izlenenler,
  notlar cihazda kalır. Posterler TMDB'den, haftalık katalog GitHub'dan indirilir; kişisel veri gönderilmez.
- **Takip (ATT)?** Hayır — App Tracking Transparency izni gerekmez.
- **Şifreleme:** Yalnızca standart HTTPS → `Info.plist` içinde `ITSAppUsesNonExemptEncryption = NO`.

### Yaş sınırı
Uygulama içeriği film bilgisi; korku afişleri ve özetleri bulunduğu için anketin "Seyrek/hafif korku
temaları" sorusuna "Seyrek" cevabı verilmesi uygundur (beklenen derece 12+).

## 6. İnceleme riskleri ve önlemleri

| Kural | Risk | Önlem |
|---|---|---|
| 1.2 Kullanıcı içeriği | **Forum açıldığında** şikâyet etme, engelleme, içerik filtresi ve yasal iletişim zorunlu | Forum şu an sadece görünüm; altyapı kurulurken bu dört özellik birlikte eklenmeli |
| 5.2.1 Fikri mülkiyet | Platform adları ve film afişleri | Platformlar yalnızca yazıyla, logosuz; TMDB atfı uygulamada; marka notu mevcut |
| TMDB koşulları | Ticari kullanım | Ücretsiz/reklamsız kaldıkça uygun; reklam/ücretli özellik öncesi TMDB ticari lisansı |
| 4.2 Asgari işlev | "Web sitesinin kopyası" sayılmak | Çark, filtreler, geçmiş, düello gibi yerel özellikler yeterli |
| 2.1 Uygulama bütünlüğü | Çalışmayan forum düğmesi | Forum ekranında "Yakında açılıyor" açıkça yazıyor; sahte içerik yok |

## 7. Sonraki adım

Hangi yaklaşımla ilerleyeceğimize siz karar verin (A önerilir). A seçilirse ilk iş Android kodunu
bozmadan `shared` (ortak) modüle ayırmak olacak; her adımda Android testleri yeşil kalacak şekilde
ilerlenir. iOS derlemesi ve App Store'a yükleme için Mac'inizi bu oturuma bağlamanız ya da macOS
sunucusunda çalışan bir GitHub Actions işi kurmamız gerekir.
