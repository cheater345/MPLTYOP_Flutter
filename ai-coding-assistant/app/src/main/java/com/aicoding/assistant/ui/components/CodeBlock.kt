package com.aicoding.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeBlock(
    code: String,
    language: String?,
    modifier: Modifier = Modifier,
    onCopy: (String) -> Unit = {},
) {
    val bg = Color(0xFF12141C)
    val borderColor = Color(0xFF23263A)
    val label = if (language.isNullOrBlank()) "code" else language
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, MaterialTheme.shapes.medium)
            .border(1.dp, borderColor, MaterialTheme.shapes.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label + if (SyntaxHighlighter.isRegistered(label)) "" else "",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B8FAE),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onCopy(code) }, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color(0xFF8B8FAE),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val highlighted = buildAnnotatedString {
                val ranges = SyntaxHighlighter.highlight(code, label)
                var last = 0
                for ((range, color) in ranges) {
                    append(code.substring(last, range.first))
                    withStyle(SpanStyle(color = Color(color))) {
                        append(code.substring(range.first, range.last + 1))
                    }
                    last = range.last + 1
                }
                append(code.substring(last))
            }
            Text(
                text = highlighted,
                style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                color = Color(0xFFD4D4D4),
            )
        }
    }
}