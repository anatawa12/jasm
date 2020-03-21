package com.anatawa12.jasm.parser

import com.anatawa12.jasm.withTesting
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class LexerTest {
    @Test
    fun doReadInteger() {
        val pairs = listOf(
            "-1" to -1,
            "+1" to 1,
            "0" to 0,
            "1" to 1,
            "63" to 63
        )
        for ((str, expect) in pairs) withTesting(str) {
            assertEquals(expect, Lexer(StringLexerReader(str)).doRead(TokenType.Integer))
        }
    }

    @Test
    fun doReadInternalName() {
        val intenalNames = listOf(
            " java/lang/String  aaaa" to "java/lang/String",
            " java/lang/Object  aaaa" to "java/lang/Object"
        )
        for ((str, intenalName) in intenalNames) withTesting(str) {
            assertEquals(intenalName, Lexer(StringLexerReader(str)).doRead(TokenType.InternalName))
        }
    }

    @Test
    fun doReadMethodDescriptor() {
        val intenalNames = listOf(
            " ()Ljava/lang/String; aaaa" to "()Ljava/lang/String;",
            " ()V aaaa" to "()V"
        )
        for ((str, intenalName) in intenalNames) withTesting(str) {
            assertEquals(intenalName, Lexer(StringLexerReader(str)).doRead(TokenType.MethodDescriptor))
        }
    }
}
