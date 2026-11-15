package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupManager
import com.example.data.local.MotionPreferences
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Profil > Ayarlar: animasyonları azalt + kendi dosyana yedekle / geri yükle.
 */
@Composable
fun ProfileSettingsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reduceMotion by MotionPreferences.reduceMotion.collectAsState()
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = true
        scope.launch {
            status = runCatching { BackupManager.exportTo(context, uri) }
                .fold({ "✓ Yedek kaydedildi. Dosyayı güvenli bir yerde sakla." }, { "Yedek alınamadı: ${it.message ?: "bilinmeyen hata"}" })
            busy = false
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = true
        scope.launch {
            status = runCatching { BackupManager.importFrom(context, uri) }
                .fold({ "✓ ${it.summary()}." }, { "Geri yüklenemedi: ${it.message ?: "bilinmeyen hata"}" })
            busy = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
            .testTag("profile_settings_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AYARLAR", fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Animasyonları azalt", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "Tür açılış sahnesi ve kayan şerit kapanır, çark daha kısa döner. " +
                            "Telefonda animasyonlar kapalıysa kendiliğinden açılır.",
                        fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = reduceMotion,
                    onCheckedChange = { MotionPreferences.setReduceMotion(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = ElectricCyan),
                    modifier = Modifier.testTag("profile_reduce_motion_switch")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Yedekle ve geri yükle", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                "Geçmiş, izlediklerin, notların, favorilerin, özel listelerin ve \"Daha Sonra İzle\" " +
                    "senin seçtiğin bir dosyaya kaydedilir. Hiçbir sunucuya gönderilmez.",
                fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { exportLauncher.launch(BackupManager.suggestedFileName()) },
                    enabled = !busy,
                    modifier = Modifier.weight(1f).testTag("profile_backup_button")
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = PopcornGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Yedekle", color = TextPrimary)
                }
                OutlinedButton(
                    onClick = { confirmRestore = true },
                    enabled = !busy,
                    modifier = Modifier.weight(1f).testTag("profile_restore_button")
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Geri Yükle", color = TextPrimary)
                }
            }
            status?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.testTag("profile_backup_status"))
            }
        }
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text("Yedekten geri yükle") },
            text = {
                Text(
                    "Seçtiğin yedek dosyasındaki kayıtlar mevcut verilerinle birleştirilir; " +
                        "şu anki verilerin silinmez."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRestore = false
                        importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                    },
                    modifier = Modifier.testTag("profile_restore_confirm_button")
                ) { Text("Dosya Seç") }
            },
            dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text("Vazgeç") } }
        )
    }
}
