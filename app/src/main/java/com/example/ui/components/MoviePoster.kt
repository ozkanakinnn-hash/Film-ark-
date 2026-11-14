package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.TmdbImageConfig

/**
 * Reusable movie poster image backed by TMDb w500 URLs and Coil, with automatic fallback
 * to the bundled default poster placeholder when [posterPath] / [posterUrl] is missing or fails to load.
 */
@Composable
fun MoviePosterImage(
    title: String,
    posterPath: String? = null,
    posterUrl: String? = null,
    accentColorHex: Long = 0xFF8B5CF6,
    cornerRadius: Dp = 12.dp,
    showFallbackTitleBadge: Boolean = true,
    testTag: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resolvedUrl = remember(posterPath, posterUrl) {
        TmdbImageConfig.getPosterUrl(posterPath?.takeIf { it.isNotBlank() } ?: posterUrl)
    }
    var loadFailed by remember(resolvedUrl) { mutableStateOf(false) }

    val baseModifier = modifier
        .clip(RoundedCornerShape(cornerRadius))
        .background(
            Brush.verticalGradient(
                listOf(
                    Color(accentColorHex),
                    Color(0xFF0F0A1E)
                )
            )
        )
        .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(cornerRadius))
        .let { if (testTag != null) it.testTag(testTag) else it }

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        if (resolvedUrl != null && !loadFailed) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolvedUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.img_default_poster_placeholder)
                    .error(R.drawable.img_default_poster_placeholder)
                    .fallback(R.drawable.img_default_poster_placeholder)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                onError = { loadFailed = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_default_poster_placeholder),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (showFallbackTitleBadge) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xD9090614)
                                )
                            )
                        )
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}
