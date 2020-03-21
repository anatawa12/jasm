package com.anatawa12.jasm.parser

import com.anatawa12.jasm.tree.BranchNode
import com.anatawa12.jasm.tree.Node
import com.anatawa12.jasm.tree.Token

fun <T : Node>grammar(vararg starts: TokenType<*>, run: Grammar.GrammarScope.() -> T) =
    Grammar({ setOf(*starts) }, run)
fun <T : Node>grammar(startGetter: () -> Set<TokenType<*>>, run: Grammar.GrammarScope.() -> T) =
    Grammar(startGetter, run)

class Grammar<T : Node>(startGetter: () -> Set<TokenType<*>>, val run: GrammarScope.() -> T) {
    val start by lazy(startGetter)

    interface GrammarScope {
        fun <T: Any> ILexer.read(expect: TokenType<T>): T
        operator fun <T : Node> Grammar<T>.invoke(): T

        // utils
        fun <T : Node> Grammar<T>.optional(): T?
        fun <T : Node> Grammar<T>.many(): List<T>
    }
}

abstract class AbstractParser(protected val lex: ILexer) {
    inner class GrammarScopeImpl : Grammar.GrammarScope {
        private val tokens = mutableListOf<Token<*>>()
        override fun <T : Any> ILexer.read(expect: TokenType<T>): T {
            val token = readToken(expect)
            tokens.add(token)
            return token.value
        }

        override fun <T : Node> Grammar<T>.invoke(): T {
            val newScope = GrammarScopeImpl()
            val result = newScope.run()
            tokens += newScope.tokens
            if (result is BranchNode)
                result.tokens = newScope.tokens.toTypedArray()
            return result
        }

        // utils
        override fun <T : Node> Grammar<T>.optional() = if (lex.isNext(start)) this() else null
        override fun <T : Node> Grammar<T>.many(): List<T> {
            val list = mutableListOf<T>()
            while (lex.isNext(start))
                list.add(this())
            return list
        }
    }

    fun <T : Node> run(grammar: Grammar<T>): T = GrammarScopeImpl().run { grammar() }
}
