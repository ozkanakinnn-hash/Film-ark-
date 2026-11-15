package com.example.crash

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.MainActivity

/**
 * Çökme anında AYRI bir süreçte (":crash_report") açılan, Compose ve uygulama verisi kullanmayan
 * basit rapor ekranı. Uygulama açılışta bile çökse kullanıcı raporu görüp paylaşabilir.
 */
class CrashReportActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val report = intent.getStringExtra(EXTRA_REPORT)
            ?: CrashReporter.pendingReport(this)
            ?: "Rapor bulunamadı."
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val title = TextView(this).apply {
            text = "Ne İzlesek? beklenmedik şekilde kapandı"
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#F6F2EA"))
        }
        val info = TextView(this).apply {
            text = "Hatayı düzeltebilmemiz için aşağıdaki raporu paylaş. Rapor kişisel bilgi içermez."
            textSize = 14f
            setTextColor(Color.parseColor("#B5AEA3"))
            setPadding(0, dp(8), 0, dp(12))
        }
        val reportView = TextView(this).apply {
            text = report
            textSize = 11f
            typeface = Typeface.MONOSPACE
            setTextColor(Color.parseColor("#E9B949"))
            setTextIsSelectable(true)
        }
        val share = Button(this).apply {
            text = "Raporu Paylaş"
            setOnClickListener { CrashReporter.share(this@CrashReportActivity, report) }
        }
        val restart = Button(this).apply {
            text = "Uygulamayı Yeniden Başlat"
            setOnClickListener {
                CrashReporter.clear(this@CrashReportActivity)
                startActivity(
                    Intent(this@CrashReportActivity, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            }
        }
        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, 0)
            addView(share, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            addView(restart, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            setPadding(dp(20), dp(48), dp(20), dp(24))
            setBackgroundColor(Color.parseColor("#0B0A0F"))
            addView(title)
            addView(info)
            addView(buttons)
            addView(ScrollView(this@CrashReportActivity).apply {
                setPadding(0, dp(16), 0, 0)
                addView(reportView)
            }, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
        }
        setContentView(content)
    }

    companion object {
        const val EXTRA_REPORT = "crash_report_text"
    }
}
