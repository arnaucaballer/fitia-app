package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaCard
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionCalendarScreen(
    onBack: () -> Unit,
    onSelectDay: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: NutritionCalendarViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            NutritionCalendarViewModel(repository = c.nutritionRepository)
        },
    )
    val daysWithFood by viewModel.daysWithFood.collectAsStateWithLifecycle()
    val daysSet = remember(daysWithFood) { daysWithFood.toSet() }
    var month by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val locale = Locale.getDefault()
    val monthLabel = month.month.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() } +
        " ${month.year}"

    val cells = remember(month) { buildMonthCells(month) }
    val recentDays = remember(daysWithFood) {
        daysWithFood.take(14).map { LocalDate.ofEpochDay(it) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nutrition_calendar)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.nutrition_calendar_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            item {
                FitiaCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { month = month.minusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                        }
                        Text(monthLabel, style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { month = month.plusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        DayOfWeek.entries.forEach { dow ->
                            Text(
                                text = dow.getDisplayName(TextStyle.NARROW, locale),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }

                    cells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            week.forEach { date ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                date == null -> MaterialTheme.colorScheme.background
                                                date.toEpochDay() in daysSet -> MaterialTheme.colorScheme.primary
                                                date == today -> MaterialTheme.colorScheme.secondaryContainer
                                                else -> MaterialTheme.colorScheme.surface
                                            },
                                        )
                                        .then(
                                            if (date != null) {
                                                Modifier.clickable { onSelectDay(date.toEpochDay()) }
                                            } else {
                                                Modifier
                                            },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (date != null) {
                                        val onAccent = date.toEpochDay() in daysSet
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            color = when {
                                                onAccent -> MaterialTheme.colorScheme.onPrimary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.nutrition_calendar_recent),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            if (recentDays.isEmpty()) {
                item {
                    FitiaCard {
                        Text(
                            text = stringResource(R.string.nutrition_calendar_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(recentDays, key = { it.toEpochDay() }) { date ->
                    FitiaCard(onClick = { onSelectDay(date.toEpochDay()) }) {
                        Text(
                            text = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(date),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.nutrition_calendar_open_day),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun buildMonthCells(month: YearMonth): List<LocalDate?> {
    val first = month.atDay(1)
    val mondayBasedLead = first.dayOfWeek.value - 1
    val days = month.lengthOfMonth()
    val cells = MutableList(mondayBasedLead) { null as LocalDate? }
    for (d in 1..days) {
        cells += month.atDay(d)
    }
    while (cells.size % 7 != 0) {
        cells += null
    }
    return cells
}
