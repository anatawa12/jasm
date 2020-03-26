package com.anatawa12.jasm

import com.anatawa12.jasm.assembler.Assembler
import com.anatawa12.jasm.assembler.AssemblerOptions
import com.anatawa12.jasm.assembler.Verifier
import com.anatawa12.jasm.parser.Lexer
import com.anatawa12.jasm.parser.LexingError
import com.anatawa12.jasm.parser.Parser
import com.anatawa12.jasm.parser.StringLexerReader
import com.anatawa12.jasm.tree.JasmFile
import java.io.File
import javax.management.monitor.StringMonitor
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val fileName = args[0]
    val jasmFile = parseJasmFile(File(fileName).readText()) ?: exitProcess(1)
    val options = AssemblerOptions.parse(jasmFile.header)

    if (!verifyFile(fileName, jasmFile, options)) exitProcess(1)

    val classFile = assembleJasmFile(fileName, jasmFile, options)

    File(args[1]).writeBytes(classFile)
}

fun parseJasmFile(fileName: String, content: String): JasmFile? {
    val reader = StringLexerReader(content)
    val lexer = Lexer(reader)
    val parser = Parser(lexer)
    return try {
        parser.run(parser.jasm_file)
    } catch (e: LexingError) {
        System.err.println("e: ${fileName}: ${e.message}")
        null
    }
}

fun verifyFile(fileName: String, jasmFile: JasmFile, options: AssemblerOptions): Boolean {
    val verifier = Verifier(options)
    verifier.verify(jasmFile)
    for (error in verifier.errors) {
        val at = error.at.tokens.first().begin
        System.err.println("e: ${fileName}: (${at.line}, ${at.column}): ${error.error.message}")
    }
    for (warn in verifier.warns) {
        val at = warn.at.tokens.first().begin
        System.err.println("w: ${fileName}: (${at.line}, ${at.column}): ${warn.warn.message}")
    }
    if (verifier.errors.isNotEmpty())
        return false
    return true
}

fun assembleJasmFile(fileName: String, jasmFile: JasmFile, options: AssemblerOptions): ByteArray? {
    val assembler = Assembler(options)

    assembler.assemble(jasmFile)

    return assembler.classWriter.toByteArray()
}
