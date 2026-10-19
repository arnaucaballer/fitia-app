package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FeaturePlaceholder

@Composable
fun NutritionScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        title = stringResource(R.string.nutrition_title),
        body = stringResource(R.string.nutrition_placeholder),
        modifier = modifier,
    )
}
