package com.anatawa12.jasm.tree

data class JasmFile(
    val header: JasmHeader,
    val classHeader: ClassHeader,
    val elements: List<ClassElement>
) : BranchNode()
