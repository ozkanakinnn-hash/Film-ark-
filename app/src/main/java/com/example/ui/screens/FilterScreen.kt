package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioLanguageOption
import com.example.data.model.AvailablePlatforms
import com.example.data.model.CustomWheelList
import com.example.data.model.Mood
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.ui.components.MovieFilterComponent
import com.example.ui.components.MoviePosterImage
import com.example.ui.components.YearFilterOption
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
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterScreen(
    isSoloMode: Boolean,
    selectedMoods: Set<String>,
    selectedPlatforms: Set<String>,
    partnerMoods: Set<String>,
    partnerPlatforms: Set<String>,
    isAdvancedFilterMode: Boolean,
    selectedGenres: Set<String>,
    selectedAudioOptions: Set<String> = emptySet(),
    selectedYearOptionId: String,
    minYear: Int,
    maxYear: Int,
    minRating: Double,
    watchedMovieIds: Set<String> = emptySet(),
    customWheelLists: List<CustomWheelList> = emptyList(),
    onSpinCustomList: (String) -> Unit = {},
    onOpenCustomLists: () -> Unit = {},
    onToggleFilterMode: (Boolean) -> Unit,
    onToggleMood: (String, Boolean) -> Unit,
    onTogglePlatform: (String, Boolean) -> Unit,
    onGenreToggle: (String) -> Unit,
    onClearGenres: () -> Unit,
    onToggleAudioOption: (String) -> Unit = {},
    onClearAudioOptions: () -> Unit = {},
    onSpinCultOtherClick: () -> Unit = {},
    onQuickSelectCultMovie: (Movie) -> Unit = {},
    onYearOptionSelected: (YearFilterOption) -> Unit,
    onYearRangeChange: (Int, Int) -> Unit,
    onRatingChange: (Double) -> Unit,
    onResetAdvancedFilters: () -> Unit,
    onProceedToWheel: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var editingPartner by remember { mutableStateOf(false) }
    var showQuickNarrowFilters by remember { mutableStateOf(false) }

    val activeMoods = if (isSoloMode || !editingPartner) selectedMoods else partnerMoods
    val activePlatforms = if (isSoloMode || !editingPartner) selectedPlatforms else partnerPlatforms

    // Calculate common intersection preview for fast mood mode
    val commonMoods = if (!isSoloMode && selectedMoods.isNotEmpty() && partnerMoods.isNotEmpty()) {
        val inter = selectedMoods.intersect(partnerMoods)
        if (inter.isNotEmpty()) inter else selectedMoods + partnerMoods
    } else selectedMoods

    val commonPlatforms = if (!isSoloMode && selectedPlatforms.isNotEmpty() && partnerPlatforms.isNotEmpty()) {
        val inter = selectedPlatforms.intersect(partnerPlatforms)
        if (inter.isNotEmpty()) inter else selectedPlatforms + partnerPlatforms
    } else selectedPlatforms

    val effectiveMoods = if (isAdvancedFilterMode) emptySet() else commonMoods
    val filterResult = MovieRepository.filterMoviesWithCriteria(
        moods = effectiveMoods,
        genres = selectedGenres,
        minYear = minYear,
        maxYear = maxYear,
        minRating = minRating,
        platforms = commonPlatforms,
        excludedMovieIds = watchedMovieIds,
        audioOptions = selectedAudioOptions
    )
    val eligibleMovies = filterResult.movies
    val isExactMatch = filterResult.isExactMatch

    val cultOtherMovies = remember(watchedMovieIds, selectedAudioOptions) {
        MovieRepository.getCultAndOtherMovies(
            excludedMovieIds = watchedMovieIds,
            audioOptions = selectedAudioOptions
        )
    }

    val activeNarrowingCount =
        (if (selectedGenres.isNotEmpty()) 1 else 0) +
            (if (selectedAudioOptions.isNotEmpty()) 1 else 0) +
            (if (minYear > 1970 || maxYear < 2026) 1 else 0) +
            (if (minRating > 0.0) 1 else 0)

    val narrowingSummaryParts = buildList {
        if (selectedGenres.isNotEmpty()) add(selectedGenres.joinToString(", "))
        if (selectedAudioOptions.contains(AudioLanguageOption.DUBBED.id)) add("Türkçe Dublaj")
        if (selectedAudioOptions.contains(AudioLanguageOption.SUBTITLED.id)) add("Türkçe Altyazı")
        if (minYear > 1970 || maxYear < 2026) add("$minYear-$maxYear")
        if (minRating > 0.0) add("★${String.format(Locale.US, "%.1f", minRating)}+")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("filter_screen_list"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .background(CinemaSurface, CircleShape)
                        .testTag("filter_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isAdvancedFilterMode) "Film Filtreleme" else "Hızlı Filtreleme",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (isAdvancedFilterMode) NeonPurple else PopcornGold,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isAdvancedFilterMode) "Tür • Yıl • Puan" else "10 Saniye ⚡",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAdvancedFilterMode) Color.White else Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isAdvancedFilterMode) "Kriterleri belirle, çark sadece en sevdiklerinle dönsün"
                        else if (isSoloMode) "Modunu ve platformunu seç, istersen tür/yıl/puanla daralt" else "İki taraf da seçer, sistem kesişimini alır",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Launch Custom Wheel Categories Strip
            if (customWheelLists.isNotEmpty() || true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Özel Çark Kategorileri",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopcornGold
                    )
                    Surface(
                        color = CinemaSurfaceVariant,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onOpenCustomLists() }
                            .testTag("filter_open_custom_lists_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Özel kategori oluştur",
                                tint = ElectricCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Oluştur / Düzenle",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("filter_custom_lists_row")
                ) {
                    items(customWheelLists, key = { "filter_cl_${it.id}" }) { customList ->
                        val accent = Color(customList.accentColorHex)
                        Surface(
                            color = accent.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSpinCustomList(customList.id) }
                                .testTag("filter_custom_list_${customList.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = customList.emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = customList.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${customList.movies.size} Film • Çevir",
                                        fontSize = 10.sp,
                                        color = accent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Mode Selector Tabs: [⚡ Hızlı Mod] / [🎯 Tür, Yıl, Puan Filtreleme]
            TabRow(
                selectedTabIndex = if (isAdvancedFilterMode) 1 else 0,
                containerColor = CinemaSurface,
                contentColor = PopcornGold,
                indicator = { tabPositions ->
                    val tabIdx = if (isAdvancedFilterMode) 1 else 0
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabIdx]),
                        color = PopcornGold,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("filter_mode_tabs")
            ) {
                Tab(
                    selected = !isAdvancedFilterMode,
                    onClick = { onToggleFilterMode(false) },
                    modifier = Modifier.testTag("quick_filter_tab"),
                    text = {
                        Text(
                            text = "Hızlı Mod (10 Sn) ⚡",
                            fontWeight = if (!isAdvancedFilterMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isAdvancedFilterMode) PopcornGold else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = isAdvancedFilterMode,
                    onClick = { onToggleFilterMode(true) },
                    modifier = Modifier.testTag("advanced_filter_tab"),
                    text = {
                        Text(
                            text = "Tür, Yıl & Puan 🎯",
                            fontWeight = if (isAdvancedFilterMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (isAdvancedFilterMode) PopcornGold else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Partner Toggle Switch if in Room Mode
            if (!isSoloMode && !isAdvancedFilterMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (editingPartner) "Partnerin Tercihlerini Düzenliyorsun 🍿" else "Kendi Tercihlerini Düzenliyorsun 👑",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (editingPartner) NeonPink else PopcornGold
                            )
                            Text(
                                text = "İki cihaz senkron kesişim hesabı",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = editingPartner,
                            onCheckedChange = { editingPartner = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonPink,
                                checkedTrackColor = NeonPink.copy(alpha = 0.25f),
                                uncheckedThumbColor = PopcornGold,
                                uncheckedTrackColor = PopcornGold.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.testTag("toggle_partner_editing_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (isAdvancedFilterMode) {
            // ADVANCED FILTER COMPONENT (Tür, Yıl, Puan)
            item {
                MovieFilterComponent(
                    selectedGenres = selectedGenres,
                    onGenreToggle = onGenreToggle,
                    onClearGenres = onClearGenres,
                    selectedYearOptionId = selectedYearOptionId,
                    minYear = minYear,
                    maxYear = maxYear,
                    onYearOptionSelected = onYearOptionSelected,
                    onYearRangeChange = onYearRangeChange,
                    minRating = minRating,
                    onRatingChange = onRatingChange,
                    matchedMovies = eligibleMovies,
                    isExactMatch = isExactMatch,
                    onResetFilters = onResetAdvancedFilters,
                    modifier = Modifier.testTag("advanced_movie_filter_component")
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            // 1. FAST MOOD PICKER
            item {
                Text(
                    text = "1. Bu Akşamki Modumuz Ne?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // List of Moods
            items(Mood.values().toList()) { mood ->
                val isSelected = activeMoods.contains(mood.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onToggleMood(mood.id, !isSoloMode && editingPartner) }
                        .border(
                            1.5.dp,
                            if (isSelected) PopcornGold else CinemaBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("mood_card_${mood.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CinemaTheme.colors.selectedChipFill else CinemaSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = mood.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mood.label,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PopcornGold else TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mood.description,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(PopcornGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçili",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Platforms (Always accessible for both modes)
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isAdvancedFilterMode) "Platform Filtresi (Açık Olanlar)" else "2. Hangi Platformlar Açık?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Platform Chips in FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AvailablePlatforms.forEach { platform ->
                    val isSelected = activePlatforms.contains(platform.name)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onTogglePlatform(platform.name, !isSoloMode && editingPartner) }
                            .background(if (isSelected) CinemaSurfaceVariant else CinemaSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) PopcornGold else CinemaBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("platform_chip_${platform.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(platform.badgeColorHex),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = platform.iconName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = platform.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PopcornGold else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Türkçe Dublaj / Altyazı Seçeneği Kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (selectedAudioOptions.isNotEmpty()) ElectricCyan else CinemaBorder,
                        RoundedCornerShape(18.dp)
                    )
                    .testTag("audio_filter_section"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎧 Dil & Altyazı Seçeneği",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (selectedAudioOptions.isEmpty()) "Tümü açık" else "${selectedAudioOptions.size} seçenek",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedAudioOptions.isEmpty()) TextMuted else ElectricCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Filmleri Türkçe dublaj veya Türkçe altyazı tercihine göre filtrele",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isAllAudio = selectedAudioOptions.isEmpty()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onClearAudioOptions() }
                                .background(if (isAllAudio) CinemaTheme.colors.selectedChipFill else CinemaSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isAllAudio) ElectricCyan else CinemaBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("audio_chip_all")
                        ) {
                            Text(
                                text = "✨ Tümü",
                                fontSize = 12.sp,
                                fontWeight = if (isAllAudio) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllAudio) ElectricCyan else TextSecondary
                            )
                        }

                        AudioLanguageOption.entries.forEach { option ->
                            val selected = selectedAudioOptions.contains(option.id)
                            val emoji = if (option == AudioLanguageOption.DUBBED) "🎙️" else "💬"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onToggleAudioOption(option.id) }
                                    .background(if (selected) CinemaTheme.colors.selectedChipFill else CinemaSurfaceVariant)
                                    .border(
                                        1.2.dp,
                                        if (selected) PopcornGold else CinemaBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("audio_chip_${option.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "$emoji ${option.label}", fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) PopcornGold else TextPrimary)
                                    if (selected) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seçili",
                                            tint = PopcornGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Diğer: Kült & Çok İzlenen Filmler (Platformlarda Olmasa da Göster)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(PopcornGold.copy(alpha = 0.65f), NeonPurple.copy(alpha = 0.65f))),
                        RoundedCornerShape(18.dp)
                    )
                    .testTag("other_cult_category_section"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "🏛️ Diğer: Kült & Çok İzlenenler",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PopcornGold
                            )
                            Text(
                                text = "Platformlarda yer almasa da tüm zamanların efsane yapımları",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            color = PopcornGold.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${cultOtherMovies.size} Film",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PopcornGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (cultOtherMovies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(cultOtherMovies, key = { "cult_${it.id}" }) { movie ->
                                Card(
                                    modifier = Modifier
                                        .width(126.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onQuickSelectCultMovie(movie) }
                                        .border(1.dp, CinemaBorder, RoundedCornerShape(14.dp))
                                        .testTag("cult_movie_card_${movie.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = CinemaSurfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        MoviePosterImage(
                                            title = movie.title,
                                            posterPath = movie.posterPath,
                                            posterUrl = movie.posterUrl,
                                            accentColorHex = movie.colorHex,
                                            cornerRadius = 10.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(124.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = movie.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${movie.year} • ★${movie.imdb}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PopcornGold
                                        )
                                        Text(
                                            text = if (movie.platforms.contains("Diğer")) "Platformsuz • Kült" else movie.platforms.firstOrNull().orEmpty(),
                                            fontSize = 9.sp,
                                            color = ElectricCyan,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onSpinCultOtherClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaTheme.colors.selectedChipFill),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PopcornGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("spin_cult_other_button")
                    ) {
                        Text(
                            text = "Kült Seçkiyi Çarka Al 🎡",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold
                        )
                    }
                }
            }

            // Expandable Quick-Mode Narrowing Panel (Tür • Yıl • Puan)
            if (!isAdvancedFilterMode) {
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            1.dp,
                            if (activeNarrowingCount > 0 || showQuickNarrowFilters) NeonPurple else CinemaBorder,
                            RoundedCornerShape(18.dp)
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showQuickNarrowFilters = !showQuickNarrowFilters }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("quick_narrow_filters_card"),
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
                                        .background(CinemaTheme.colors.selectedChipFill, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Seçimi daralt",
                                        tint = PopcornGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Seçimi Daralt: Tür • Yıl • Puan",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        if (activeNarrowingCount > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = PopcornGold,
                                                shape = CircleShape
                                            ) {
                                                Text(
                                                    text = "$activeNarrowingCount",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (narrowingSummaryParts.isNotEmpty()) {
                                            narrowingSummaryParts.joinToString(" • ")
                                        } else {
                                            "Opsiyonel: Havuzu tür, dönem veya puanla daralt"
                                        },
                                        fontSize = 11.sp,
                                        color = if (narrowingSummaryParts.isNotEmpty()) ElectricCyan else TextSecondary
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (showQuickNarrowFilters) {
                                    Icons.Default.KeyboardArrowUp
                                } else {
                                    Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = if (showQuickNarrowFilters) "Daraltma panelini kapat" else "Daraltma panelini aç",
                                tint = PopcornGold
                            )
                        }

                        AnimatedVisibility(
                            visible = showQuickNarrowFilters,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp)) {
                                MovieFilterComponent(
                                    selectedGenres = selectedGenres,
                                    onGenreToggle = onGenreToggle,
                                    onClearGenres = onClearGenres,
                                    selectedYearOptionId = selectedYearOptionId,
                                    minYear = minYear,
                                    maxYear = maxYear,
                                    onYearOptionSelected = onYearOptionSelected,
                                    onYearRangeChange = onYearRangeChange,
                                    minRating = minRating,
                                    onRatingChange = onRatingChange,
                                    matchedMovies = eligibleMovies,
                                    isExactMatch = isExactMatch,
                                    onResetFilters = onResetAdvancedFilters,
                                    modifier = Modifier.testTag("quick_inline_movie_filter_component")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Real-Time Intersection / Wheel Pool Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                CinemaTheme.colors.heroGradientStart,
                                CinemaTheme.colors.heroGradientEnd
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(NeonPurple, ElectricCyan)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAdvancedFilterMode) "FİLTRELENEN HAVUZ"
                                else if (isSoloMode) "HAZIR HAVUZ" else "ORTAK KESİŞİM KÜMESİ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${eligibleMovies.size} Film Çarka Eklendi 🎡",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PopcornGold,
                            modifier = Modifier.testTag("pool_summary_count")
                        )
                        Text(
                            text = when {
                                !isExactMatch -> "Tam eşleşme yok; en yakın öneriler seçildi"
                                narrowingSummaryParts.isNotEmpty() -> narrowingSummaryParts.joinToString(" • ")
                                isAdvancedFilterMode -> "Kriterlerinize tam uyan seçki"
                                else -> "Kavgasız, adil ve sürprizli!"
                            } + if (watchedMovieIds.isNotEmpty()) " • ${watchedMovieIds.size} izlenen hariç" else "",
                            fontSize = 11.sp,
                            color = if (!isExactMatch) PopcornGold else TextSecondary
                        )
                    }

                    Button(
                        onClick = onProceedToWheel,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("launch_wheel_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Çarkı Aç 🎯",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
