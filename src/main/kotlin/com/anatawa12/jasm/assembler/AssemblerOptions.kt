package com.anatawa12.jasm.assembler

import com.anatawa12.jasm.tree.JasmAutoline
import com.anatawa12.jasm.tree.JasmHeader

class AssemblerOptions(
    val autoLine: Boolean
) {

    companion object {
        fun parse(header: JasmHeader): AssemblerOptions {
            var autoLine: Boolean = false
            for (element in header.elements) {
                when (element) {
                    is JasmAutoline -> autoLine = true
                }
            }
            return AssemblerOptions(autoLine)
        }
    }
}
