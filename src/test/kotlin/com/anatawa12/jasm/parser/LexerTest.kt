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

    @Test
    fun doReadOwnerAndName() {
        val ownerAndNames = listOf(
            " java/lang/Object/toString aaaa" to ("java/lang/Object" to "toString"),
            " java/lang/Object/<init> aaaa" to ("java/lang/Object" to "<init>")
        )
        for ((str, ownerAndName) in ownerAndNames) withTesting(str) {
            assertEquals(ownerAndName, Lexer(StringLexerReader(str)).doRead(TokenType.OwnerAndName))
        }
    }

    @Test
    fun doReadDouble() {
        val strings = listOf(
            """ 0.1 """ to 0.1,
            """ 7 """ to null
        )
        for ((value, string) in strings) withTesting(value){
            assertEquals(string, Lexer(StringLexerReader(value)).doRead(TokenType.Double))
        }
    }

    @Test
    fun doReadInt() {
        val strings = listOf(
            """ 0.1 """ to 0,
            """ 1 """ to 1,
            """ 7 """ to 7
        )
        for ((value, string) in strings) withTesting(value){
            assertEquals(string, Lexer(StringLexerReader(value)).doRead(TokenType.Integer))
        }
    }

    @Test
    fun doReadFloat() {
        val strings = listOf(
            """ 100.0f """ to 100.0f,
            """ 7 """ to null
        )
        for ((value, string) in strings) withTesting(value){
            assertEquals(string, Lexer(StringLexerReader(value)).doRead(TokenType.Float))
        }
    }

    @Test
    fun doReadLong() {
        val strings = listOf(
            """ 0.1 """ to null,
            """ 1 """ to null,
            """ 7 """ to null,
            """ 1L """ to 1L,
            """ 7L """ to 7L
        )
        for ((value, string) in strings) withTesting(value){
            assertEquals(string, Lexer(StringLexerReader(value)).doRead(TokenType.Long))
        }
    }
}
