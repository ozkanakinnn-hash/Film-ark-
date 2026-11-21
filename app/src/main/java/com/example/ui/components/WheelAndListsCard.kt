package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.data.model.CustomWheelList
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Ana sayfa: "Karar Çarkını Çevir" ve "Özel Çark Listeleri" tek kartta.
 * Üstte filtre akışı, altta neon ikonlu hazır liste kutucukları (dokununca o liste çevrilir).
 */
@Composable
fun WheelAndListsCard(
    lists: List<CustomWheelList>,
    onSpinClick: () -> Unit,
    onSpinList: (String) -> Unit,
    onManageLists: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CinemaSurface)
            .border(1.5.dp, Brush.linearGradient(listOf(ElectricCyan, PopcornGold, Color(0xFF22C55E))), shape)
            .padding(18.dp)
            .testTag("home_wheel_lists_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClickLabel = "Karar çarkını aç", onClick = onSpinClick)
                .testTag("solo_mode_button")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF2DD4BF), Color(0xFF14B8A6))), CircleShape)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Karar Çarkını Çevir", fontSize = 19.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                Spacer(Modifier.height(3.dp))
                Text(
                    "Modunu ve platformunu seç, çark bu akşamki filmini anında belirlesin.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(24.dp))
        }

        Spacer(Modifier.height(14.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PopcornGold)
                .clickable(onClick = onSpinClick)
                .padding(vertical = 12.dp)
                .testTag("home_spin_filters_button")
        ) {
            Text("▶ Filtreleri Seç ve Çevir", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1408))
        }

        if (lists.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("HAZIR LİSTELER", fontSize = 11.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Black, color = PopcornGold)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${lists.size} LİSTE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF22C55E),
                    modifier = Modifier
                        .background(Color(0x2222C55E), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f).height(1.dp).background(CinemaBorder))
            }
            Spacer(Modifier.height(10.dp))
            lists.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    pair.forEach { list ->
                        val icon = filmIconForList(list)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1B1820))
                                .border(1.dp, icon.neon.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .clickable(onClickLabel = "${list.name} çarkını çevir") { onSpinList(list.id) }
                                .padding(horizontal = 8.dp, vertical = 7.dp)
                                .testTag("home_list_tile_${list.id}")
                        ) {
                            FilmIconView(icon = icon, size = 36.dp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(list.name, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${list.movies.size} film", fontSize = 10.5.sp, color = icon.neon)
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(2.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                "＋ Kendi Listeni Oluştur",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = PopcornGold,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, PopcornGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onManageLists)
                    .padding(vertical = 10.dp)
                    .testTag("home_create_list_button")
            )
            Text(
                "Tümünü Düzenle →",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = TextSecondary,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, CinemaBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = onManageLists)
                    .padding(vertical = 10.dp)
                    .testTag("custom_lists_mode_button")
            )
        }
    }
}

