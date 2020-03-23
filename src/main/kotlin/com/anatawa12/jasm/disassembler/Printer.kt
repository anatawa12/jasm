package com.anatawa12.jasm.disassembler

import com.anatawa12.jasm.parser.isWardChar
import java.lang.StringBuilder

class Printer() {
    private val file = StringBuilder()
    private var printSpace = false
    private var afterNewLine = false
    private val indentStr = "   "
    private var indent = 0

    private inline fun m(function: () -> Unit) = apply {
        if (afterNewLine)
            repeat(indent) { file.append(indentStr) }
        afterNewLine = false
        if (printSpace) file.append(' ')
        printSpace = false
        function()
    }

    fun indent() = apply { indent++ }
    fun outdent() = apply { indent-- }

    fun nl() {
        file.append('\n')
        printSpace = false
        afterNewLine = true
    }

    fun key(s: String) = m {
        file.append(s)
        printSpace = true
    }

    fun c(s: Char) = apply {
        file.append(s)
        printSpace = false
        afterNewLine = false
    }

    fun str(str: String) = m {
        file.append('"')
        for (c in str) {
            when (c) {
                '\t' -> file.append("\\t")
                '\b' -> file.append("\\b")
                '\n' -> file.append("\\n")
                '\r' -> file.append("\\r")
                '\'' -> file.append("\\'")
                '\"' -> file.append("\\\"")
                '\\' -> file.append("\\\\")
                '\$' -> file.append("\\$")
                else -> when (c.category) {
                    CharCategory.UPPERCASE_LETTER,
                    CharCategory.LOWERCASE_LETTER,
                    CharCategory.TITLECASE_LETTER,
                    CharCategory.MODIFIER_LETTER,
                    CharCategory.OTHER_LETTER,
                    CharCategory.DECIMAL_DIGIT_NUMBER,
                    CharCategory.LETTER_NUMBER,
                    CharCategory.OTHER_NUMBER,
                    CharCategory.SPACE_SEPARATOR,
                    CharCategory.DASH_PUNCTUATION,
                    CharCategory.START_PUNCTUATION,
                    CharCategory.END_PUNCTUATION,
                    CharCategory.CONNECTOR_PUNCTUATION,
                    CharCategory.OTHER_PUNCTUATION,
                    CharCategory.MATH_SYMBOL,
                    CharCategory.CURRENCY_SYMBOL,
                    CharCategory.MODIFIER_SYMBOL,
                    CharCategory.OTHER_SYMBOL,
                    CharCategory.INITIAL_QUOTE_PUNCTUATION,
                    CharCategory.FINAL_QUOTE_PUNCTUATION -> {
                        file.append("$c")
                    }

                    CharCategory.NON_SPACING_MARK,
                    CharCategory.ENCLOSING_MARK,
                    CharCategory.COMBINING_SPACING_MARK,
                    CharCategory.LINE_SEPARATOR,
                    CharCategory.PARAGRAPH_SEPARATOR,
                    CharCategory.CONTROL,
                    CharCategory.FORMAT,
                    CharCategory.PRIVATE_USE,
                    CharCategory.SURROGATE,
                    CharCategory.UNASSIGNED -> {
                        file.append("\\u${c.toInt().toString(16).padStart(4, '0')}")
                    }
                }
            }
        }
        file.append('"')
        printSpace = true
    }

    // including internal name
    fun name(name: String) = m {
        if (name.all { it.isWardChar() || it == '/' || it == ';' })
            key(name)
        else str(name)
    }

    fun mDesc(name: String) = m {
        if (name.all { it.isWardChar() || it == '/' || it == ';' || it == '(' || it == ')'})
            key(name)
        else str(name)
    }

    fun getString(): String = file.toString()
}
