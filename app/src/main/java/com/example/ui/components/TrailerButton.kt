package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Resmi fragmanı YouTube'da açar (YouTube uygulaması yüklüyse orada, değilse tarayıcıda).
 *
 * Video uygulama içine gömülmez: YouTube API anahtarı gerekmez, YouTube'un oynatıcı/reklam
 * kurallarıyla çakışmaz ve izlenme kanal sahibine (stüdyoya) gider. Fragmanı olmayan filmde gizlenir.
 */
object TrailerLauncher {
    fun youtubeUrl(trailerKey: String): String = "https://www.youtube.com/watch?v=$trailerKey"

    fun open(context: Context, trailerKey: String): Boolean = runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl(trailerKey))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }.isSuccess
}

@Composable
fun TrailerButton(
    movie: Movie,
    modifier: Modifier = Modifier,
    accent: Color = Color(0xFFD63447)
) {
    val key = movie.trailerKey ?: return
    val context = LocalContext.current
    OutlinedButton(
        onClick = { TrailerLauncher.open(context, key) },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, accent),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("trailer_button")
    ) {
        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = "Resmi Fragmanı İzle", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "YouTube'da açılır", color = TextSecondary, fontSize = 10.sp)
        }
    }
}
