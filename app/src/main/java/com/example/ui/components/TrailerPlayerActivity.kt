package com.example.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.BuildConfig

/**
 * Fragman oynatıcı ekranı (klasik View, Compose yok).
 *
 * Neden ayrı ekran: YouTube'un gömülü oynatıcısı videoyu pencerenin arkasındaki ayrı bir yüzeye çizer.
 * Compose Dialog içinde (yuvarlak köşe/kırpma ile) bu yüzey örtülüp yalnızca ses geliyordu.
 * Opak, donanım hızlandırmalı tam bir Activity penceresinde video ve oynatıcı kontrolleri düzgün görünür.
 *
 * YouTube API Hizmetleri kuralları: resmi oynatıcı değiştirilmeden kullanılır (logo, reklam, kontroller),
 * en az 200x200, arka planda oynatma yok (ekran kapanınca WebView yok edilir), indirme yok.
 */
class TrailerPlayerActivity : Activity() {

    private var webView: WebView? = null
    private var root: FrameLayout? = null
    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        val key = intent.getStringExtra(EXTRA_KEY).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val embedUrl = TrailerLauncher.embedUrl(key)
        if (embedUrl == null) {
            TrailerLauncher.open(this, key)
            finish()
            return
        }

        val gold = Color.parseColor("#E9B949")
        val frame = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        root = frame

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Üst çubuk: başlık + kapat
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(10), dp(4), dp(10))
        }
        top.addView(TextView(this).apply {
            text = "$title • Fragman"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        top.addView(TextView(this).apply {
            text = "✕"
            contentDescription = "Fragmanı kapat"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setPadding(dp(14), dp(6), dp(14), dp(6))
            setOnClickListener { finish() }
            tag = "trailer_player_close"
        })

        val player = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val host = request.url.host.orEmpty()
                    if (host.endsWith("youtube-nocookie.com") && request.url.path.orEmpty().startsWith("/embed")) return false
                    runCatching { startActivity(Intent(Intent.ACTION_VIEW, request.url)) }
                    return true
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                    fullscreenView = view
                    fullscreenCallback = callback
                    frame.addView(view, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                    column.visibility = View.GONE
                }

                override fun onHideCustomView() {
                    fullscreenView?.let { frame.removeView(it) }
                    fullscreenView = null
                    fullscreenCallback = null
                    column.visibility = View.VISIBLE
                }
            }
            loadDataWithBaseURL("https://${BuildConfig.APPLICATION_ID}/", playerHtml(embedUrl), "text/html", "utf-8", null)
        }
        webView = player
        val screenW = resources.displayMetrics.widthPixels
        column.addView(top)
        column.addView(player, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenW * 9 / 16))

        // Alt: YouTube'da aç yedeği
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        bottom.addView(TextView(this).apply {
            text = "Oynatılmıyorsa YouTube'da açabilirsin"
            setTextColor(Color.argb(160, 255, 255, 255))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        bottom.addView(TextView(this).apply {
            text = "YouTube'da Aç ↗"
            setTextColor(gold)
            typeface = Typeface.DEFAULT_BOLD
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnClickListener {
                TrailerLauncher.open(this@TrailerPlayerActivity, key)
                finish()
            }
            tag = "trailer_open_youtube"
        })
        column.addView(bottom)

        frame.addView(column, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setContentView(frame)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Döndürünce oynatıcı 16:9 kalsın
        webView?.let { wv ->
            wv.layoutParams = wv.layoutParams.apply { height = resources.displayMetrics.widthPixels * 9 / 16 }
        }
    }

    @Deprecated("Geri tuşu: önce tam ekrandan çık")
    override fun onBackPressed() {
        if (fullscreenView != null) {
            fullscreenCallback?.onCustomViewHidden()
            webView?.webChromeClient?.onHideCustomView()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onDestroy() {
        webView?.apply {
            stopLoading()
            loadUrl("about:blank")
            (parent as? ViewGroup)?.removeView(this)
            destroy()
        }
        webView = null
        super.onDestroy()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_KEY = "trailer_key"
        const val EXTRA_TITLE = "trailer_title"

        fun intent(context: Context, title: String, key: String): Intent =
            Intent(context, TrailerPlayerActivity::class.java)
                .putExtra(EXTRA_KEY, key)
                .putExtra(EXTRA_TITLE, title)
    }
}

/** Yalnızca doğrulanmış embed adresi eklenir (anahtar regex ile denetlenir; HTML enjeksiyonu yok). */
internal fun playerHtml(embedUrl: String): String = """
<!DOCTYPE html>
<html><head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>html,body{margin:0;padding:0;background:#000;height:100%;overflow:hidden}
iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:0}</style>
</head><body>
<iframe src="$embedUrl" title="YouTube video player"
 allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
 referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
</body></html>
""".trimIndent()
