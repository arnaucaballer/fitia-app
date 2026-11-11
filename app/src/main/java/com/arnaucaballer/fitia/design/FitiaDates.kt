package com.arnaucaballer.fitia.design

import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

internal val fullDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

internal val mediumDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

internal val shortTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

private val mediumDateTimeFormatter: DateFormat =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

internal fun formatDateTime(epochMs: Long): String = mediumDateTimeFormatter.format(Date(epochMs))
