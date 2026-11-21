package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig

/**
 * Fragmanı uygulama içinde bir pencerede, YouTube'un resmi gömülü oynatıcısıyla oynatır.
 *
 * - Oynatıcıya dokunulmaz: YouTube logosu, reklamlar ve kontroller olduğu gibi kalır.
 * - "Referer" olarak uygulamanın kimliği gönderilir (YouTube, WebView içindeki gömülü oynatıcılardan bunu ister).
 * - Oynatıcı içindeki "YouTube'da izle" gibi bağlantılar uygulama dışında (YouTube uygulamasında) açılır.
 * - Pencere kapanınca WebView yok edilir; video arka planda çalmaz.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TrailerPlayerDialog(
    title: String,
    trailerKey: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val embedUrl = remember(trailerKey) { TrailerLauncher.embedUrl(trailerKey) }
    var fullscreenView by remember { mutableStateOf<View?>(null) }
    var fullscreenCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(trailerKey) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                loadUrl("about:blank")
                (parent as? ViewGroup)?.removeView(this)
                destroy()
            }
            webViewRef = null
        }
    }

    Dialog(
        onDismissRequest = {
            if (fullscreenView != null) {
                fullscreenCallback?.onCustomViewHidden()
                fullscreenView = null
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0B0A0F))
                .testTag("trailer_player_dialog")
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 6.dp)
                ) {
                    Text(
                        text = "$title • Fragman",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("trailer_player_close")) {
                        Icon(Icons.Default.Close, contentDescription = "Fragmanı kapat", tint = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    if (embedUrl != null) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize().testTag("trailer_player_webview"),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    setBackgroundColor(android.graphics.Color.BLACK)
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    settings.allowFileAccess = false
                                    settings.allowContentAccess = false
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                            val host = request.url.host.orEmpty()
                                            val isEmbed = host.endsWith("youtube-nocookie.com") && request.url.path.orEmpty().startsWith("/embed")
                                            if (isEmbed) return false
                                            // Oynatıcıdan çıkan bağlantılar (YouTube'da izle, kanal vb.) dışarıda açılır
                                            runCatching {
                                                view.context.startActivity(
                                                    Intent(Intent.ACTION_VIEW, request.url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                )
                                            }
                                            return true
                                        }
                                    }
                                    webChromeClient = object : WebChromeClient() {
                                        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                                            fullscreenView = view
                                            fullscreenCallback = callback
                                        }

                                        override fun onHideCustomView() {
                                            fullscreenView = null
                                            fullscreenCallback = null
                                        }
                                    }
                                    loadDataWithBaseURL(
                                        "https://${BuildConfig.APPLICATION_ID}/",
                                        playerHtml(embedUrl),
                                        "text/html",
                                        "utf-8",
                                        null
                                    )
                                    webViewRef = this
                                }
                            }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Oynatılmıyorsa YouTube'da açabilirsin",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 8.dp).weight(1f)
                    )
                    TextButton(
                        onClick = {
                            TrailerLauncher.open(context, trailerKey)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("trailer_open_youtube")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color(0xFFE9B949), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("YouTube'da Aç", color = Color(0xFFE9B949), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Oynatıcının tam ekran düğmesi: video pencereyi kaplar
            fullscreenView?.let { view ->
                AndroidView(
                    factory = { view },
                    modifier = Modifier.matchParentSize().background(Color.Black)
                )
            }
        }
    }
}

/** Yalnızca doğrulanmış embed adresi eklenir (anahtar regex ile denetlenir; HTML enjeksiyonu yok). */
private fun playerHtml(embedUrl: String): String = """
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
