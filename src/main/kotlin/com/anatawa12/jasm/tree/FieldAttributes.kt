package com.anatawa12.jasm.tree

sealed class FieldAttributes() : BranchNode()
class FieldDeprecated : FieldAttributes()
data class FieldSignature(val signature: String) : FieldAttributes()
data class FieldAnnotation(val annotation: Annotation) : FieldAttributes()
