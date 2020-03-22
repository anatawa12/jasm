package com.anatawa12.jasm

import com.anatawa12.jasm.assembler.Assembler
import com.anatawa12.jasm.assembler.AssemblerOptions
import com.anatawa12.jasm.assembler.Verifier
import com.anatawa12.jasm.parser.Lexer
import com.anatawa12.jasm.parser.LexingError
import com.anatawa12.jasm.parser.Parser
import com.anatawa12.jasm.parser.StringLexerReader
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val reader = StringLexerReader(File(args[0]).readText())
    val lexer = Lexer(reader)
    val parser = Parser(lexer)
    val jasmFile = try {
        parser.run(parser.jasm_file)
    } catch (e: LexingError) {
        System.err.println("e: ${args[0]}: ${e.message}")
        exitProcess(1)
    }

    val verifier = Verifier()
    verifier.verify(jasmFile)
    for (error in verifier.errors) {
        val at = error.at.tokens.first().begin
        System.err.println("e: ${args[0]}: (${at.line}, ${at.column}): ${error.error.message}")
    }
    for (warn in verifier.warns) {
        val at = warn.at.tokens.first().begin
        System.err.println("w: ${args[0]}: (${at.line}, ${at.column}): ${warn.warn.message}")
    }
    if (verifier.errors.isNotEmpty())
        exitProcess(1)

    val options = AssemblerOptions.parse(jasmFile.header)

    val assembler = Assembler(options)

    assembler.assemble(jasmFile)

    File(args[1]).writeBytes(assembler.classWriter.toByteArray())
}
