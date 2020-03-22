package com.anatawa12.jasm.tree

data class JasmHeader(val elements: List<JasmHeaderElement>) : BranchNode()

sealed class JasmHeaderElement() : BranchNode()
class JasmAutoline() : JasmHeaderElement()
