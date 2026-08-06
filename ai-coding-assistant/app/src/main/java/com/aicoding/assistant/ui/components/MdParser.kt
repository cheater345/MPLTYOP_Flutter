package com.aicoding.assistant.ui.components

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle

object MdParser {

    enum class Kind { HEADING, QUOTE, LIST, CODE, TABLE, PARAGRAPH }

    data class Paragraph(
        val kind: Kind,
        val text: String = "",
        val level: Int = 0,
        val items: List<String> = emptyList(),
        val ordered: Boolean = false,
        val code: String? = null,
        val language: String? = null,
        val rows: List<List<String>> = emptyList(),
    )

    fun parse(md: String): List<Paragraph> {
        val lines = md.replace("\r\n", "\n").split("\n")
        val out = mutableListOf<Paragraph>()
        var i = 0

        fun inlinePieces(text: String): Paragraph = Paragraph(Kind.PARAGRAPH, text)

        while (i < lines.size) {
            val line = lines[i]

            // fenced code block
            val fence = Regex("^```(\\w*)").find(line.trim())
            if (fence != null) {
                val lang = fence.groupValues[1]
                val buf = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    buf.appendLine(lines[i])
                    i++
                }
                i++
                out += Paragraph(Kind.CODE, code = buf.toString().trimEnd(), language = lang.ifEmpty { null })
                continue
            }

            // heading
            val heading = Regex("^(#{1,6})\\s+(.*)").find(line)
            if (heading != null) {
                out += Paragraph(Kind.HEADING, heading.groupValues[2], level = heading.groupValues[1].length)
                i++
                continue
            }

            // blockquote
            if (line.trimStart().startsWith(">")) {
                val buf = StringBuilder()
                while (i < lines.size && lines[i].trimStart().startsWith(">")) {
                    buf.appendLine(lines[i].trimStart().removePrefix(">").trim())
                    i++
                }
                out += Paragraph(Kind.QUOTE, buf.toString().trim())
                continue
            }

            // list
            val listItem = Regex("^\\s*[-*+]\\s+(.*)").find(line)
            val orderedItem = Regex("^\\s*\\d+[.)]\\s+(.*)").find(line)
            if (listItem != null || orderedItem != null) {
                val ordered = orderedItem != null
                val items = mutableListOf<String>()
                if (listItem != null) items += listItem.groupValues[1] else items += orderedItem!!.groupValues[1]
                i++
                while (i < lines.size) {
                    val li = Regex("^\\s*[-*+]\\s+(.*)").find(lines[i])
                    val oi = Regex("^\\s*\\d+[.)]\\s+(.*)").find(lines[i])
                    if (li != null) items += li.groupValues[1]
                    else if (oi != null) items += oi.groupValues[1]
                    else break
                    i++
                }
                out += Paragraph(Kind.LIST, items = items, ordered = ordered)
                continue
            }

            // table
            if (line.trim().contains("|")) {
                val rows = mutableListOf<List<String>>()
                while (i < lines.size && lines[i].trim().contains("|")) {
                    val cells = lines[i].trim()
                        .removePrefix("|").removeSuffix("|")
                        .split("|").map { it.trim() }
                    rows += cells
                    i++
                }
                // drop separator row like |---|---|
                if (rows.size >= 2 && rows[1].all { it.replace("-", "").replace(":", "").isBlank() }) {
                    rows.removeAt(1)
                }
                if (rows.isNotEmpty()) {
                    out += Paragraph(Kind.TABLE, text = line, rows = rows)
                    continue
                }
            }

            // blank line
            if (line.isBlank()) {
                i++
                continue
            }

            // plain paragraph: merge consecutive non-blank lines
            val buf = StringBuilder(line)
            i++
            while (i < lines.size) {
                val next = lines[i]
                if (next.isBlank() || next.trimStart().startsWith("```") ||
                    next.trimStart().startsWith("#") || next.trimStart().startsWith(">") ||
                    Regex("^\\s*[-*+]\\s+").matches(next) || Regex("^\\s*\\d+[.)]\\s+").matches(next) ||
                    next.trim().contains("|")
                ) break
                buf.append(' ').append(next.trim())
                i++
            }
            out += Paragraph(Kind.PARAGRAPH, buf.toString())
        }
        return out
    }

    /** Builds an AnnotatedString with inline formatting: **bold**, *italic*, `code`, ~~strike~~, [text](url) */
    fun inline(text: String): AnnotatedString = buildAnnotatedString {
        var i = 0
        var plain = StringBuilder()
        fun flush() {
            if (plain.isNotEmpty()) {
                append(plain.toString())
                plain = StringBuilder()
            }
        }
        while (i < text.length) {
            val c = text[i]
            when {
                c == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end > i) {
                        flush()
                        withStyle(SpanStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, background = androidx.compose.ui.graphics.Color(0x33FFFFFF))) {
                            append(text.substring(i + 1, end))
                        }
                        i = end
                    } else {
                        plain.append(c)
                    }
                }
                c == '*' && i + 1 < text.length && text[i + 1] == '*' -> {
                    val end = text.indexOf("**", i + 2)
                    if (end > i) {
                        flush()
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 1
                    } else plain.append(c)
                }
                c == '*' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end > i && text.getOrNull(end - 1) != '*') {
                        flush()
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end
                    } else plain.append(c)
                }
                c == '~' && i + 1 < text.length && text[i + 1] == '~' -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end > i) {
                        flush()
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 1
                    } else plain.append(c)
                }
                c == '[' -> {
                    val close = text.indexOf(']', i + 1)
                    if (close > i && close + 1 < text.length && text[close + 1] == '(') {
                        val endParen = text.indexOf(')', close + 2)
                        if (endParen > close) {
                            flush()
                            withStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = androidx.compose.ui.graphics.Color(0xFF60A5FA))) {
                                append(text.substring(i + 1, close))
                            }
                            i = endParen
                        } else plain.append(c)
                    } else plain.append(c)
                }
                c == '$' -> {
                    // LaTeX inline: render as monospace
                    val end = text.indexOf('$', i + 1)
                    if (end > i) {
                        flush()
                        withStyle(SpanStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontStyle = FontStyle.Italic)) {
                            append(text.substring(i, end + 1))
                        }
                        i = end
                    } else plain.append(c)
                }
                else -> plain.append(c)
            }
            i++
        }
        flush()
    }
}