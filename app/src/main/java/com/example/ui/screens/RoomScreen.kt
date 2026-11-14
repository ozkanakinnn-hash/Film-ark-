package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Room
import com.example.data.model.RoomStatus
import com.example.ui.theme.CinemaBackground
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RoomScreen(
    room: Room?,
    roomCodeInput: String,
    onCodeInputChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: (String) -> Unit,
    onSimulatePartner: () -> Unit,
    onProceedToFilters: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var copiedCode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(CinemaSurface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Ortak Karar Odası",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "İki telefon, tek senkron çark 🎡",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tabs: [Oda Kur] / [Odaya Katıl]
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CinemaSurface,
            contentColor = PopcornGold,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PopcornGold,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Oda Kur 🍿",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) PopcornGold else TextSecondary
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Odaya Katıl 🔑",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) PopcornGold else TextSecondary
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedTab == 0) {
            // TAB 1: CREATE ROOM
            if (room == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🛋️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Birlikte Karar Verelim",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Oda kurduğunda anlık 4 haneli bir kod üretilecek. WhatsApp ile eşine gönder, odaya katıldığında iki cihaz senkronize olsun!",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onCreateRoom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("create_room_action_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Oda Kodunu Oluştur",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            } else {
                // ROOM ACTIVE: DISPLAY ROOM CODE & VIRAL SHARE ENGINE
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.5.dp,
                            Brush.linearGradient(listOf(NeonPurple, PopcornGold)),
                            RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SENİN ODA KODUN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = ElectricCyan
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large 4-digit code display
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            room.roomCode.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(CinemaTheme.colors.calloutSurface, RoundedCornerShape(12.dp))
                                        .border(1.dp, PopcornGold, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char.toString(),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PopcornGold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // One-tap copy code
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(room.roomCode))
                                    copiedCode = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (copiedCode) "Kopyalandı! ✓" else "Kodu Kopyala",
                                fontSize = 13.sp,
                                color = if (copiedCode) EmeraldGreen else TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // VIRAL LOOP MOTORU: DIRECT WHATSAPP BUTTON
                        val whatsappMessage = "🍿 Akşam ne izleyeceğimize karar verememe krizini bitiriyoruz! Odaya gel, çarkı çevirelim: https://neizlesek.app/room/${room.roomCode} (Oda Kodu: ${room.roomCode})"

                        Button(
                            onClick = {
                                try {
                                    val uri = Uri.parse("whatsapp://send?text=" + Uri.encode(whatsappMessage))
                                    val waIntent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(waIntent)
                                } catch (e: Exception) {
                                    // Fallback to general share chooser
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, whatsappMessage)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Oda Kodunu Gönder"))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("whatsapp_share_room_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WhatsApp ile Eşine Gönder 💬",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Participant Status List
                        Text(
                            text = "Katılımcılar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Host Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CinemaSurfaceVariant, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "👑", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sen (Oda Sahibi)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Bağlandı • Hazır",
                                    fontSize = 11.sp,
                                    color = EmeraldGreen
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Guest / Partner Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CinemaSurfaceVariant, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🍿", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = room.guest?.name ?: "Eşin / Arkadaşın",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (room.guest != null) "Odaya katıldı! Senkronize." else "WhatsApp linki veya kod bekleniyor...",
                                    fontSize = 11.sp,
                                    color = if (room.guest != null) EmeraldGreen else PopcornGold
                                )
                            }
                            if (room.guest != null) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = PopcornGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Simulation button for single phone demo
                        if (room.guest == null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = onSimulatePartner,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulate_partner_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Partneri Simüle Et (Test Modu) ⚡",
                                    fontSize = 12.sp,
                                    color = ElectricCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Proceed to Filters Button
                        Button(
                            onClick = onProceedToFilters,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("proceed_to_filters_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Filtrelere Geç (10 Saniye) ⚡",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        } else {
            // TAB 2: JOIN ROOM WITH 4-DIGIT CODE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🔑", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Oda Kodunu Gir",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Eşinin veya arkadaşının WhatsApp'tan paylaştığı 4 haneli kodu yaz:",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                onCodeInputChange(input)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 12.sp,
                            textAlign = TextAlign.Center,
                            color = PopcornGold
                        ),
                        placeholder = {
                            Text(
                                text = "0000",
                                fontSize = 32.sp,
                                letterSpacing = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PopcornGold,
                            unfocusedBorderColor = CinemaBorder,
                            focusedContainerColor = CinemaSurfaceVariant,
                            unfocusedContainerColor = CinemaSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag("room_code_input_field")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onJoinRoom(roomCodeInput) },
                        enabled = roomCodeInput.length == 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_join_room_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Odaya Bağlan 🍿",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
