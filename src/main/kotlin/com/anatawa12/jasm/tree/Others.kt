package com.anatawa12.jasm.tree

sealed class AnnotationValue : BranchNode()

/**
 * @param T must be one of [Byte] [Char] [Double] [Float] [Int] [Long] [Short] [Boolean] [String]
 */
class Value<T : Any>(val value: T) : AnnotationValue()
class AnnotationArray(val values: List<AnnotationValue>) : List<AnnotationValue> by values, AnnotationValue()

data class AnnotationEnum(val internalName: String, val name: String) : AnnotationValue()
class ClassName(val desc: String) : AnnotationValue()

data class Annotation(
    val type: AnnotationType,
    val internalName: String,
    val pairs: List<Pair<String, AnnotationValue>>) : AnnotationValue()

sealed class AnnotationType : BranchNode() {
    class Visible: AnnotationType()
    class Invisible: AnnotationType()
    data class VisiblePararm(val pararmIndex: Int): AnnotationType()
    data class InvisiblePararm(val pararmIndex: Int): AnnotationType()
    class Default: AnnotationType()
}


class AccessFlags(val flags: List<AccessFlag>) : BranchNode()

sealed class AccessFlag() : BranchNode() {
    val name = javaClass.simpleName.toLowerCase()
    class Public() : AccessFlag()
    class Private() : AccessFlag()
    class Protected() : AccessFlag()
    class Static() : AccessFlag()
    class Final() : AccessFlag()
    class Super() : AccessFlag()
    class Synchronized() : AccessFlag()
    class Volatile() : AccessFlag()
    class Bridge() : AccessFlag()
    class Varargs() : AccessFlag()
    class Transient() : AccessFlag()
    class Native() : AccessFlag()
    class Interface() : AccessFlag()
    class Abstract() : AccessFlag()
    class Strict() : AccessFlag()
    class Synthetic() : AccessFlag()
    class Annotation() : AccessFlag()
    class Enum() : AccessFlag()
    class Mandated() : AccessFlag()
}
