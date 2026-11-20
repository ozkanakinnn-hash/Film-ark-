package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.components.DataSourceAttributionCard
import com.example.ui.components.ProfileSettingsSection
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/** Ayarlar: tema, animasyonları azalt, yedekle/geri yükle, zevk seçimini yenile, veri kaynağı. */
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onRedoTasteOnboarding: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("settings_screen_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(CinemaSurface, CircleShape)
                        .testTag("settings_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                }
                Spacer(Modifier.width(12.dp))
                Text("Ayarlar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = TextPrimary)
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CinemaSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Icon(
                    if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = PopcornGold
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Koyu tema", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        if (isDarkTheme) "Sinema salonu karanlığı açık" else "Açık tema kullanılıyor",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(checkedTrackColor = PopcornGold),
                    modifier = Modifier.testTag("theme_toggle_button")
                )
            }
        }
        item { ProfileSettingsSection() }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CinemaSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("Zevkini yeniden seç", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "İzlediğin ve beğendiğin film/dizileri seçerek \"Sana Özel\" önerileri güncelle.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onRedoTasteOnboarding,
                    modifier = Modifier.fillMaxWidth().testTag("settings_redo_onboarding")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PopcornGold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Seçim Ekranını Aç", color = TextPrimary)
                }
            }
        }
        item { DataSourceAttributionCard() }
        item {
            Text(
                "Ne İzlesek? ${BuildConfig.VERSION_NAME}",
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}
