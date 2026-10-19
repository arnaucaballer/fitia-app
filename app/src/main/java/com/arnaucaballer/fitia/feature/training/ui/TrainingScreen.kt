package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FeaturePlaceholder

@Composable
fun TrainingScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        title = stringResource(R.string.training_title),
        body = stringResource(R.string.training_placeholder),
        modifier = modifier,
    )
}
