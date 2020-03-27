package com.anatawa12.jasm

import com.anatawa12.jasm.disassembler.Disassembler
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File

fun main(args: Array<String>) {
    File(args[1]).writeText(disassemble(File(args[0]).readBytes()))
}

fun disassemble(classFile: ByteArray): String {
    val reader = ClassReader(classFile)
    val node = ClassNode()
    reader.accept(node, arrayOf(DeprecatedAttribute), ClassReader.EXPAND_FRAMES)
    val disassembler = Disassembler()
    disassembler.disassemble(node)
    return disassembler.file.getString()
}
