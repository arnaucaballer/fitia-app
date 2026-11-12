package app.fitia.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import app.fitia.design.FitiaMotion

/**
 * Short spring between pushed screens. The fade is critically damped and the
 * slide is a fraction of the width, so a push stays interruptible without
 * holding two full screens in the frame budget for the old default crossfade.
 */
@Composable
fun FitiaNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(FitiaMotion.effects()) + slideInHorizontally(FitiaMotion.slide) { it / 5 }
        },
        exitTransition = {
            fadeOut(FitiaMotion.effects()) + slideOutHorizontally(FitiaMotion.slide) { -it / 8 }
        },
        popEnterTransition = {
            fadeIn(FitiaMotion.effects()) + slideInHorizontally(FitiaMotion.slide) { -it / 8 }
        },
        popExitTransition = {
            fadeOut(FitiaMotion.effects()) + slideOutHorizontally(FitiaMotion.slide) { it / 5 }
        },
        builder = builder,
    )
}
