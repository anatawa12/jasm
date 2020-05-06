package com.anatawa12.jasm.assembler

import com.anatawa12.jasm.tree.Node

data class VerifyingError(val error: VerifyingErrorType, val at: Node, val params: Array<Any?>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VerifyingError

        if (error != other.error) return false
        if (at != other.at) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = error.hashCode()
        result = 31 * result + at.hashCode()
        result = 31 * result + params.contentHashCode()
        return result
    }
}

enum class VerifyingErrorType(val message: String) {
    UnsupportedBytecode("unsupported bytecode version. must be in 45..52"),
    ClassNameEmpty("class name must not be empty"),
    MethodNameEmpty("method name must not be empty"),
    FieldNameEmpty("field name must not be empty"),

    InvalidClassName("class name is not valid"),
    InvalidMethodName("method name is not valid"),
    InvalidFieldName("field name is not valid"),

    InvalidClassSignature("class signature is not valid."),
    InvalidMethodDescriptor("method descriptor is not valid."),
    InvalidMethodSignature("method signature is not valid."),
    InvalidFieldDescriptor("field descriptor is not valid."),
    InvalidFieldSignature("field signature is not valid."),

    InvalidDefaultValueForType("type mismatch: constant value and descriptor"),
    AnnotationDefaultNotSupported (".annotation default not supported for this target"),

    TwoOrMoreStackLimit("two or more .limit stack found"),
    TwoOrMoreLocalLimit("two or more .limit local found"),
    TwoOrMoreSignature("two or more .signature found"),
    TwoOrMoreDeprecated("two or more .deprecated found"),
    TwoOrMoreAnnotationDefault("two ore more .annotation default found"),

    NoInstructionButSomeMeta("instructions not found but .line .var .try label: or .stack"),
    InstructionButNoStackLimit("instructions found but .limit stack not found"),
    InstructionButNoLocalLimit("instructions found but .limit local not found"),

    ClassTypeDuplicated("cannot set two ore more interface, abstract, anotation, or enum."),
    AccessDuplicated("cannot set two ore more public, private, or protected."),
    UnsupportedAccessForClass("this flag is not supported for class"),
    UnsupportedAccessForInnerClass("this flag is not supported for inner class"),
    UnsupportedAccessForMethod("this flag is not supported for method"),
    UnsupportedAccessForField("this flag is not supported for field"),
    FlagDuplicated("some acces flags are duplicated"),

    // code verify
    LabelDuplicated("label definition duplicated"),
    LabelIsNotDefined("the label is not defined."),
}

data class VerifyingWarn(val warn: VerifyingWarnType, val at: Node, val params: Array<Any?>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VerifyingWarn

        if (warn != other.warn) return false
        if (at != other.at) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = warn.hashCode()
        result = 31 * result + at.hashCode()
        result = 31 * result + params.contentHashCode()
        return result
    }
}

enum class VerifyingWarnType(val message: String) {
    NoSuperClass("no .super directive is specified"),
    SeemsNestedButNoEnclosing("class seems nested class but there is not .enclosing directive"),
    LineNumberWillIgnoredOnAutoLineMode(".line will be ignored because .autoline mode is enabled"),
}
