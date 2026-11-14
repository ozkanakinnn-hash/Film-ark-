package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CustomWheelList
import com.example.data.model.Movie
import com.example.data.repository.CustomWheelListRepository
import com.example.data.repository.MovieRepository
import com.example.ui.components.MoviePosterImage
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
import com.example.ui.theme.VetoRed

private val CustomCategoryEmojis = listOf("👻", "👨‍👩‍👧‍👦", "🎞️", "🍿", "❤️", "🧠", "🚀", "🔥")
private val CustomCategoryAccentColors = listOf(
    0xFFE9B949L, // Gold
    0xFFDC2626L, // Horror Crimson
    0xFF10B981L, // Emerald
    0xFF2EC4B6L, // Teal
    0xFF8B5CF6L, // Violet
    0xFFE11D48L  // Rose
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomListsScreen(
    customLists: List<CustomWheelList>,
    onSpinList: (String) -> Unit,
    onSaveList: (
        editingListId: String?,
        name: String,
        emoji: String,
        description: String,
        movies: List<Movie>,
        accentColorHex: Long
    ) -> Unit,
    onDeleteList: (String) -> Unit,
    onRestorePresets: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var showEditorDialog by remember { mutableStateOf(false) }
    var editingList by remember { mutableStateOf<CustomWheelList?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .testTag("custom_lists_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(22.dp))

            // Top App Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .background(CinemaSurface, CircleShape)
                        .testTag("custom_lists_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = PopcornGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PopcornGold.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = "ÖZEL ÇARK LİSTELERİ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = PopcornGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${customLists.size} Kategori • Kendi Çarkını Kur",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = {
                        editingList = null
                        showEditorDialog = true
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .background(PopcornGold, CircleShape)
                        .testTag("custom_lists_create_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Yeni Kategori Oluştur",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Banner + Primary Create CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                CinemaTheme.colors.heroGradientStart,
                                CinemaTheme.colors.heroGradientEnd
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(PopcornGold, ElectricCyan)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Kendi Temalı Çarkını Oluştur 🎡",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Korku Gecesi, Aile Dostu, Klasikler veya tamamen kendi belirlediğin filmlerden oluşan özel çark kategorileri kurup tek dokunuşla çevir.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                editingList = null
                                showEditorDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(48.dp)
                                .testTag("custom_lists_create_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Yeni Kategori Ekle",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = onRestorePresets,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CinemaBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(48.dp)
                                .testTag("restore_presets_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hazırlar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (customLists.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("custom_lists_empty_state"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalMovies,
                            contentDescription = null,
                            tint = PopcornGold,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Henüz Özel Çark Listen Yok",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Yukarıdaki butonla kendi özel kategorini oluşturabilir veya hazır kategorileri geri yükleyebilirsin.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(customLists, key = { it.id }) { customList ->
                CustomWheelCategoryCard(
                    customList = customList,
                    onSpinClick = { onSpinList(customList.id) },
                    onEditClick = {
                        editingList = customList
                        showEditorDialog = true
                    },
                    onDeleteClick = { onDeleteList(customList.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    if (showEditorDialog) {
        CustomWheelListEditorDialog(
            initialList = editingList,
            onDismiss = { showEditorDialog = false },
            onSave = { name, emoji, description, movies, accentColorHex ->
                onSaveList(editingList?.id, name, emoji, description, movies, accentColorHex)
                showEditorDialog = false
            }
        )
    }
}

@Composable
private fun CustomWheelCategoryCard(
    customList: CustomWheelList,
    onSpinClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val accent = Color(customList.accentColorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.2.dp,
                color = accent.copy(alpha = 0.55f),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("custom_list_card_${customList.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(accent.copy(alpha = 0.18f), CircleShape)
                        .border(1.2.dp, accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = customList.emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = customList.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            color = if (customList.isPreset) accent.copy(alpha = 0.16f) else ElectricCyan.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (customList.isPreset) "HAZIR" else "ÖZEL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (customList.isPreset) accent else ElectricCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${customList.movies.size} Film" +
                            if (customList.description.isNotBlank()) " • ${customList.description}" else "",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(42.dp)
                        .testTag("delete_custom_list_${customList.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "${customList.name} listesini sil",
                        tint = VetoRed.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Poster / Movie Preview Strip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(customList.movies, key = { "${customList.id}_${it.id}" }) { movie ->
                    Surface(
                        color = CinemaSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CinemaBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MoviePosterImage(
                                title = movie.title,
                                posterPath = movie.posterPath,
                                posterUrl = movie.posterUrl,
                                accentColorHex = movie.colorHex,
                                cornerRadius = 6.dp,
                                showFallbackTitleBadge = false,
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(34.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = movie.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "★ ${movie.imdb} • ${movie.year}",
                                    fontSize = 10.sp,
                                    color = PopcornGold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Spin This Custom List + Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSpinClick,
                    enabled = customList.movies.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("spin_custom_list_${customList.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bu Listeyi Çevir 🎡",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp)
                        .testTag("edit_custom_list_${customList.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Düzenle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomWheelListEditorDialog(
    initialList: CustomWheelList?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        emoji: String,
        description: String,
        movies: List<Movie>,
        accentColorHex: Long
    ) -> Unit
) {
    var name by remember(initialList?.id) { mutableStateOf(initialList?.name ?: "") }
    var description by remember(initialList?.id) { mutableStateOf(initialList?.description ?: "") }
    var selectedEmoji by remember(initialList?.id) { mutableStateOf(initialList?.emoji ?: "🍿") }
    var selectedAccentHex by remember(initialList?.id) {
        mutableLongStateOf(initialList?.accentColorHex ?: CustomCategoryAccentColors.first())
    }
    var customMovieTitleInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val selectedMovies = remember(initialList?.id) {
        mutableStateListOf<Movie>().apply {
            if (initialList != null) addAll(initialList.movies)
        }
    }

    val catalogMatches = remember(searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) MovieRepository.allMovies
        else MovieRepository.allMovies.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.genre.contains(q, ignoreCase = true) ||
                it.originalTitle.contains(q, ignoreCase = true)
        }
    }

    val customAddedOnly = selectedMovies.filter { MovieRepository.getMovieById(it.id) == null }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(PopcornGold, ElectricCyan, NeonPink)),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("custom_list_editor_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .padding(16.dp)
                    .testTag("custom_list_editor_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (initialList == null) "Yeni Çark Kategorisi" else "Kategoriyi Düzenle",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(40.dp)
                                .background(CinemaSurfaceVariant, CircleShape)
                                .testTag("editor_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                item {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PopcornGold,
                        unfocusedBorderColor = CinemaBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Kategori Adı (örn. Korku Gecesi)") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_list_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Kısa Açıklama (İsteğe bağlı)") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_list_desc_input")
                    )
                }

                item {
                    Text(
                        text = "Simge & Renk Seç",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomCategoryEmojis.forEachIndexed { idx, emoji ->
                            val isSelected = selectedEmoji == emoji
                            Surface(
                                color = if (isSelected) PopcornGold.copy(alpha = 0.22f) else CinemaSurfaceVariant,
                                shape = CircleShape,
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSelected) PopcornGold else CinemaBorder
                                ),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedEmoji = emoji }
                                    .testTag("custom_list_emoji_$idx")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CustomCategoryAccentColors.forEachIndexed { idx, hex ->
                            val isSelected = selectedAccentHex == hex
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else CinemaBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedAccentHex = hex }
                                    .testTag("custom_list_color_$idx")
                            )
                        }
                    }
                }

                item {
                    // Add custom movie title inline (beyond built-in catalog)
                    Text(
                        text = "Listeye Özel Film Adı Ekle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customMovieTitleInput,
                            onValueChange = { customMovieTitleInput = it },
                            placeholder = { Text("Örn. The Shining") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = CinemaBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_movie_title_input")
                        )

                        Button(
                            onClick = {
                                val trimmed = customMovieTitleInput.trim()
                                if (trimmed.isNotEmpty()) {
                                    val customMovie = CustomWheelListRepository.createCustomMovie(
                                        title = trimmed,
                                        genre = name.trim().ifBlank { "Özel Seçki" },
                                        accentColorHex = selectedAccentHex
                                    )
                                    selectedMovies.add(0, customMovie)
                                    customMovieTitleInput = ""
                                }
                            },
                            enabled = customMovieTitleInput.trim().isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("add_custom_movie_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Özel Film Ekle",
                                tint = CinemaTheme.colors.onAccentTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ekle",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = CinemaTheme.colors.onAccentTeal
                            )
                        }
                    }

                    if (customAddedOnly.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            customAddedOnly.forEach { customMov ->
                                Surface(
                                    color = NeonPurple.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.dp, NeonPurple),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .clickable { selectedMovies.removeAll { it.id == customMov.id } }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✨ ${customMov.title}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Kaldır",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Katalogdan Film Seç (${selectedMovies.size} seçili)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold
                        )
                        if (selectedMovies.size < 2) {
                            Text(
                                text = "En az 2 film seçin",
                                fontSize = 11.sp,
                                color = VetoRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Film veya tür ara...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PopcornGold,
                            unfocusedBorderColor = CinemaBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("catalog_search_input")
                    )
                }

                items(catalogMatches, key = { "picker_${it.id}" }) { movie ->
                    val isChecked = selectedMovies.any { it.id == movie.id }
                    Surface(
                        color = if (isChecked) EmeraldGreen.copy(alpha = 0.14f) else CinemaSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isChecked) EmeraldGreen else CinemaBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (isChecked) {
                                    selectedMovies.removeAll { it.id == movie.id }
                                } else {
                                    selectedMovies.add(movie)
                                }
                            }
                            .testTag("catalog_movie_toggle_${movie.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = movie.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${movie.year} • ${movie.genre}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = PopcornGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${movie.imdb}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PopcornGold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (isChecked) EmeraldGreen else Color.Transparent,
                                            CircleShape
                                        )
                                        .border(
                                            1.5.dp,
                                            if (isChecked) EmeraldGreen else TextMuted,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChecked) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seçildi",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    val canSave = name.trim().isNotEmpty() && selectedMovies.size >= 2
                    Button(
                        onClick = {
                            onSave(
                                name.trim(),
                                selectedEmoji,
                                description.trim(),
                                selectedMovies.toList(),
                                selectedAccentHex
                            )
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_custom_list_button")
                    ) {
                        Text(
                            text = if (initialList == null) "Özel Çark Listesini Kaydet ✅" else "Değişiklikleri Kaydet ✅",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
