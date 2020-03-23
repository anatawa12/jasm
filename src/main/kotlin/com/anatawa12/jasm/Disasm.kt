package com.anatawa12.jasm

import com.anatawa12.jasm.disassembler.Disassembler
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File

fun main(args: Array<String>) {
    val reader = ClassReader(File(args[0]).readBytes())
    val node = ClassNode()
    reader.accept(node, arrayOf(DeprecatedAttribute), ClassReader.EXPAND_FRAMES)
    val disassembler = Disassembler()
    disassembler.disassemble(node)
    File(args[1]).writeText(disassembler.file.getString())
}
