package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AudioLanguageOption
import com.example.data.model.Mood
import com.example.data.model.Movie
import com.example.data.model.OTHER_PLATFORM_NAME
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MovieRepository {

    private const val PREF_DAILY_FEED = "ne_izlesek_daily_feed_prefs"
    private const val KEY_LAST_SYNC_DATE = "last_daily_sync_date"
    private const val KEY_SYNC_COUNT = "daily_sync_counter"

    @Volatile
    private var prefs: SharedPreferences? = null

    private val baseMovies: List<Movie> = listOf(
        // --- BEYIN YORMAYAN / CEREZLIK (EASY_WATCH) ---
        Movie(
            id = "m1",
            title = "Bıçaklar Çekildi: Gizemli Bir Serüven",
            originalTitle = "Glass Onion: A Knives Out Mystery",
            year = 2022,
            durationMins = 139,
            imdb = 7.1,
            moodTag = Mood.EASY_WATCH.id,
            platforms = listOf("Netflix"),
            genre = "Gizem / Komedi",
            logline = "Teknoloji milyarderi arkadaş grubunu Yunan adasındaki lüks malikanesine davet eder, ancak oyun gerçeğe dönüşür.",
            whyWatch = "Rengarenk ada manzaraları, Daniel Craig'in şivesi ve sıfır beyin yorgunluğuyla patlamış mısırın dibini gördüren eğlence.",
            posterPath = "/vDGr1YdrlfbU9wxTOdpf3zChmv9.jpg",
            colorHex = 0xFF0284C7,
            isNewRelease = false
        ),
        Movie(
            id = "m2",
            title = "Ölümlü Dünya 2",
            originalTitle = "Ölümlü Dünya 2",
            year = 2023,
            durationMins = 116,
            imdb = 7.3,
            moodTag = Mood.EASY_WATCH.id,
            platforms = listOf("Netflix", "Gain"),
            genre = "Komedi / Aksiyon",
            logline = "Mermer ailesi kanun kaçağı hayatına devam ederken Zafer'in kaçırılmasıyla son bir büyük soygun için tekrar bir araya gelir.",
            whyWatch = "Servet Abi'nin tiratları ve absürt diyaloglarla çiftlerin asla sıkılmayacağı garanti Türk komedisi.",
            posterPath = "/z0T0oD10V2j9XG1h9cM2uY9p8A7.jpg",
            colorHex = 0xFFD97706,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = false
        ),
        Movie(
            id = "m3",
            title = "Hızlı ve Öfkeli 5: Rio Soygunu",
            originalTitle = "Fast Five",
            year = 2011,
            durationMins = 130,
            imdb = 7.3,
            moodTag = Mood.EASY_WATCH.id,
            platforms = listOf("Prime Video", "Netflix", "TOD"),
            genre = "Aksiyon / Macera",
            logline = "Dom ve Brian ekibi Rio'da 100 milyon dolarlık devasa bir kasa soygunu planlar.",
            whyWatch = "Fizik kurallarını kapıda bırakıp saf adrenalin ve hızla akşamı şenlendirmek isteyenlerin ilacı.",
            posterPath = "/7aZ182e0j3gV3LpUeO10j9Q6Zk.jpg",
            colorHex = 0xFFDC2626,
            isNewRelease = false
        ),

        // --- SOLUKSUZ GERILIM (THRILLER) ---
        Movie(
            id = "m4",
            title = "Kayıp Kız",
            originalTitle = "Gone Girl",
            year = 2014,
            durationMins = 149,
            imdb = 8.1,
            moodTag = Mood.THRILLER.id,
            platforms = listOf("Disney+", "Prime Video"),
            genre = "Psikolojik Gerilim",
            logline = "Evliliklerinin beşinci yıl dönümünde eşi esrarengiz şekilde kaybolan Nick, bir anda baş şüpheli haline gelir.",
            whyWatch = "İlişkinizi sorgulatacak derecede zekice kurgulanmış David Fincher başyapıtı. Nefesinizi tutarak izleyeceksiniz.",
            posterPath = "/qymaJhucquUwjpVI3xs07ne0whz.jpg",
            colorHex = 0xFF475569,
            isNewRelease = false
        ),
        Movie(
            id = "m5",
            title = "Zindan Adası",
            originalTitle = "Shutter Island",
            year = 2010,
            durationMins = 138,
            imdb = 8.2,
            moodTag = Mood.THRILLER.id,
            platforms = listOf("Netflix", "BluTV"),
            genre = "Gizem / Gerilim",
            logline = "İki polis memuru, akıl hastası suçluların tutulduğu izole bir adadaki hastaneden kaçan bir kadını araştırır.",
            whyWatch = "Son sahnesine kadar aklınızı kurcalayacak ve bittiğinde yarım saat tartıştıracak Leonardo DiCaprio efsanesi.",
            posterPath = "/4BgSWFj5qgC8nK0ZqR3W3Y9.jpg",
            colorHex = 0xFF1E293B,
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m6",
            title = "Sessiz Bir Yer",
            originalTitle = "A Quiet Place",
            year = 2018,
            durationMins = 90,
            imdb = 7.5,
            moodTag = Mood.THRILLER.id,
            platforms = listOf("Prime Video", "Netflix"),
            genre = "Gerilim / Korku",
            logline = "En ufak sese duyarlı canavarlarla dolu bir dünyada dört kişilik bir aile hayatta kalmaya çalışır.",
            whyWatch = "Film boyunca evde çıt çıkaramayacaksınız; 90 dakikalık saf tansiyon ve harika bir gerilim dozu.",
            posterPath = "/nAU74GmpUk7t5iklEp3bufwDq4n.jpg",
            colorHex = 0xFF7F1D1D,
            isNewRelease = false
        ),

        // --- GOZLERDEN YAS GETIREN KOMEDI (COMEDY) ---
        Movie(
            id = "m7",
            title = "Felekten Bir Gece",
            originalTitle = "The Hangover",
            year = 2009,
            durationMins = 100,
            imdb = 7.7,
            moodTag = Mood.COMEDY.id,
            platforms = listOf("Netflix", "Prime Video", "BluTV"),
            genre = "Komedi",
            logline = "Las Vegas'ta bekarlığa veda partisi veren üç arkadaş, ertesi sabah damat kayıpken otel odasında kaplanla uyanır.",
            whyWatch = "Dünyanın en kaotik sabahı; gülmekten karın kaslarınızı ağrıtacak eskimeyen kült komedi.",
            posterPath = "/ulMsXr5zWqW1z1YqQ7C2Tq9jG9.jpg",
            colorHex = 0xFFEAB308,
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m8",
            title = "Pardon",
            originalTitle = "Pardon",
            year = 2005,
            durationMins = 94,
            imdb = 8.2,
            moodTag = Mood.COMEDY.id,
            platforms = listOf("BluTV", "Gain"),
            genre = "Kara Komedi",
            logline = "Yanlış anlaşılma sonucu cezaevine düşen üç masum arkadaşın trajikomik bürokrasi mücadelesi.",
            whyWatch = "Ferhan Şensoy ve Rasim Öztekin'in her repliği ezberlenen Türk sineması başyapıtı.",
            posterPath = "/p1a7uV6q8w1T9z3e8k5.jpg",
            colorHex = 0xFFB45309,
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m9",
            title = "Büyük Budapeşte Oteli",
            originalTitle = "The Grand Budapest Hotel",
            year = 2014,
            durationMins = 99,
            imdb = 8.1,
            moodTag = Mood.COMEDY.id,
            platforms = listOf("Disney+", "Mubi"),
            genre = "Macera / Komedi",
            logline = "Efsanevi otel konsiyerji Gustave H ve genç komisi Zero'nun paha biçilmez bir tablonun çalınmasıyla başlayan serüveni.",
            whyWatch = "Wes Anderson'ın pastel renk paleti, enfes simetrisi ve çılgın temposuyla tam bir görsel şölen.",
            posterPath = "/eWdyYQreja6JGCzqHWX9ne8mN45.jpg",
            colorHex = 0xFFBE185D,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id),
            isNewRelease = false
        ),

        // --- ASK & ROMANTIZM (ROMANCE) ---
        Movie(
            id = "m10",
            title = "Zamanda Aşk",
            originalTitle = "About Time",
            year = 2013,
            durationMins = 123,
            imdb = 7.8,
            moodTag = Mood.ROMANCE.id,
            platforms = listOf("Prime Video", "Netflix"),
            genre = "Romantik / Bilim Kurgu",
            logline = "21 yaşına geldiğinde zamanda yolculuk yapabildiğini öğrenen Tim, hayatının aşkını bulmaya ve anı güzelleştirmeye çalışır.",
            whyWatch = "Sadece bir aşk filmi değil, hayattaki her saniyenin değerini hissettiren sıcacık bir başyapıt.",
            posterPath = "/iHrOX0x04kG9iL41F8.jpg",
            colorHex = 0xFFF43F5E,
            isNewRelease = false
        ),
        Movie(
            id = "m11",
            title = "La La Land",
            originalTitle = "La La Land",
            year = 2016,
            durationMins = 128,
            imdb = 8.0,
            moodTag = Mood.ROMANCE.id,
            platforms = listOf("Netflix", "TOD"),
            genre = "Müzikal / Romantik",
            logline = "Los Angeles'ta yolları kesişen tutkulu caz piyanisti Sebastian ile oyuncu adayı Mia'nın rüya gibi aşkı.",
            whyWatch = "Emma Stone ve Ryan Gosling'in kimyası, büyüleyici müzikleri ve unutulmaz finaliyle kalbinize dokunacak.",
            posterPath = "/uDO8zWDhfWNz0g0VjVqV0Z0K0L.jpg",
            colorHex = 0xFF6366F1,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id, AudioLanguageOption.DUBBED.id),
            isNewRelease = false
        ),
        Movie(
            id = "m12",
            title = "Gün Doğmadan",
            originalTitle = "Before Sunrise",
            year = 1995,
            durationMins = 101,
            imdb = 8.1,
            moodTag = Mood.ROMANCE.id,
            platforms = listOf("BluTV", "Apple TV+", "Mubi"),
            genre = "Romantik / Dram",
            logline = "Viyana'ya giden trende tanışan Jesse ve Celine, ertesi sabaha kadar şehri gezerek derin sohbetlere dalar.",
            whyWatch = "İki insanın konuşmasının bile büyüleyici olabileceğini kanıtlayan sinema tarihinin en samimi aşk filmi.",
            posterPath = "/kf1J2a8d3vF8gL01.jpg",
            colorHex = 0xFFEC4899,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id),
            isCultOrMostWatched = true,
            isNewRelease = false
        ),

        // --- AKIL OYUNLARI / ZIHIN BUKUCU (MIND_BENDER) ---
        Movie(
            id = "m13",
            title = "Yıldızlararası",
            originalTitle = "Interstellar",
            year = 2014,
            durationMins = 169,
            imdb = 8.7,
            moodTag = Mood.MIND_BENDER.id,
            platforms = listOf("Netflix", "Prime Video", "TOD"),
            genre = "Bilim Kurgu / Macera",
            logline = "Yok olmanın eşiğindeki insanlık için yaşanabilir yeni bir gezegen arayan bir grup astronot solucan deliğinden geçer.",
            whyWatch = "Hans Zimmer'ın iliklere işleyen müzikleri ve kara delik teorileriyle vizyonunuzu ikiye katlayacak dev yapım.",
            posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
            colorHex = 0xFF0EA5E9,
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m14",
            title = "Başlangıç",
            originalTitle = "Inception",
            year = 2010,
            durationMins = 148,
            imdb = 8.8,
            moodTag = Mood.MIND_BENDER.id,
            platforms = listOf("Netflix", "Prime Video", "BluTV"),
            genre = "Bilim Kurgu / Aksiyon",
            logline = "Rüyaların içinde bilgi çalan yetenekli bir hırsıza, bir insanın zihnine fikir ekleme (inception) görevi verilir.",
            whyWatch = "Fırıldak dönmeye devam ediyor mu etmiyor mu tartışmasını yeniden alevlendirecek modern klasik.",
            posterPath = "/edv5CZvWj09upOsy2Y6IwDhK8bt.jpg",
            colorHex = 0xFF14B8A6,
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m15",
            title = "Prestij",
            originalTitle = "The Prestige",
            year = 2006,
            durationMins = 130,
            imdb = 8.5,
            moodTag = Mood.MIND_BENDER.id,
            platforms = listOf("Prime Video", "Apple TV+"),
            genre = "Gizem / Dram",
            logline = "19. yüzyıl Londra'sında iki rakip illüzyonist, en iyi ışınlanma numarasını yaratmak için takıntılı bir savaşa girer.",
            whyWatch = "Gözünüzü ekrandan bir saniye bile ayıramayacağınız, kusursuz ters köşelerle dolu bir akıl düellosu.",
            posterPath = "/tRNlZbgNCNOpL9Nk9oc9I2Um9.jpg",
            colorHex = 0xFF6D28D9,
            isCultOrMostWatched = true,
            isNewRelease = false
        ),

        // --- KULT & ODULLU SINEMA (CULT_AWARD) ---
        Movie(
            id = "m16",
            title = "Parazit",
            originalTitle = "Parasite",
            year = 2019,
            durationMins = 132,
            imdb = 8.5,
            moodTag = Mood.CULT_AWARD.id,
            platforms = listOf("Mubi", "BluTV"),
            genre = "Dram / Gerilim",
            logline = "Yoksul Kim ailesi fertleri, zengin Park ailesinin evine tek tek sızarak çalışan olarak işe girer.",
            whyWatch = "Oscar tarihini baştan yazan, her sahnesi dahiyane detaylarla örülü çağımızın en iyi filmlerinden biri.",
            posterPath = "/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg",
            colorHex = 0xFF047857,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id),
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m17",
            title = "Bir Zamanlar Anadolu'da",
            originalTitle = "Bir Zamanlar Anadolu'da",
            year = 2011,
            durationMins = 157,
            imdb = 7.8,
            moodTag = Mood.CULT_AWARD.id,
            platforms = listOf("Mubi", "Netflix"),
            genre = "Dram / Suç",
            logline = "Bozkırın karanlığında bir doktor, savcı ve komiser, bir cinayet zanlısının gösterdiği cesedi arar.",
            whyWatch = "Nuri Bilge Ceylan'ın Cannes ödüllü, insan ruhunun derinliklerine ayna tutan sinematik şaheseri.",
            posterPath = "/kZ0q8J9V9j9X1Y8Z7.jpg",
            colorHex = 0xFF854D0E,
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m18",
            title = "Whiplash",
            originalTitle = "Whiplash",
            year = 2014,
            durationMins = 106,
            imdb = 8.5,
            moodTag = Mood.CULT_AWARD.id,
            platforms = listOf("Netflix", "Prime Video"),
            genre = "Dram / Müzik",
            logline = "Genç ve hırslı bir baterist, mükemmeliyetçi ve acımasız orkestra şefi Fletcher'ın sınırlarını zorlar.",
            whyWatch = "Son 15 dakikasında yerinizde duramayacağınız, adrenalin pompalayan inanılmaz bir oyunculuk gösterisi.",
            posterPath = "/7fn624j5lj3xTwhPa29qv2O9.jpg",
            colorHex = 0xFFB91C1C,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id, AudioLanguageOption.DUBBED.id),
            isNewRelease = false
        ),

        // --- KORKU / ADRENALIN (HORROR) ---
        Movie(
            id = "m19",
            title = "Korku Seansı",
            originalTitle = "The Conjuring",
            year = 2013,
            durationMins = 112,
            imdb = 7.5,
            moodTag = Mood.HORROR.id,
            platforms = listOf("Netflix", "Prime Video", "BluTV"),
            genre = "Korku / Gizem",
            logline = "Paranormal araştırmacılar Ed ve Lorraine Warren, izole bir çiftlik evinde dehşete düşen ailenin yardımına koşar.",
            whyWatch = "Klişe zıplatmalardan uzak, atmosferiyle iliklerinize kadar ürperten modern korku klasiği.",
            posterPath = "/wVYREutTvI2COPuCm0Yng7IlR.jpg",
            colorHex = 0xFF450A0A,
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m20",
            title = "Ayin",
            originalTitle = "Hereditary",
            year = 2018,
            durationMins = 127,
            imdb = 7.3,
            moodTag = Mood.HORROR.id,
            platforms = listOf("BluTV", "Apple TV+", "Mubi"),
            genre = "Korku / Gizem",
            logline = "Ailenin yaşlı annesi öldükten sonra, kızı ve torunları soylarındaki karanlık ve korkunç sırlarla yüzleşir.",
            whyWatch = "Işıkları açtıktan sonra bile etkisinden çıkamayacağınız, psikolojik dozu tavan bir kabus.",
            posterPath = "/p9fomJy2w414j0Qv.jpg",
            colorHex = 0xFF18181B,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id),
            isNewRelease = false
        ),

        // --- ANIMASYON / MASALSI (ANIMATION) ---
        Movie(
            id = "m21",
            title = "Ruhların Kaçışı",
            originalTitle = "Spirited Away",
            year = 2001,
            durationMins = 125,
            imdb = 8.6,
            moodTag = Mood.ANIMATION.id,
            platforms = listOf("Netflix"),
            genre = "Animasyon / Fantastik",
            logline = "Anne ve babası hayvana dönüşen 10 yaşındaki Chihiro, ruhların dinlendiği gizemli bir hamamda çalışmaya başlar.",
            whyWatch = "Hayao Miyazaki'nin Oscar ödüllü, izleyen her yetişkini çocukluğuna ve masallar diyarına götüren başyapıtı.",
            posterPath = "/393D227v0z7mU99.jpg",
            colorHex = 0xFF059669,
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m22",
            title = "Örümcek-Adam: Örümcek Evreninde",
            originalTitle = "Spider-Man: Into the Spider-Verse",
            year = 2018,
            durationMins = 117,
            imdb = 8.4,
            moodTag = Mood.ANIMATION.id,
            platforms = listOf("Disney+", "Netflix"),
            genre = "Animasyon / Aksiyon",
            logline = "Genç Miles Morales yeni örümcek güçlerini keşfederken diğer evrenlerden gelen alternatif örümceklerle tanışır.",
            whyWatch = "Görsel stili, dinamik müzikleri ve temposuyla animasyon sinemasında çığır açan bir enerji patlaması.",
            posterPath = "/iiZZdoQBEYBv6id8su7ImL0oCbD.jpg",
            colorHex = 0xFFE11D48,
            isNewRelease = false
        ),
        Movie(
            id = "m23",
            title = "Coco",
            originalTitle = "Coco",
            year = 2017,
            durationMins = 105,
            imdb = 8.4,
            moodTag = Mood.ANIMATION.id,
            platforms = listOf("Disney+"),
            genre = "Animasyon / Aile",
            logline = "Müzisyen olma hayali kuran küçük Miguel, kendisini Ölüler Diyarı'nda atalarının sırlarını çözerken bulur.",
            whyWatch = "Müzikleriyle gözlerinizden yaş getirecek, aile bağlarını sıcacık anlatan Pixar mucizesi.",
            posterPath = "/gGEsBPAijhVUFoiNpgZXq.jpg",
            colorHex = 0xFFEA580C,
            isNewRelease = false
        ),

        // =========================================================================
        // YENİ KORKU FİLMLERİ (2024–2026) — TÜM PLATFORMLARDA ZENGİN HAVUZ
        // =========================================================================
        Movie(
            id = "m24",
            title = "Günahkarlar",
            originalTitle = "Sinners",
            year = 2025,
            durationMins = 137,
            imdb = 8.0,
            moodTag = Mood.HORROR.id,
            platforms = listOf("BluTV", "TOD", "Apple TV+"),
            genre = "Korku / Gerilim",
            logline = "Geçmişlerini geride bırakmak için memleketlerine dönen ikiz kardeşler, karanlık ve kadim bir kötülükle karşılaşır.",
            whyWatch = "Ryan Coogler imzalı, müzik ve doğaüstü korkuyu harmanlayan 2025'in en çok konuşulan yapımı.",
            posterPath = "/jYfMTSiFFK7ffbY2lay4zyvTkEk.jpg",
            colorHex = 0xFF991B1B,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m25",
            title = "Cevher",
            originalTitle = "The Substance",
            year = 2024,
            durationMins = 141,
            imdb = 7.3,
            moodTag = Mood.HORROR.id,
            platforms = listOf("Mubi", "Prime Video"),
            genre = "Korku / Bilim Kurgu",
            logline = "Parlak günlerini geride bırakan bir yıldız, kendisinin daha genç bir versiyonunu yaratan gizemli bir karaborsa ilacı dener.",
            whyWatch = "Demi Moore'un cesur performansıyla Cannes'dan ödülle dönen çarpıcı modern body-horror şaheseri.",
            posterPath = "/lqoMzCcZYEFK729d6qzt349fB4o.jpg",
            colorHex = 0xFFBE123C,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m26",
            title = "Nosferatu",
            originalTitle = "Nosferatu",
            year = 2024,
            durationMins = 132,
            imdb = 7.4,
            moodTag = Mood.HORROR.id,
            platforms = listOf("Prime Video", "TOD", "Apple TV+"),
            genre = "Korku / Gizem",
            logline = "19. yüzyıl Almanya'sında genç bir kadın ile ona saplantılı hale gelen kadim bir Transilvanya vampiri arasındaki gotik kabus.",
            whyWatch = "Robert Eggers'ın büyüleyici gotik atmosferi ve karanlık sinematografisiyle tüyler ürperten yeni nesil korku.",
            posterPath = "/5qGIxdEO841C0tdY8vOdLoRVrr0.jpg",
            colorHex = 0xFF31102F,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m27",
            title = "28 Yıl Sonra",
            originalTitle = "28 Years Later",
            year = 2025,
            durationMins = 115,
            imdb = 7.5,
            moodTag = Mood.HORROR.id,
            platforms = listOf("Netflix", "Apple TV+"),
            genre = "Korku / Aksiyon",
            logline = "Öfke virüsünün patlak vermesinden yıllar sonra izole bir adada hayatta kalanların anakaradaki dehşetle yüzleşmesi.",
            whyWatch = "Danny Boyle ve Alex Garland ikilisinin soluksuz gerilim ve hayatta kalma korkusuyla dönüşü.",
            posterPath = "/mIg1qCkVxnAlM2TK3RUF0tdEXlE.jpg",
            colorHex = 0xFF7F1D1D,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m28",
            title = "Alien: Romulus",
            originalTitle = "Alien: Romulus",
            year = 2024,
            durationMins = 119,
            imdb = 7.1,
            moodTag = Mood.HORROR.id,
            platforms = listOf("Disney+", "Netflix"),
            genre = "Korku / Bilim Kurgu",
            logline = "Terk edilmiş bir uzay istasyonunu yağmalayan genç koloniciler, evrendeki en korkunç yaşam formuyla burun buruna gelir.",
            whyWatch = "Klasik uzay korkusu gerilimini yeni nesil klostrofobik bir tempoyla yeniden canlandırıyor.",
            posterPath = "/b33nnKl1GSFbao4l3fZDDqsMx0F.jpg",
            colorHex = 0xFF14532D,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m29",
            title = "Longlegs",
            originalTitle = "Longlegs",
            year = 2024,
            durationMins = 101,
            imdb = 6.7,
            moodTag = Mood.HORROR.id,
            platforms = listOf("Prime Video", "Gain", "Mubi"),
            genre = "Korku / Suç",
            logline = "Genç bir FBI ajanı, okült izler bırakan esrarengiz bir seri katilin çözülmemiş davasını devralır.",
            whyWatch = "Nicolas Cage'in ürkütücü dönüşümüyle her sahnesi tedirginlik yaratan modern dedektif korkusu.",
            posterPath = "/5aj8vVGFwGVbQQs26ywhg4Zxk2L.jpg",
            colorHex = 0xFF4C0519,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id, AudioLanguageOption.DUBBED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m30",
            title = "Tuzak",
            originalTitle = "Trap",
            year = 2024,
            durationMins = 105,
            imdb = 6.1,
            moodTag = Mood.HORROR.id,
            platforms = listOf("BluTV", "Netflix"),
            genre = "Korku / Gerilim",
            logline = "Kızıyla pop konserine giden baba, stadyumun kendisini yakalamak için kurulmuş dev bir polis tuzağı olduğunu fark eder.",
            whyWatch = "M. Night Shyamalan'dan konser atmosferinde geçen akıl oyunlu gerilim-korku.",
            posterPath = "/jwoaKYVqPgYemFztktL92zZ6k5z.jpg",
            colorHex = 0xFF701A75,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m31",
            title = "İlk Alamet",
            originalTitle = "The First Omen",
            year = 2024,
            durationMins = 119,
            imdb = 6.5,
            moodTag = Mood.HORROR.id,
            platforms = listOf("Disney+", "Gain"),
            genre = "Korku",
            logline = "Roma'ya gönderilen genç Amerikalı bir rahibe adayı, kilisenin içindeki korkunç bir komployla yüzleşir.",
            whyWatch = "Klasik doğaüstü korku sevenler için 70'ler sinematografisine saygı duruşu niteliğinde ürpertici bir yapım.",
            posterPath = "/uGyiewQnDHPuiHN9V4k2t9QBPnh.jpg",
            colorHex = 0xFF881337,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id, AudioLanguageOption.DUBBED.id),
            isNewRelease = true
        ),

        // =========================================================================
        // YENİ ÇIKANLAR & GÜNCEL PLATFORM HİTLERİ (2024–2026) — YENİLER ÖNCELİKLİ
        // =========================================================================
        Movie(
            id = "m32",
            title = "Kurtuluş Projesi",
            originalTitle = "Project Hail Mary",
            year = 2026,
            durationMins = 148,
            imdb = 8.4,
            moodTag = Mood.MIND_BENDER.id,
            platforms = listOf("Prime Video", "Apple TV+"),
            genre = "Bilim Kurgu / Macera",
            logline = "Güneş'in sönmesini engellemek için tek başına derin uzaya gönderilen bilim insanı, hafızası yerine geldikçe imkansız görevi hatırlar.",
            whyWatch = "Ryan Gosling başrolünde, zeka ve duygusallığı birleştiren 2026'nın en büyük bilim kurgu olayı.",
            posterPath = "",
            colorHex = 0xFF0284C7,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m33",
            title = "The Rip",
            originalTitle = "The Rip",
            year = 2026,
            durationMins = 122,
            imdb = 7.5,
            moodTag = Mood.THRILLER.id,
            platforms = listOf("Netflix"),
            genre = "Gerilim / Suç",
            logline = "Miami narkotik ekibi terk edilmiş bir evde milyonlarca dolar nakit bulunca, ekip içindeki sadakat sınanmaya başlar.",
            whyWatch = "Matt Damon ve Ben Affleck'i gerilim dolu bir suç hikayesinde yeniden buluşturan yeni Netflix hiti.",
            posterPath = "",
            colorHex = 0xFF334155,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m34",
            title = "Dune: Çöl Gezegeni Bölüm İki",
            originalTitle = "Dune: Part Two",
            year = 2024,
            durationMins = 166,
            imdb = 8.5,
            moodTag = Mood.MIND_BENDER.id,
            platforms = listOf("BluTV", "TOD", "Apple TV+"),
            genre = "Bilim Kurgu / Macera",
            logline = "Paul Atreides, Chani ve Fremenler ile birleşerek ailesini yok edenlere karşı destansı bir intikam savaşına hazırlanır.",
            whyWatch = "Dev ekran atmosferi, Hans Zimmer müzikleri ve nefes kesen görselliğiyle modern sinema efsanesi.",
            posterPath = "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
            colorHex = 0xFFD97706,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isCultOrMostWatched = true,
            isNewRelease = true
        ),
        Movie(
            id = "m35",
            title = "Mickey 17",
            originalTitle = "Mickey 17",
            year = 2025,
            durationMins = 137,
            imdb = 7.3,
            moodTag = Mood.MIND_BENDER.id,
            platforms = listOf("BluTV", "Prime Video"),
            genre = "Bilim Kurgu / Komedi",
            logline = "Buz gezegeni kolonisinde tehlikeli görevler için klonlanan Mickey, aynı anda iki kopyasının yaşadığını fark eder.",
            whyWatch = "Bong Joon Ho ve Robert Pattinson'dan zeki, eğlenceli ve sistem eleştirisi yüklü bilim kurgu.",
            posterPath = "/edKpE9B5qN3e559OuMCZdW1I1vF.jpg",
            colorHex = 0xFF0D9488,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m36",
            title = "Deadpool & Wolverine",
            originalTitle = "Deadpool & Wolverine",
            year = 2024,
            durationMins = 128,
            imdb = 7.6,
            moodTag = Mood.EASY_WATCH.id,
            platforms = listOf("Disney+"),
            genre = "Aksiyon / Komedi",
            logline = "Evreni kurtarmak zorunda kalan geveze kahraman Deadpool, gönülsüz Wolverine'i ikna ederek kaotik bir yolculuğa çıkar.",
            whyWatch = "Yüksek tempo, keskin mizah ve kesintisiz aksiyon arayanlar için birebir.",
            posterPath = "/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
            colorHex = 0xFFDC2626,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m37",
            title = "Hoppers",
            originalTitle = "Hoppers",
            year = 2026,
            durationMins = 104,
            imdb = 7.8,
            moodTag = Mood.ANIMATION.id,
            platforms = listOf("Disney+"),
            genre = "Animasyon / Aile",
            logline = "Bilincini robot bir kunduza aktaran genç Mabel, hayvanlar dünyasının gizli dilini çözerek doğayı korumaya çalışır.",
            whyWatch = "Pixar'ın 2026 tarihli hem kahkaha attıran hem duygulandıran yepyeni aile animasyonu.",
            posterPath = "",
            colorHex = 0xFF059669,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m38",
            title = "Ters Yüz 2",
            originalTitle = "Inside Out 2",
            year = 2024,
            durationMins = 96,
            imdb = 7.6,
            moodTag = Mood.ANIMATION.id,
            platforms = listOf("Disney+"),
            genre = "Animasyon / Aile",
            logline = "Ergenliğe adım atan Riley'nin zihin merkezine Kaygı ve Gıpta gibi yepyeni duygular taşınır.",
            whyWatch = "Her yaştan izleyicinin kendinden bir parça bulduğu rekor kıran Pixar macerası.",
            posterPath = "/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg",
            colorHex = 0xFFEA580C,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m39",
            title = "Vahşi Robot",
            originalTitle = "The Wild Robot",
            year = 2024,
            durationMins = 102,
            imdb = 8.2,
            moodTag = Mood.ANIMATION.id,
            platforms = listOf("Prime Video", "TOD", "Apple TV+"),
            genre = "Animasyon / Macera",
            logline = "Issız bir adaya düşen akıllı robot Roz, vahşi doğaya uyum sağlarken yetim bir kaz yavrusuna annelik yapmaya başlar.",
            whyWatch = "Görsel olarak nefes kesen, sıcacık hikayesiyle son yılların en sevilen animasyon başyapıtı.",
            posterPath = "/wTnV3PCVW5O92JMrFvvrRcV39RU.jpg",
            colorHex = 0xFF10B981,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m40",
            title = "Tetikçi",
            originalTitle = "Hit Man",
            year = 2024,
            durationMins = 115,
            imdb = 6.9,
            moodTag = Mood.COMEDY.id,
            platforms = listOf("Netflix", "Gain"),
            genre = "Komedi / Suç",
            logline = "Polis için sahte kiralık katil kılığına giren üniversite hocası, müşterilerden birine aşık olunca işler karışır.",
            whyWatch = "Glen Powell'ın karizması ve zekice yazılmış diyaloglarıyla zahmetsiz keyif sunan suç komedisi.",
            posterPath = "/5B22eed7ErxYiY729da7x8rR499.jpg",
            colorHex = 0xFFD97706,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m41",
            title = "Kurtlar",
            originalTitle = "Wolfs",
            year = 2024,
            durationMins = 108,
            imdb = 6.6,
            moodTag = Mood.EASY_WATCH.id,
            platforms = listOf("Apple TV+"),
            genre = "Aksiyon / Komedi",
            logline = "Birbirinden nefret eden iki profesyonel temizlikçi, aynı yüksek profilli suçu örtbas etmek için bir gece birlikte çalışmak zorunda kalır.",
            whyWatch = "George Clooney ve Brad Pitt'in atışmalarıyla hafif ama tempolu akşam eğlencesi.",
            posterPath = "/vOX1Zng472PC2KnS0B9nRfM8aaZ.jpg",
            colorHex = 0xFF475569,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m42",
            title = "F1",
            originalTitle = "F1",
            year = 2025,
            durationMins = 155,
            imdb = 7.9,
            moodTag = Mood.EASY_WATCH.id,
            platforms = listOf("Apple TV+"),
            genre = "Aksiyon / Macera",
            logline = "Emekli olmuş eski bir Formula 1 pilotu, çaylak bir yeteneğe akıl hocalığı yapmak üzere pistlere görkemli bir dönüş yapar.",
            whyWatch = "Brad Pitt ve Top Gun: Maverick yönetmeninden gerçek yarış pistlerinde çekilmiş saf hız şöleni.",
            posterPath = "",
            colorHex = 0xFFEF4444,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m43",
            title = "Sonsuzluk",
            originalTitle = "Eternity",
            year = 2026,
            durationMins = 114,
            imdb = 7.6,
            moodTag = Mood.ROMANCE.id,
            platforms = listOf("Apple TV+", "Netflix"),
            genre = "Romantik / Komedi",
            logline = "Öbür dünyada sonsuzluğu kiminle geçireceğine karar vermek zorunda kalan Joan, eski aşkı ve hayat arkadaşı arasında kalır.",
            whyWatch = "2026'nın hem kalbe dokunan hem güldüren en taze romantik komedi seçeneklerinden.",
            posterPath = "",
            colorHex = 0xFFEC4899,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id, AudioLanguageOption.DUBBED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m44",
            title = "Anora",
            originalTitle = "Anora",
            year = 2024,
            durationMins = 139,
            imdb = 7.7,
            moodTag = Mood.CULT_AWARD.id,
            platforms = listOf("Mubi", "Gain", "TOD"),
            genre = "Dram / Komedi",
            logline = "Genç Anora'nın bir oligark varisiyle yaptığı ani evlilik, ailenin New York'a gelmesiyle kaotik bir kovalamacaya dönüşür.",
            whyWatch = "Oscar En İyi Film ve Altın Palmiye ödüllü, temposu bir an bile düşmeyen modern klasik.",
            posterPath = "/qh8m8Ugz0sM5D0d1Q1xG9Z2qP1V.jpg",
            colorHex = 0xFFE11D48,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m45",
            title = "Konsey",
            originalTitle = "Conclave",
            year = 2024,
            durationMins = 120,
            imdb = 7.4,
            moodTag = Mood.THRILLER.id,
            platforms = listOf("Prime Video", "TOD", "BluTV"),
            genre = "Gizem / Gerilim",
            logline = "Yeni papanın seçilmesi için kapalı kapılar ardında yürütülen gizli oturumda şok edici sırlar açığa çıkar.",
            whyWatch = "Ralph Fiennes'ın muazzam oyunculuğuyla politika ve gizemi ustaca harmanlayan ödüllü gerilim.",
            posterPath = "/v3P3v2h5z9W4k5d7W2X1G3M4.jpg",
            colorHex = 0xFF7C3AED,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m46",
            title = "Yan Yana",
            originalTitle = "Yan Yana",
            year = 2025,
            durationMins = 118,
            imdb = 7.7,
            moodTag = Mood.COMEDY.id,
            platforms = listOf("Netflix", "Gain"),
            genre = "Komedi / Dram",
            logline = "İki bambaşka dünyadan gelen yol arkadaşının sıcak dostluk, kahkaha ve hayat muhasebesiyle örülü hikayesi.",
            whyWatch = "Haluk Bilginer ve Feyyaz Yiğit uyumuyla hem güldüren hem iç ısıtan güncel yerli hit.",
            posterPath = "",
            colorHex = 0xFFF59E0B,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id),
            isNewRelease = true
        ),
        Movie(
            id = "m47",
            title = "Vadi",
            originalTitle = "The Gorge",
            year = 2025,
            durationMins = 127,
            imdb = 7.0,
            moodTag = Mood.ROMANCE.id,
            platforms = listOf("Apple TV+", "Prime Video"),
            genre = "Romantik / Aksiyon",
            logline = "Gizemli bir uçurumun iki yakasındaki gözetleme kulelerinde görev yapan iki elit keskin nişancı mesafelere rağmen bağ kurar.",
            whyWatch = "Anya Taylor-Joy ve Miles Teller'la romantizm ve bilim kurgu gerilimini aynı potada eritiyor.",
            posterPath = "/7iMBZzVZtG0oBug4TfqDb9ZxAOa.jpg",
            colorHex = 0xFF0EA5E9,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isNewRelease = true
        ),

        // =========================================================================
        // DİĞER KATEGORİSİ — PLATFORMLARDA OLMAYAN KÜLT & ÇOK İZLENEN FİLMLER
        // =========================================================================
        Movie(
            id = "m48",
            title = "Baba",
            originalTitle = "The Godfather",
            year = 1972,
            durationMins = 175,
            imdb = 9.2,
            moodTag = Mood.CULT_AWARD.id,
            platforms = listOf(OTHER_PLATFORM_NAME),
            genre = "Suç / Dram",
            logline = "Corleone suç ailesinin yaşlanan patriği, yeraltı imparatorluğunun kontrolünü en küçük oğlu Michael'a devreder.",
            whyWatch = "Sinema tarihinin tartışmasız en büyük başyapıtlarından; her izleyişte yeni bir detay yakalatır.",
            posterPath = "/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
            colorHex = 0xFF78350F,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m49",
            title = "Esaretin Bedeli",
            originalTitle = "The Shawshank Redemption",
            year = 1994,
            durationMins = 142,
            imdb = 9.3,
            moodTag = Mood.CULT_AWARD.id,
            platforms = listOf(OTHER_PLATFORM_NAME),
            genre = "Dram",
            logline = "İşlemediği bir suçtan ömür boyu hapis cezası alan bankacı Andy Dufresne'in Shawshank'taki umut ve dostluk öyküsü.",
            whyWatch = "IMDb'de tüm zamanların 1 numarası; umudu ve sabrı en derinden hissettiren kült film.",
            posterPath = "/9cqNxx0GxF0bflZmeSMuL5tnGzr.jpg",
            colorHex = 0xFF475569,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m50",
            title = "Kara Şövalye",
            originalTitle = "The Dark Knight",
            year = 2008,
            durationMins = 152,
            imdb = 9.0,
            moodTag = Mood.THRILLER.id,
            platforms = listOf(OTHER_PLATFORM_NAME),
            genre = "Aksiyon / Suç",
            logline = "Batman, Gotham City'yi kaosa sürükleyen dahi suçlu Joker ile ahlaki sınırlarını zorlayan bir savaşa girer.",
            whyWatch = "Heath Ledger'ın efsanevi Joker performansıyla süper kahraman sinemasının zirvesi.",
            posterPath = "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
            colorHex = 0xFF1E293B,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m51",
            title = "Ucuz Roman",
            originalTitle = "Pulp Fiction",
            year = 1994,
            durationMins = 154,
            imdb = 8.9,
            moodTag = Mood.CULT_AWARD.id,
            platforms = listOf(OTHER_PLATFORM_NAME),
            genre = "Suç / Kara Komedi",
            logline = "Los Angeles yeraltı dünyasından iki tetikçi, bir boksör ve bir gangster eşinin yolları iç içe geçmiş hikayelerde kesişir.",
            whyWatch = "Tarantino'nun diyalog dehası, müzikleri ve kurgusuyla kült sinemanın kitabı.",
            posterPath = "/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
            colorHex = 0xFFB91C1C,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id),
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m52",
            title = "Cinnet",
            originalTitle = "The Shining",
            year = 1980,
            durationMins = 146,
            imdb = 8.4,
            moodTag = Mood.HORROR.id,
            platforms = listOf(OTHER_PLATFORM_NAME),
            genre = "Korku / Psikolojik",
            logline = "Kış bakıcılığı için izole Overlook Oteli'ne taşınan yazar Jack Torrance, otelin karanlık geçmişiyle akıl sağlığını yitirir.",
            whyWatch = "Stanley Kubrick ve Jack Nicholson'dan tüm zamanların en ikonik psikolojik korku klasiği.",
            posterPath = "/xazWoLealQwEgqZ895Z8j9bX41w.jpg",
            colorHex = 0xFF991B1B,
            audioOptions = listOf(AudioLanguageOption.SUBTITLED.id, AudioLanguageOption.DUBBED.id),
            isCultOrMostWatched = true,
            isNewRelease = false
        ),
        Movie(
            id = "m53",
            title = "Dövüş Kulübü",
            originalTitle = "Fight Club",
            year = 1999,
            durationMins = 139,
            imdb = 8.8,
            moodTag = Mood.MIND_BENDER.id,
            platforms = listOf(OTHER_PLATFORM_NAME),
            genre = "Dram / Gerilim",
            logline = "Uykusuzluk çeken sıradan bir beyaz yakalı, karizmatik sabun satıcısı Tyler Durden ile yeraltı dövüş kulübü kurar.",
            whyWatch = "Kült finallerin atası; modern tüketim toplumuna sert bir yumruk.",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            colorHex = 0xFFBE185D,
            audioOptions = listOf(AudioLanguageOption.DUBBED.id, AudioLanguageOption.SUBTITLED.id),
            isCultOrMostWatched = true,
            isNewRelease = false
        )
    )

    private val _dailyExtraMovies = MutableStateFlow<List<Movie>>(emptyList())

    val allMovies: List<Movie>
        get() = (_dailyExtraMovies.value + baseMovies).distinctBy { it.id }

    private val _dailyFeedLastUpdated = MutableStateFlow(currentDateKey())
    val dailyFeedLastUpdated: StateFlow<String> = _dailyFeedLastUpdated.asStateFlow()

    private val _dailyUpdatedMovies = MutableStateFlow<List<Movie>>(computeDailyNewReleases())
    val dailyUpdatedMovies: StateFlow<List<Movie>> = _dailyUpdatedMovies.asStateFlow()

    fun initDailyFeed(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREF_DAILY_FEED, Context.MODE_PRIVATE)
        }
        syncDailyMovieUpdates(forceRefresh = false)
    }

    fun syncDailyMovieUpdates(
        forceRefresh: Boolean = false,
        extraDailyAdditions: List<Movie> = emptyList()
    ) {
        val today = currentDateKey()
        val storedDate = prefs?.getString(KEY_LAST_SYNC_DATE, null)
        if (extraDailyAdditions.isNotEmpty()) {
            val tagged = extraDailyAdditions.map { movie ->
                movie.copy(
                    isNewRelease = true,
                    addedDateKey = movie.addedDateKey ?: today
                )
            }
            _dailyExtraMovies.value = (tagged + _dailyExtraMovies.value).distinctBy { it.id }
        }

        if (forceRefresh || storedDate != today || _dailyUpdatedMovies.value.isEmpty()) {
            val nextCounter = if (forceRefresh) {
                (prefs?.getInt(KEY_SYNC_COUNT, 0) ?: 0) + 1
            } else {
                prefs?.getInt(KEY_SYNC_COUNT, 0) ?: 0
            }
            prefs?.edit()
                ?.putString(KEY_LAST_SYNC_DATE, today)
                ?.putInt(KEY_SYNC_COUNT, nextCounter)
                ?.apply()

            _dailyFeedLastUpdated.value = today
            _dailyUpdatedMovies.value = computeDailyNewReleases(rotationOffset = nextCounter, todayKey = today)
        }
    }

    private fun computeDailyNewReleases(
        rotationOffset: Int = 0,
        todayKey: String = currentDateKey()
    ): List<Movie> {
        val extras = _dailyExtraMovies.value
        val newerBase = baseMovies
            .filter { it.isNewRelease || it.year >= 2024 }
            .sortedWith(compareByDescending<Movie> { it.year }.thenByDescending { it.imdb })

        val rotatedBase = if (newerBase.isNotEmpty() && rotationOffset > 0) {
            val shift = rotationOffset % newerBase.size
            newerBase.drop(shift) + newerBase.take(shift)
        } else {
            newerBase
        }

        return (extras + rotatedBase)
            .distinctBy { it.id }
            .mapIndexed { index, movie ->
                if (movie.addedDateKey == null && index < 4) {
                    movie.copy(addedDateKey = todayKey)
                } else {
                    movie
                }
            }
    }

    fun getCultAndOtherMovies(
        excludedMovieIds: Set<String> = emptySet(),
        audioOptions: Set<String> = emptySet()
    ): List<Movie> {
        return allMovies
            .filter { it.platforms.contains(OTHER_PLATFORM_NAME) || it.isCultOrMostWatched }
            .filter { it.supportsAudioOptions(audioOptions) }
            .filterNot { excludedMovieIds.contains(it.id) }
            .sortedWith(
                compareByDescending<Movie> { it.platforms.contains(OTHER_PLATFORM_NAME) }
                    .thenByDescending { it.imdb }
            )
    }

    data class FilterResult(
        val movies: List<Movie>,
        val isExactMatch: Boolean
    )

    fun filterMovies(
        selectedMoods: Set<String>,
        selectedPlatforms: Set<String>,
        excludedMovieIds: Set<String> = emptySet(),
        audioOptions: Set<String> = emptySet()
    ): List<Movie> {
        val candidatePool = allMovies.filterNot { excludedMovieIds.contains(it.id) }.ifEmpty { allMovies }
        val exact = candidatePool.filter { movie ->
            val moodMatches = selectedMoods.isEmpty() || selectedMoods.contains(movie.moodTag)
            val platformMatches = selectedPlatforms.isEmpty() || movie.platforms.any { selectedPlatforms.contains(it) }
            val audioMatches = movie.supportsAudioOptions(audioOptions)
            moodMatches && platformMatches && audioMatches
        }

        val resolved = exact.ifEmpty {
            // Fallback: keep mood + audio preference if intersection across selected platforms is empty
            candidatePool.filter { movie ->
                val moodMatches = selectedMoods.isEmpty() || selectedMoods.contains(movie.moodTag)
                val audioMatches = movie.supportsAudioOptions(audioOptions)
                moodMatches && audioMatches
            }.ifEmpty {
                candidatePool.filter { selectedMoods.isEmpty() || selectedMoods.contains(it.moodTag) }
                    .ifEmpty { candidatePool.take(8) }
            }
        }

        return sortPrioritizingNewReleases(resolved)
    }

    val allGenres: List<String> = listOf(
        "Komedi", "Gerilim", "Aksiyon", "Dram", "Bilim Kurgu",
        "Macera", "Romantik", "Animasyon", "Gizem", "Korku", "Suç"
    )

    fun filterMoviesAdvanced(
        genres: Set<String>,
        minYear: Int = 1970,
        maxYear: Int = 2026,
        minRating: Double = 0.0,
        platforms: Set<String> = emptySet(),
        excludedMovieIds: Set<String> = emptySet(),
        audioOptions: Set<String> = emptySet()
    ): List<Movie> {
        return filterMoviesWithCriteria(
            moods = emptySet(),
            genres = genres,
            minYear = minYear,
            maxYear = maxYear,
            minRating = minRating,
            platforms = platforms,
            excludedMovieIds = excludedMovieIds,
            audioOptions = audioOptions
        ).movies
    }

    fun filterMoviesWithCriteria(
        moods: Set<String> = emptySet(),
        genres: Set<String> = emptySet(),
        minYear: Int = 1970,
        maxYear: Int = 2026,
        minRating: Double = 0.0,
        platforms: Set<String> = emptySet(),
        excludedMovieIds: Set<String> = emptySet(),
        audioOptions: Set<String> = emptySet()
    ): FilterResult {
        val candidatePool = allMovies.filterNot { excludedMovieIds.contains(it.id) }.ifEmpty { allMovies }

        val exact = candidatePool.filter { movie ->
            val moodMatches = moods.isEmpty() || moods.contains(movie.moodTag)
            val genreMatches = genres.isEmpty() || genres.any { g -> movie.genre.contains(g, ignoreCase = true) }
            val yearMatches = movie.year in minYear..maxYear
            val ratingMatches = movie.imdb >= minRating
            val platformMatches = platforms.isEmpty() || movie.platforms.any { platforms.contains(it) }
            val audioMatches = movie.supportsAudioOptions(audioOptions)
            moodMatches && genreMatches && yearMatches && ratingMatches && platformMatches && audioMatches
        }
        if (exact.isNotEmpty()) {
            return FilterResult(movies = sortPrioritizingNewReleases(exact), isExactMatch = true)
        }

        // Progressive fallback if platform/mood intersection was too strict (still respects audio & excluded watched)
        val relaxedPlatformAndMood = candidatePool.filter { movie ->
            val genreMatches = genres.isEmpty() || genres.any { g -> movie.genre.contains(g, ignoreCase = true) }
            val yearMatches = movie.year in minYear..maxYear
            val ratingMatches = movie.imdb >= minRating
            val audioMatches = movie.supportsAudioOptions(audioOptions)
            genreMatches && yearMatches && ratingMatches && audioMatches
        }
        if (relaxedPlatformAndMood.isNotEmpty()) {
            return FilterResult(movies = sortPrioritizingNewReleases(relaxedPlatformAndMood), isExactMatch = false)
        }

        val genreOrMood = candidatePool.filter { movie ->
            val matchesGenre = genres.isNotEmpty() && genres.any { g -> movie.genre.contains(g, ignoreCase = true) }
            val matchesMood = moods.isNotEmpty() && moods.contains(movie.moodTag)
            val matchesPlatform = platforms.isNotEmpty() && movie.platforms.any { platforms.contains(it) }
            val audioMatches = movie.supportsAudioOptions(audioOptions)
            (matchesGenre || matchesMood || matchesPlatform) && audioMatches
        }.ifEmpty {
            candidatePool.filter { it.supportsAudioOptions(audioOptions) }.ifEmpty { candidatePool }.take(8)
        }

        return FilterResult(movies = sortPrioritizingNewReleases(genreOrMood), isExactMatch = false)
    }

    private fun sortPrioritizingNewReleases(movies: List<Movie>): List<Movie> {
        return movies.sortedWith(
            compareByDescending<Movie> { it.addedDateKey != null }
                .thenByDescending { it.isNewRelease }
                .thenByDescending { it.year }
                .thenByDescending { it.imdb }
        )
    }

    fun getMovieById(id: String): Movie? {
        return allMovies.find { it.id == id }
    }

    fun resetDailyFeedForTests() {
        _dailyExtraMovies.value = emptyList()
        prefs?.edit()?.clear()?.apply()
        val today = currentDateKey()
        _dailyFeedLastUpdated.value = today
        _dailyUpdatedMovies.value = computeDailyNewReleases(rotationOffset = 0, todayKey = today)
    }

    private fun currentDateKey(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }

    data class MovieRecommendation(
        val movie: Movie,
        val sharedGenres: List<String>,
        val ratingDelta: Double,
        val matchPercent: Int,
        val reasonLabel: String
    )

    /**
     * Recommends up to [limit] alternative movies similar to [targetMovie] in genre and IMDb rating,
     * excluding [targetMovie] itself and any watched/excluded movie IDs when possible.
     */
    fun findSimilarAlternatives(
        targetMovie: Movie,
        excludedMovieIds: Set<String> = emptySet(),
        limit: Int = 3
    ): List<MovieRecommendation> {
        val targetGenres = parseGenreTokens(targetMovie.genre)

        val preferredCandidates = allMovies.filter {
            it.id != targetMovie.id && !excludedMovieIds.contains(it.id)
        }
        val candidates = if (preferredCandidates.size >= limit) {
            preferredCandidates
        } else {
            val extra = allMovies.filter { candidate ->
                candidate.id != targetMovie.id && preferredCandidates.none { it.id == candidate.id }
            }
            preferredCandidates + extra
        }

        val scored = candidates.map { candidate ->
            val candidateGenres = parseGenreTokens(candidate.genre)
            val shared = candidateGenres.filter { cg ->
                targetGenres.any { tg -> tg.equals(cg, ignoreCase = true) }
            }
            val unionSize = (targetGenres + candidateGenres)
                .map { it.lowercase() }
                .distinct()
                .size
                .coerceAtLeast(1)
            val genreJaccard = shared.size.toDouble() / unionSize.toDouble()
            val genreScore = (shared.size * 4.0) + (genreJaccard * 2.0)

            val moodScore = if (candidate.moodTag == targetMovie.moodTag) 1.5 else 0.0
            val ratingDelta = kotlin.math.abs(candidate.imdb - targetMovie.imdb)
            val ratingScore = ((2.5 - ratingDelta).coerceAtLeast(0.0) * 1.6) +
                if (candidate.imdb >= targetMovie.imdb) 0.25 else 0.0

            val totalScore = genreScore + moodScore + ratingScore

            val genreRatio = if (targetGenres.isEmpty()) 0.0 else shared.size.toDouble() / targetGenres.size.toDouble()
            val ratingCloseness = (1.0 - (ratingDelta / 3.0)).coerceIn(0.0, 1.0)
            val moodBonus = if (candidate.moodTag == targetMovie.moodTag) 0.12 else 0.0
            val rawMatch = (0.55 * genreRatio) + (0.33 * ratingCloseness) + moodBonus
            val matchPercent = (64 + (rawMatch * 34).toInt()).coerceIn(65, 98)

            val reasonLabel = when {
                shared.isNotEmpty() -> "Ortak tür: ${shared.joinToString(", ")} • ★ ${candidate.imdb}"
                candidate.moodTag == targetMovie.moodTag -> "Aynı atmosfer • ★ ${candidate.imdb}"
                else -> "Yakın IMDb puanı • ★ ${candidate.imdb}"
            }

            Triple(
                totalScore,
                ratingDelta,
                MovieRecommendation(
                    movie = candidate,
                    sharedGenres = shared,
                    ratingDelta = ratingDelta,
                    matchPercent = matchPercent,
                    reasonLabel = reasonLabel
                )
            )
        }

        return scored
            .sortedWith(
                compareByDescending<Triple<Double, Double, MovieRecommendation>> { it.first }
                    .thenBy { it.second }
                    .thenByDescending { it.third.movie.year }
                    .thenByDescending { it.third.movie.imdb }
            )
            .take(limit)
            .map { it.third }
    }

    private fun parseGenreTokens(genre: String): List<String> {
        return genre.split("/", ",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
