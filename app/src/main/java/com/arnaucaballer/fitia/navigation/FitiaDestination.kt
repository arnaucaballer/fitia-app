package com.arnaucaballer.fitia.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.arnaucaballer.fitia.R

enum class FitiaDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Training(
        route = "training",
        labelRes = R.string.nav_training,
        icon = Icons.Outlined.FitnessCenter,
    ),
    Chat(
        route = "chat",
        labelRes = R.string.nav_chat,
        icon = Icons.Outlined.ChatBubbleOutline,
    ),
    Nutrition(
        route = "nutrition",
        labelRes = R.string.nav_nutrition,
        icon = Icons.Outlined.Restaurant,
    ),
}
