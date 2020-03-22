package com.anatawa12.jasm.tree

data class JasmFile(
    val classHeader: ClassHeader,
    val elements: List<ClassElement>
) : BranchNode()
