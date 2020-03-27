package com.anatawa12.jasm.cli

import com.anatawa12.jasm.assembleJasmFile
import com.anatawa12.jasm.assembler.AssemblerOptions
import com.anatawa12.jasm.disassemble
import com.anatawa12.jasm.parseJasmFile
import com.anatawa12.jasm.verifyFile
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import kotlin.system.exitProcess

class Root : CliktCommand() {
    override fun run() {}
}

class Assemble : CliktCommand(name = "assemble") {
    val jasmFile by argument("jasm file")
        .file(mustExist = true, mustBeReadable = true, canBeDir = false)
    val classFile by argument("output file")
        .file()

    override fun run() {
        val fileName = jasmFile.path

        val jasmFile = parseJasmFile(fileName, jasmFile.readText()) ?: exitProcess(1)
        val options = AssemblerOptions.parse(jasmFile.header)

        if (!verifyFile(fileName, jasmFile, options)) exitProcess(1)

        val classFile = assembleJasmFile(fileName, jasmFile, options)

        this.classFile.apply { parentFile.mkdirs() }
            .writeBytes(classFile)
    }
}

class Disassemble : CliktCommand(name = "disassemble") {
    val classFile by argument("class file")
        .file(mustExist = true, mustBeReadable = true, canBeDir = false)
    val jasmFile by argument("output file")
        .file()

    override fun run() {
        jasmFile.apply { parentFile.mkdirs() }
            .writeText(disassemble(classFile.readBytes()))
    }
}

fun main(args: Array<String>) = Root().subcommands(Assemble(), Disassemble()).main(args)
