package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.Screen

private data class BottomTab(val screen: Screen, val label: String, val icon: ImageVector, val tag: String)

private val tabs = listOf(
    BottomTab(Screen.HOME, "Ana Sayfa", Icons.Default.Home, "bottom_nav_home"),
    BottomTab(Screen.UPCOMING, "Yakında", Icons.Default.Event, "bottom_nav_upcoming"),
    BottomTab(Screen.POPULAR, "Popüler", Icons.Default.LocalFireDepartment, "bottom_nav_popular"),
    BottomTab(Screen.SERIES, "Diziler", Icons.Default.Tv, "bottom_nav_series"),
    BottomTab(Screen.FORUM, "Forum", Icons.AutoMirrored.Filled.Chat, "bottom_nav_forum")
)

/** Alt menü: Ana Sayfa, Yakında, Popüler, Diziler, Forum. */
@Composable
fun MainBottomBar(current: Screen, onSelect: (Screen) -> Unit) {
    NavigationBar(containerColor = CinemaSurface) {
        tabs.forEach { tab ->
            val selected = current == tab.screen
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onSelect(tab.screen) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = {
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PopcornGold,
                    selectedTextColor = PopcornGold,
                    indicatorColor = PopcornGold.copy(alpha = 0.16f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = androidx.compose.ui.Modifier.testTag(tab.tag)
            )
        }
    }
}
