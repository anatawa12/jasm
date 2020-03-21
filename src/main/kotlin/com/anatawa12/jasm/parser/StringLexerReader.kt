package com.anatawa12.jasm.parser

import com.anatawa12.jasm.tree.CharPosition

class StringLexerReader(private val string: String) : Lexer.Reader {
    private var index = 0

    private var line = 1
    private var column = 1

    override val charPosition: CharPosition
        get() = CharPosition(index, line, column)

    override fun setCurrentTo(pos: Int) {
        val subStringEnd = minOf(index + pos, string.length)
        val lines = string.substring(minOf(index, subStringEnd), subStringEnd).lines()
        line += lines.count() - 1
        if (lines.count() == 1)
            column += lines.last().length
        else
            column = lines.last().length + 1
        index += pos
    }

    override fun getOrNull(pos: Int): Char? = string.getOrNull(index + pos)
    override fun error(message: String): Nothing = throw LexingError(
        message,
        line,
        column
    )
}
