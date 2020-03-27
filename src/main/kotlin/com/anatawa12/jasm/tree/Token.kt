package com.anatawa12.jasm.tree

class Token<T>(val text: String, val value: T, val begin: CharPosition, val end: CharPosition) : Node() {
    override val tokens: Array<out Token<*>> = arrayOf(this)

    companion object {
        val TEST = Token("", null, CharPosition(0, 0, 0), CharPosition(0, 0, 0))
    }
}

data class CharPosition(val index: Int, val line: Int, val column: Int)
