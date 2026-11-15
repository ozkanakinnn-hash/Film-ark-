package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private const val TMDB_URL = "https://www.themoviedb.org/"

/**
 * TMDB kullanım koşullarının zorunlu kıldığı veri kaynağı atfı.
 * TMDB logosu resmi "Primary short" varlığıdır; renkleri ve oranları değiştirilmemelidir.
 */
@Composable
fun DataSourceAttributionCard(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClickLabel = "TMDB web sitesini aç") {
                runCatching { uriHandler.openUri(TMDB_URL) }
            }
            .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) {}
            .testTag("profile_data_source_attribution"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = "VERİ KAYNAĞI",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.tmdb_logo_short),
                contentDescription = "TMDB (The Movie Database) logosu"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Film bilgileri ve posterler TMDB'den alınmaktadır.",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "This product uses the TMDB API but is not endorsed or certified by TMDB.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Platform bilgileri JustWatch tarafından sağlanır. Platform, dublaj ve altyazı bilgileri değişebilir.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
