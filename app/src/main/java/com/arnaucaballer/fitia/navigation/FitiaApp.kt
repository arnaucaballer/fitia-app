package com.arnaucaballer.fitia.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arnaucaballer.fitia.design.FitiaNavShape
import com.arnaucaballer.fitia.design.FitiaPill
import com.arnaucaballer.fitia.feature.chat.ui.ChatScreen
import com.arnaucaballer.fitia.feature.nutrition.ui.NutritionScreen
import com.arnaucaballer.fitia.feature.settings.ui.SettingsScreen
import com.arnaucaballer.fitia.feature.training.ui.TrainingScreen
import kotlin.math.roundToInt

private val pillSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium,
)

private val tabSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

@Composable
fun FitiaApp() {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var selectedRoute by rememberSaveable { mutableStateOf(FitiaDestination.Chat.route) }
    val selectedTab = FitiaDestination.entries.first { it.route == selectedRoute }
    val openedTabs = remember { mutableStateListOf(selectedTab) }
    var slideForward by remember { mutableStateOf(true) }
    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

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
                            slideForward = destination.ordinal > selectedTab.ordinal
                            if (destination !in openedTabs) openedTabs.add(destination)
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
            openedTabs.forEach { destination ->
                key(destination.route) {
                val selected = destination == selectedTab
                TabLayer(
                    selected = selected,
                    slideForward = slideForward,
                    startVisible = openedTabs.size == 1,
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(if (selected) 1f else 0f)
                        .then(if (selected) Modifier else Modifier.clearAndSetSemantics {}),
                ) {
                    when (destination) {
                        FitiaDestination.Training -> TrainingScreen(Modifier.fillMaxSize())
                        FitiaDestination.Chat -> {
                            ChatScreen(
                                onOpenSettings = { showSettings = true },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        FitiaDestination.Nutrition -> NutritionScreen(Modifier.fillMaxSize())
                    }
                }
                }
            }
            AnimatedVisibility(
                visible = showSettings,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f),
                enter = fadeIn(tabSpring),
                exit = fadeOut(tabSpring),
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
private fun TabLayer(
    selected: Boolean,
    slideForward: Boolean,
    startVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var target by remember { mutableFloatStateOf(if (startVisible) 1f else 0f) }
    LaunchedEffect(selected) {
        target = if (selected) 1f else 0f
    }
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tabSpring,
        label = "tabProgress",
    )
    val slidePx = with(LocalDensity.current) { 28.dp.toPx() }
    Box(
        modifier = modifier.graphicsLayer {
            val amount = progress.coerceIn(0f, 1f)
            alpha = amount
            val sign = if (slideForward) 1f else -1f
            translationX = (1f - amount) * (if (selected) sign else -sign) * slidePx
        },
    ) {
        content()
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
    val progress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = pillSpring,
        label = "navItem",
    )
    val amount = progress.coerceIn(0f, 1f)
    val backgroundColor = lerp(
        Color.Transparent,
        MaterialTheme.colorScheme.primary,
        amount,
    )
    val contentColor = lerp(
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.onPrimary,
        amount,
    )

    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(FitiaPill)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.semantics { contentDescription = label }
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = if (selected) null else label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .padding(start = 8.dp * amount)
                .clipToBounds()
                .graphicsLayer { alpha = amount }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val width = (placeable.width * amount).roundToInt()
                    layout(width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                },
        )
    }
}
