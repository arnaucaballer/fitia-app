package com.arnaucaballer.fitia.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arnaucaballer.fitia.design.FitiaNavShape
import com.arnaucaballer.fitia.design.FitiaPill
import com.arnaucaballer.fitia.feature.chat.ui.ChatScreen
import com.arnaucaballer.fitia.feature.nutrition.ui.NutritionScreen
import com.arnaucaballer.fitia.feature.settings.ui.SettingsScreen
import com.arnaucaballer.fitia.feature.training.ui.TrainingScreen

@Composable
fun FitiaApp() {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var selectedRoute by rememberSaveable { mutableStateOf(FitiaDestination.Chat.route) }
    val selectedTab = FitiaDestination.entries.first { it.route == selectedRoute }
    var openedTabs by remember { mutableStateOf(listOf(selectedTab)) }
    val openSettings = remember { { showSettings = true } }
    val density = LocalDensity.current
    val ime = WindowInsets.ime
    val keyboardOpen by remember(ime, density) {
        derivedStateOf { ime.getBottom(density) > 0 }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
        bottomBar = {
            if (!showSettings && !keyboardOpen) {
                FitiaBottomNavBar(
                    selectedTab = selectedTab,
                    onNavigate = { destination ->
                        if (destination != selectedTab) {
                            if (destination !in openedTabs) openedTabs = openedTabs + destination
                            selectedRoute = destination.route
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                .fillMaxSize(),
        ) {
            RetainedPages(
                pages = openedTabs,
                selected = selectedTab,
                modifier = Modifier.fillMaxSize(),
                pageKey = { it.route },
            ) { destination ->
                when (destination) {
                    FitiaDestination.Training -> TrainingScreen(Modifier.fillMaxSize())
                    FitiaDestination.Chat -> ChatScreen(
                        onOpenSettings = openSettings,
                        modifier = Modifier.fillMaxSize(),
                    )
                    FitiaDestination.Nutrition -> NutritionScreen(Modifier.fillMaxSize())
                }
            }
            AnimatedVisibility(
                visible = showSettings,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f),
                enter = EnterTransition.None,
                exit = ExitTransition.None,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    SettingsScreen(
                        onBack = { showSettings = false },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun FitiaBottomNavBar(
    selectedTab: FitiaDestination,
    onNavigate: (FitiaDestination) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(FitiaNavShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FitiaDestination.entries.forEach { destination ->
                FitiaNavBarItem(
                    destination = destination,
                    selected = selectedTab == destination,
                    onClick = { onNavigate(destination) },
                )
            }
        }
    }
}

@Composable
private fun FitiaNavBarItem(
    destination: FitiaDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val label = stringResource(destination.labelRes)
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val content = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(FitiaPill)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = if (selected) null else label,
            tint = content,
            modifier = Modifier.size(24.dp),
        )
        if (selected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = content,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
