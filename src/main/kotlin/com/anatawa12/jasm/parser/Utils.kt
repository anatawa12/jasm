package com.anatawa12.jasm.parser

fun Char?.isWardChar()
        = this != null && (this.isJavaIdentifierPart() || this == '<' || this == '>')

fun Char?.isTokenSplitChar()
        = this == null || this.isWhitespace() || this == ':' || this == '.'
