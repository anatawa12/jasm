package com.anatawa12.jasm.tree


sealed class ClassElement : BranchNode()
data class MethodBlock(val accessFlags: AccessFlags, val name: String, val descriptor: String, val statements: List<MethodStatement>) : ClassElement()
data class FieldBlock(val accessFlags: AccessFlags, val name: String, val descriptor: String, val default: Value<*>?, val attributes: List<FieldAttributes>) : ClassElement()
