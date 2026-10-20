package com.arnaucaballer.fitia.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Lightweight Markdown renderer for chat: headers, lists, bold, italic, code, links.
 */
@Composable
fun MarkdownText(
    markdown: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }
    SelectionContainer {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            blocks.forEach { block ->
                when (block) {
                    is MdBlock.Heading -> Text(
                        text = inlineMarkdown(block.text, color),
                        color = color,
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.titleLarge
                            2 -> MaterialTheme.typography.titleMedium
                            else -> MaterialTheme.typography.titleSmall
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                    is MdBlock.Bullet -> Text(
                        text = buildAnnotatedString {
                            append("• ")
                            append(inlineMarkdown(block.text, color))
                        },
                        color = color,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                    is MdBlock.Numbered -> Text(
                        text = buildAnnotatedString {
                            append("${block.index}. ")
                            append(inlineMarkdown(block.text, color))
                        },
                        color = color,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                    is MdBlock.Code -> Text(
                        text = block.text,
                        color = color,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                    )
                    is MdBlock.Paragraph -> Text(
                        text = inlineMarkdown(block.text, color),
                        color = color,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

private sealed class MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock()
    data class Bullet(val text: String) : MdBlock()
    data class Numbered(val index: Int, val text: String) : MdBlock()
    data class Code(val text: String) : MdBlock()
    data class Paragraph(val text: String) : MdBlock()
}

private fun parseMarkdownBlocks(raw: String): List<MdBlock> {
    val lines = raw.replace("\r\n", "\n").split('\n')
    val blocks = mutableListOf<MdBlock>()
    val paragraph = StringBuilder()
    var i = 0
    fun flushParagraph() {
        val text = paragraph.toString().trim()
        if (text.isNotEmpty()) blocks += MdBlock.Paragraph(text)
        paragraph.clear()
    }
    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()
        when {
            trimmed.startsWith("```") -> {
                flushParagraph()
                i++
                val code = StringBuilder()
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    if (code.isNotEmpty()) code.append('\n')
                    code.append(lines[i])
                    i++
                }
                blocks += MdBlock.Code(code.toString())
            }
            trimmed.matches(Regex("^#{1,3}\\s+.+")) -> {
                flushParagraph()
                val level = trimmed.takeWhile { it == '#' }.length
                blocks += MdBlock.Heading(level, trimmed.drop(level).trim())
            }
            trimmed.matches(Regex("^[-*]\\s+.+")) -> {
                flushParagraph()
                blocks += MdBlock.Bullet(trimmed.drop(2).trim())
            }
            trimmed.matches(Regex("^\\d+\\.\\s+.+")) -> {
                flushParagraph()
                val dot = trimmed.indexOf('.')
                val index = trimmed.take(dot).toIntOrNull() ?: 1
                blocks += MdBlock.Numbered(index, trimmed.substring(dot + 1).trim())
            }
            trimmed.isEmpty() -> flushParagraph()
            else -> {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(trimmed)
            }
        }
        i++
    }
    flushParagraph()
    return blocks.ifEmpty { listOf(MdBlock.Paragraph(raw)) }
}

private fun inlineMarkdown(text: String, baseColor: Color): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) || text.startsWith("__", i) -> {
                val marker = text.substring(i, i + 2)
                val end = text.indexOf(marker, i + 2)
                if (end > i) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            text.startsWith("`", i) -> {
                val end = text.indexOf('`', i + 1)
                if (end > i) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = baseColor.copy(alpha = 0.12f),
                            color = baseColor,
                        ),
                    ) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            text.startsWith("*", i) || text.startsWith("_", i) -> {
                val marker = text[i]
                val end = text.indexOf(marker, i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            text.startsWith("[", i) -> {
                val close = text.indexOf(']', i)
                val openParen = if (close >= 0) text.indexOf("](", close) else -1
                val closeParen = if (openParen >= 0) text.indexOf(')', openParen + 2) else -1
                if (close > i && openParen == close && closeParen > openParen) {
                    val label = text.substring(i + 1, close)
                    withStyle(
                        SpanStyle(
                            color = baseColor,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium,
                        ),
                    ) {
                        append(label)
                    }
                    i = closeParen + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}
