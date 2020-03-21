package com.anatawa12.jasm.parser

class LexingError(val error: String, val line: Int, val column: Int) : Exception("($line, $column): $error") {
}
