package com.anatawa12.jasm.tree

data class JasmFile(
    val jasmHeader: JasmHeader,
    val elements: List<ClassElement>
) : BranchNode()
