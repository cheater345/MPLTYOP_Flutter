package com.aicoding.assistant.ui.components

object SyntaxColors {
    val keyword = 0xFFC586C0
    val string = 0xFFCE9178
    val comment = 0xFF6A9955
    val number = 0xFFB5CEA8
    val default = 0xFFD4D4D4
}

object SyntaxHighlighter {

    private data class Rule(val type: Long, val pattern: Regex)

    private val languages: Map<String, List<Rule>> = listOf(
        "kotlin" to listOf(
            Rule(
                0xFFC586C0,
                Regex("""\b(package|import|class|object|interface|fun|val|var|if|else|when|for|while|return|try|catch|finally|throw|null|true|false|this|super|private|protected|public|override|open|data|sealed|enum|companion|by|lazy|suspend|inline|reified|in|is|as|typealias|constructor|init|do|break|continue|out|expect|actual)\b""")
            ),
            Rule(0xFF4EC9B0, Regex("""\b(String|Int|Long|Double|Float|Boolean|Char|Byte|Short|Any|Unit|List|MutableList|Set|Map|Pair|Nothing|Throwable|Exception|CoroutineScope|Job|Flow|Result|Array)\b""")),
            Rule(0xFFDCDCAA, Regex("""\b(println|print|require|check|error|run|let|apply|also|with|takeIf|takeUnless|repeat|main)\b""")),
            Rule(0xFFB5CEA8, Regex("""\b\d+(\.\d+)?\b""")),
            Rule(0xFFCE9178, Regex("""\"([^\"\\]|\\.)*\"""")),
            Rule(0xFF6A9955, Regex("""(//.*)""")),
            Rule(0xFF6A9955, Regex("""/\*[\s\S]*?\*/""")),
        ),
        "java" to listOf(
            Rule(0xFFC586C0, Regex("""\b(public|private|protected|static|final|class|interface|extends|implements|if|else|for|while|do|switch|case|break|continue|return|new|try|catch|finally|throw|throws|void|int|long|double|float|boolean|char|null|true|false|this|super|import|package|abstract|synchronized|volatile|enum|instanceof|default)\b""")),
            Rule(0xFFB5CEA8, Regex("""\b\d+(\.\d+)?\b""")),
            Rule(0xFFCE9178, Regex("""\"([^\"\\]|\\.)*\"""")),
            Rule(0xFF6A9955, Regex("""(//.*)""")),
        ),
        "python" to listOf(
            Rule(0xFFC586C0, Regex("""\b(def|class|import|from|return|if|elif|else|for|while|try|except|finally|with|as|pass|break|continue|lambda|yield|raise|None|True|False|and|or|not|in|is|global|nonlocal|async|await|del)\b""")),
            Rule(0xFFB5CEA8, Regex("""\b\d+(\.\d+)?\b""")),
            Rule(0xFFCE9178, Regex("""("([^"\\]|\\.)*")"""")),
            Rule(0xFFCE9178, Regex("""('([^'\\]|\\.)*')""")),
            Rule(0xFF6A9955, Regex("""(#.*)""")),
        ),
        "javascript" to listOf(
            Rule(0xFFC586C0, Regex("""\b(const|let|var|function|return|if|else|for|while|do|switch|case|break|continue|new|class|extends|super|this|null|undefined|true|false|typeof|instanceof|in|of|yield|async|await|import|from|export|default|try|catch|finally|throw|delete|void)\b""")),
            Rule(0xFFB5CEA8, Regex("""\b\d+(\.\d+)?\b""")),
            Rule(0xFFCE9178, Regex("""("([^"\\]|\\.)*"|'([^'\\]|\\.)*'|`([^`\\]|\\.)*`)""")),
            Rule(0xFF6A9955, Regex("""(//.*)""")),
        ),
        "json" to listOf(
            Rule(0xFFCE9178, Regex("""\"([^\"\\]|\\.)*\"""")),
            Rule(0xFFB5CEA8, Regex("""\b\d+(\.\d+)?\b""")),
            Rule(0xFFC586C0, Regex("""\b(true|false|null)\b""")),
        ),
        "sql" to listOf(
            Rule(0xFFC586C0, Regex("""\b(SELECT|FROM|WHERE|INSERT|INTO|VALUES|UPDATE|SET|DELETE|CREATE|TABLE|DROP|ALTER|JOIN|LEFT|RIGHT|INNER|OUTER|ON|GROUP|BY|ORDER|HAVING|LIMIT|OFFSET|UNION|ALL|AND|OR|NOT|NULL|PRIMARY|KEY|INDEX|VIEW|COUNT|SUM|AVG|MIN|MAX|DISTINCT|AS|LIKE|BETWEEN|IN|EXISTS|CASE|WHEN|THEN|ELSE|END)\b""")),
            Rule(0xFF6A9955, Regex("""(--.*)""")),
        ),
        "shell" to listOf(
            Rule(0xFFC586C0, Regex("""\b(if|then|else|elif|fi|for|while|do|done|case|esac|function|return|break|continue|exit|export|local|echo|cd|ls|mkdir|rm|touch|cat|sudo|apt|brew|git|npm|yarn|npx|docker|pip)\b""")),
            Rule(0xFF6A9955, Regex("""(#.*)""")),
        ),
    ).toMap()

    fun isRegistered(language: String): Boolean = languages.containsKey(language.lowercase())

    /**
     * Returns ranges of matches annotated with their highlight color.
     */
    fun highlight(code: String, language: String): List<Pair<IntRange, Long>> {
        val rules = languages[language.lowercase()] ?: return emptyList()
        val out = mutableListOf<Pair<IntRange, Long>>()
        for (rule in rules) {
            var m = rule.pattern.find(code)
            while (m != null) {
                out += m.range to rule.type
                m = rule.pattern.find(code, m.range.last + 1)
            }
        }
        // Collapse overlapping ranges by longest-match-first
        out.sortByDescending { it.first.count() }
        val result = mutableListOf<Pair<IntRange, Long>>()
        val covered = mutableSetOf<Int>()
        for ((range, color) in out) {
            if (covered.any { it in range }) continue
            for (i in range) covered.add(i)
            result += range to color
        }
        result.sortBy { it.first.first }
        return result
    }
}