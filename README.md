<p align="center">
  <img src="branding/readme-banner.png" alt="Ne İzlesek? — Film Karar Çarkı" width="100%">
</p>

<p align="center">
  <b>Ücretsiz · Reklamsız · Ticari olmayan</b><br>
  <b>Free · No ads · Non-commercial</b>
</p>

---

## 🇹🇷 Ne İzlesek? nedir?

**Ne İzlesek?**, "bu akşam ne izlesek?" tartışmasını saniyeler içinde bitiren bir Android uygulamasıdır.
Ruh halini ve izlediğin platformları seçersin, karar çarkı döner ve izleyeceğin film belli olur.

- 🎡 **Karar çarkı:** Filtrelere uyan filmler çarka dizilir; çark durduğunda filmin türüne özel sinematik bir açılış sahnesi oynar.
- 🎯 **Tek veto hakkı:** Çıkan filmi beğenmezsen bir kez veto edebilirsin; ikinci karar kesindir.
- ⚔️ **Düello modu:** İki film arasında art arda seçim yaparak şampiyonu belirlersin.
- 📋 **Özel listeler, geçmiş ve istatistikler:** Kendi çarklarını oluşturur, geçmiş kararlarını ve tür dağılımını görürsün.

### Ücretsiz ve ticari olmayan kullanım

Bu uygulama **tamamen ücretsizdir** ve **ticari amaç taşımaz**:

- Reklam **yoktur**.
- Uygulama içi satın alma, abonelik ya da ücretli sürüm **yoktur**.
- Bağış ya da başka bir gelir kaynağı **yoktur**.
- Kullanıcıdan hesap açması istenmez; kişisel veri toplanmaz. Tercihler ve geçmiş yalnızca cihazda saklanır.

Film posterleri ve film bilgileri için [TMDB](https://www.themoviedb.org/) API'si, TMDB'nin ticari olmayan kullanım koşullarına uygun şekilde ve atıfla kullanılır.

---

## 🇬🇧 About

**Ne İzlesek?** ("What should we watch?") is a free Android app that ends the nightly
"what should we watch?" debate in seconds. Users pick their mood and streaming platforms,
spin a decision wheel, and get a movie to watch.

### Free, non-commercial use

This app is **completely free** and **non-commercial**:

- **No** advertising.
- **No** in-app purchases, subscriptions or paid tiers.
- **No** donations or any other source of revenue.
- No user accounts and no personal data collection; preferences and history are stored on the device only.

Movie posters and metadata are retrieved from the [TMDB](https://www.themoviedb.org/) API under
TMDB's non-commercial terms, with attribution shown inside the app.

Gizlilik politikası / Privacy policy: [PRIVACY.md](PRIVACY.md)

---

## TMDB attribution

<a href="https://www.themoviedb.org/"><img src="https://www.themoviedb.org/assets/v4/logos/v2/blue_short-8e7b30f73a4020692ccca9c88bafe5dcb6f8a62a4c6bc55cd9ba82bb2cd95f6c.svg" alt="TMDB" height="16"></a>

This product uses the TMDB API but is not endorsed or certified by TMDB.

Netflix, Prime Video, Disney+, HBO Max, Mubi, Gain, TOD and Apple TV+ are trademarks of their respective owners.
They are referenced by name only to describe where a film is available; Ne İzlesek? is not affiliated with or
endorsed by them.
Uygulama içinde de Profil ekranındaki "Veri Kaynağı" bölümünde aynı atıf gösterilir.

---

## Geliştirici notları / Developer notes

Android (Kotlin, Jetpack Compose). Derleme ve test komutları ile mimari için [`CLAUDE.md`](CLAUDE.md) dosyasına bakın.

```bash
./gradlew :app:assembleDebug        # debug APK
./gradlew :app:testDebugUnitTest    # tests
```
