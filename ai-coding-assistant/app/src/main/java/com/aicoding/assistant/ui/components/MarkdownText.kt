package com.aicoding.assistant.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 14,
    tablesEnabled: Boolean = true,
    highlightEnabled: Boolean = true,
    onCopy: (String) -> Unit = {},
) {
    val blocks = MdParser.parse(markdown)
    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEachIndexed { idx, block -> renderBlock(idx, block, fontSize, tablesEnabled, highlightEnabled, onCopy) }
    }
}

@Composable
private fun renderBlock(
    key: Any,
    block: MdParser.Paragraph,
    fontSize: Int,
    tablesEnabled: Boolean,
    highlightEnabled: Boolean,
    onCopy: (String) -> Unit,
) {
    when (block.kind) {
        MdParser.Kind.HEADING -> Text(
            text = MdParser.inline(block.text),
            style = when (block.level) {
                1 -> MaterialTheme.typography.headlineMedium
                2 -> MaterialTheme.typography.headlineSmall
                3 -> MaterialTheme.typography.titleLarge
                else -> MaterialTheme.typography.titleMedium
            },
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
            color = MaterialTheme.colorScheme.onSurface,
        )
        MdParser.Kind.QUOTE -> Text(
            text = MdParser.inline(block.text),
            style = TextStyle(fontSize = (fontSize - 1).sp, fontStyle = FontStyle.Italic),
            modifier = Modifier
                .padding(vertical = 2.dp)
                .padding(start = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MdParser.Kind.LIST -> Column(modifier = Modifier.padding(start = 8.dp)) {
            block.items.forEachIndexed { i, item ->
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = if (block.ordered) "${i + 1}." else "•",
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize.sp,
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = MdParser.inline(item),
                        fontSize = fontSize.sp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        MdParser.Kind.CODE -> {
            CodeBlock(
                code = block.code.orEmpty(),
                language = block.language,
                onCopy = onCopy,
            )
            Spacer(Modifier.height(4.dp))
        }
        MdParser.Kind.TABLE -> {
            if (tablesEnabled && block.rows.firstOrNull()?.isNotEmpty() == true) {
                TableBlock(block.rows, fontSize)
                Spacer(Modifier.height(4.dp))
            } else {
                Text(
                    text = MdParser.inline(block.text),
                    fontSize = fontSize.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        MdParser.Kind.PARAGRAPH -> Text(
            text = MdParser.inline(block.text),
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.4f).sp,
            modifier = Modifier.padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TableBlock(rows: List<List<String>>, fontSize: Int) {
    if (rows.isEmpty()) return
    val headerColor = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.shapes.small)
            .padding(4.dp)
    ) {
        rows.forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .ifTrue(rowIdx == 0) {
                        background(headerColor)
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEachIndexed { colIdx, cell ->
                    Text(
                        text = cell.trim(),
                        fontSize = (fontSize - 1).sp,
                        fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

private fun Modifier.ifTrue(condition: Boolean, then: Modifier.() -> Modifier): Modifier =
    if (condition) then() else this