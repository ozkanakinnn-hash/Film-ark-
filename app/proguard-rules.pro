# Ne İzlesek? — R8 kuralları (yayın derlemesi)

# Çökme raporlarında satır numaraları okunabilsin (dosya adları gizlenir)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Manifestte adı geçen bileşenler (Activity, Receiver, Provider, Application) AGP tarafından otomatik korunur.
# Room varlıkları/DAO'ları ve Compose kendi tüketici kurallarını getirir; ek kural gerekmez.

# Yedek dosyası ve uzaktan katalog org.json (Android platformu) ile elle eşlenir: yansıma yok.

# Enum adları geçmiş kayıtlarında kullanılmıyor (id alanları kullanılıyor), yine de güvenli tarafta kal
-keepclassmembers enum com.example.data.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
