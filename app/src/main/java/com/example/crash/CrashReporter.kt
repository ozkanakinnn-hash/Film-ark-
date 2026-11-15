package com.example.crash

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Cihazda çökme raporu tutar (bilgisayar/adb gerekmeden hata ayıklama için).
 *
 * Uygulama çökünce hata izi uygulamanın özel klasörüne yazılır; bir sonraki açılışta kullanıcıya
 * "Raporu Paylaş" seçeneğiyle gösterilir. Rapor kişisel veri içermez ve yalnızca kullanıcı paylaşırsa
 * cihazdan çıkar. Önceki işleyici (sistemin "uygulama durdu" davranışı) korunur.
 */
object CrashReporter {
    private const val FILE_NAME = "last_crash_report.txt"
    private const val MAX_CHARS = 24_000

    @Volatile
    private var installed = false

    fun install(context: Context) {
        if (installed) return
        installed = true
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { write(appContext, thread, throwable) }
            previous?.uncaughtException(thread, throwable)
        }
    }

    private fun file(context: Context) = File(context.filesDir, FILE_NAME)

    fun write(context: Context, thread: Thread, throwable: Throwable) {
        val trace = StringWriter().also { throwable.printStackTrace(PrintWriter(it)) }.toString()
        val report = buildString {
            appendLine("Ne İzlesek? çökme raporu")
            appendLine("Zaman: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
            appendLine("Sürüm: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) • ${BuildConfig.BUILD_TYPE}")
            appendLine("Poster modu: ${if (BuildConfig.TMDB_POSTERS_ENABLED) "tmdb" else "cards"}")
            appendLine("Cihaz: ${Build.MANUFACTURER} ${Build.MODEL} • Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("İş parçacığı: ${thread.name}")
            appendLine()
            append(trace)
        }.take(MAX_CHARS)
        file(context).writeText(report)
    }

    /** Bekleyen (henüz gösterilmemiş) çökme raporu varsa döndürür. */
    fun pendingReport(context: Context): String? =
        runCatching { file(context).takeIf { it.exists() }?.readText()?.takeIf { it.isNotBlank() } }.getOrNull()

    fun clear(context: Context) {
        runCatching { file(context).delete() }
    }

    fun share(context: Context, report: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Ne İzlesek? çökme raporu")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        runCatching {
            context.startActivity(Intent.createChooser(send, "Hata raporunu paylaş").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
