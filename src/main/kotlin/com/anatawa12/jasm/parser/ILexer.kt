package com.anatawa12.jasm.parser

import com.anatawa12.jasm.tree.Token

interface ILexer {
    fun isNext(expect: Set<TokenType<*>>): Boolean = expect.any { isNext(it) }
    fun isNext(expect: TokenType<*>): Boolean
    fun <T: Any> readToken(expect: TokenType<T>): Token<T>

    fun unexpectTokenError(vararg expects: TokenType<*>): Nothing = unexpectTokenError(setOf(*expects))
    fun unexpectTokenError(vararg expects: Set<TokenType<*>>): Nothing = unexpectTokenErrorInternal(expects.flatMapTo(mutableSetOf()) { it })
    fun unexpectTokenErrorInternal(expects: Set<TokenType<*>>): Nothing
}
