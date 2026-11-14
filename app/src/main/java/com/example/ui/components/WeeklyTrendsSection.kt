package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.data.repository.TrendingCinemaItem
import com.example.data.repository.TrendingCinemaUiState
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WeeklyTrendsSection(
    uiState: TrendingCinemaUiState,
    onRefreshClick: () -> Unit,
    onShareMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_trends_section")
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            Brush.linearGradient(listOf(NeonPurple, PopcornGold)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Haftanın Trendleri",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = ElectricCyan.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "Google Search",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Haftanın en popüler sinema seçkileri",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(44.dp)
                    .background(CinemaSurface, CircleShape)
                    .border(1.dp, CinemaBorder, CircleShape)
                    .testTag("trends_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Trendleri Yenile",
                    tint = PopcornGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (uiState) {
            is TrendingCinemaUiState.Loading -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = PopcornGold,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Haftanın sinema trendleri yükleniyor...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            is TrendingCinemaUiState.Error -> {
                Surface(
                    color = NeonPink.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.message,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            is TrendingCinemaUiState.Success -> {
                val result = uiState.result
                if (result.isPlaceholderData) {
                    Surface(
                        color = PopcornGold.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PopcornGold.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trends_firebase_notice")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Bilgi",
                                tint = PopcornGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = result.noticeMessage,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_trends_row"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(result.items, key = { it.rank }) { item ->
                        TrendingMovieCard(
                            item = item,
                            onShareClick = { onShareMovieClick(item.toShareableMovie()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingMovieCard(
    item: TrendingCinemaItem,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(232.dp)
            .border(
                width = 1.2.dp,
                color = if (item.rank == 1) PopcornGold.copy(alpha = 0.7f) else CinemaBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("trend_item_${item.rank}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box {
                    MoviePosterImage(
                        title = item.title,
                        posterPath = item.posterPath,
                        accentColorHex = item.accentColorHex,
                        cornerRadius = 10.dp,
                        showFallbackTitleBadge = false,
                        modifier = Modifier
                            .width(62.dp)
                            .height(92.dp)
                    )
                    Surface(
                        color = PopcornGold,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "#${item.rank}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.categoryOrGenre,
                        fontSize = 11.sp,
                        color = ElectricCyan,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = PopcornGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${item.imdbScore}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.trendReason,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CinemaSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.sourceName,
                        fontSize = 10.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = CinemaTheme.colors.selectedChipFill,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PopcornGold.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onShareClick() }
                        .testTag("trend_share_button_${item.rank}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Trendi paylaş",
                            tint = PopcornGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Paylaş",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold
                        )
                    }
                }
            }
        }
    }
}
