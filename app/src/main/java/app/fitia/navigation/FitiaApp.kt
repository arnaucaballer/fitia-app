package app.fitia.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.fitia.design.FitiaMotion
import app.fitia.design.FitiaNavShape
import app.fitia.design.FitiaPill
import app.fitia.design.fitiaPress
import kotlin.math.roundToInt
import app.fitia.feature.chat.ui.ChatScreen
import app.fitia.feature.nutrition.ui.NutritionScreen
import app.fitia.feature.settings.ui.SettingsScreen
import app.fitia.feature.training.ui.TrainingScreen

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
                enter = fadeIn(FitiaMotion.effects()),
                exit = fadeOut(FitiaMotion.effects()),
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
    val progress = animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = FitiaMotion.effects(),
        label = "nav",
    )
    val amount = progress.value
    val background = lerp(Color.Transparent, MaterialTheme.colorScheme.primary, amount)
    val content = lerp(
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.onPrimary,
        amount,
    )

    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .fitiaPress()
            .clip(FitiaPill)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = if (selected) null else label,
            tint = content,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val width = (placeable.width * amount).roundToInt()
                    layout(width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
                .padding(start = 8.dp)
                .then(if (selected) Modifier else Modifier.clearAndSetSemantics {}),
        )
    }
}
