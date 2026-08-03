package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TealAccent

enum class StimulerNavDestination(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", "home", Icons.Filled.Home, Icons.Outlined.Home),
    PRACTICE("Practice", "practice", Icons.Filled.Mic, Icons.Outlined.Mic),
    VOCABULARY("Vocab Vault", "vocab", Icons.Filled.Book, Icons.Outlined.Book),
    ANALYTICS("Analytics", "analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics)
}

@Composable
fun StimulerBottomNavBar(
    currentRoute: String,
    onNavigate: (StimulerNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("stimuler_bottom_nav_bar"),
        containerColor = SurfaceDark,
        windowInsets = WindowInsets.navigationBars
    ) {
        StimulerNavDestination.entries.forEach { destination ->
            val isSelected = currentRoute == destination.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.title
                    )
                },
                label = { Text(text = destination.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = PurplePrimary,
                    indicatorColor = PurplePrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
