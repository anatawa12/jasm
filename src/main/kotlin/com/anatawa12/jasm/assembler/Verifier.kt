package com.anatawa12.jasm.assembler

import com.anatawa12.jasm.assembler.VerifyingErrorType.*
import com.anatawa12.jasm.assembler.VerifyingWarnType.SeemsNestedButNoEnclosing
import com.anatawa12.jasm.assembler.VerifyingWarnType.NoSuperClass
import com.anatawa12.jasm.tree.*
import com.anatawa12.jasm.tree.Annotation

class Verifier(val options: AssemblerOptions) {
    fun verify(file: JasmFile) {
        verify(file.classHeader);
        var hadDeprecated = false
        for (element in file.elements) {
            when (element) {
                is MethodBlock -> verify(element)
                is FieldBlock -> verify(element)
                is ClassDeprecated -> {
                    if (hadDeprecated) addError(TwoOrMoreDeprecated, element)
                    hadDeprecated = true
                }
                is ClassAnnotation -> {
                    verify(element.annotation, isMethod = false)
                }
            }
        }
    }

    // region jasm header

    private fun verify(header: ClassHeader) {
        if (header.bytecode != null) {
            verify(header.bytecode.major in 45..52, UnsupportedBytecode, header.bytecode)
        }
        if (header.source != null) {
            // nop
        }
        @Suppress("SENSELESS_COMPARISON")
        if (header.className != null) {
            verifyClassAccessFlag(header.className.accessFlags)
            verifyInternalName(header.className.internalName, header.className)
        }
        if (header.superName != null) {
            verifyInternalName(header.superName.internalName, header.superName)
        } else {
            addWarn(NoSuperClass, header.className)
        }
        for (implement in header.implements) {
            verifyInternalName(implement.internalName, header.className)
        }
        if (header.signature != null) {
            verifyClassSignature(header.signature.signature, header.signature)
        }
        when (header.enclosing) {
            is EnclosingClassDirective -> {
                verify(header.enclosing.owner.isNotEmpty(), ClassNameEmpty, header.enclosing)
            }
            is EnclosingMethodDirective -> {
                verify(header.enclosing.owner.isNotEmpty(), ClassNameEmpty, header.enclosing)
                verifyMethodName(header.enclosing.name, header.enclosing)
                verifyMethodDescriptor(header.enclosing.descriptor, header.enclosing)
            }
            null -> {
                if (header.className.internalName.contains('$')) {
                    addWarn(SeemsNestedButNoEnclosing, header.className)
                }
            }
        }
        if (header.debug != null) {
            //nop
        }
        for (innerClass in header.innerClasses) {
            verifyInnerClassAccessFlag(innerClass.accessFlags)
            verifyInternalName(innerClass.name, innerClass)
        }
    }

    // endregion

    // region method

    private fun verify(method: MethodBlock) {
        verifyMethodFlag(method.accessFlags)
        verifyMethodName(method.name, method)
        verifyMethodDescriptor(method.descriptor, method)
        var hadStackLimit = false
        var hadLocalLimit = false
        var hadSignature = false
        var hadDeprecated = false
        var hadInstructionMetadata = false
        var hadInstruction = false
        var hadAnnotationDefault = false

        for (statement in method.statements) {
            when (statement) {
                is StackLimit -> {
                    if (hadStackLimit) addError(TwoOrMoreStackLimit, statement)
                    hadStackLimit = true
                }
                is LocalLimit -> {
                    if (hadLocalLimit) addError(TwoOrMoreLocalLimit, statement)
                    hadLocalLimit = true
                }
                is LineNumber,
                is LocalVar,
                is TryCatch,
                is LabelDefinition,
                is StackFrame -> {
                    hadInstructionMetadata = true
                }
                is Throws -> {
                    verifyInternalName(statement.internalName, statement)
                }
                is MethodSignature -> {
                    if (hadSignature) addError(TwoOrMoreSignature, statement)
                    else verifyMethodSignature(statement.methodSignature, statement)
                    hadSignature = true
                }
                is MethodDeprecated -> {
                    if (hadDeprecated) addError(TwoOrMoreDeprecated, statement)
                    hadDeprecated = true
                }
                is MethodAnnotation -> {
                    if (statement.annotation.type is AnnotationType.Default) {
                        if (hadAnnotationDefault) addError(TwoOrMoreAnnotationDefault, statement)
                        hadAnnotationDefault = true
                    }
                    verify(statement.annotation, isMethod = true)
                }
                is Instruction -> {
                    hadInstruction = true
                }
            }
        }
        if (hadInstructionMetadata && !hadInstruction)
            addError(NoInstructionButSomeMeta, method)
        if (hadInstruction && !hadStackLimit)
            addError(InstructionButNoStackLimit, method)
        if (hadInstruction && !hadLocalLimit)
            addError(InstructionButNoLocalLimit, method)
    }

    // endregion method

    // region field

    private fun verify(field: FieldBlock) {
        verifyMethodFlag(field.accessFlags)
        verifyMethodName(field.name, field)
        verifyFieldDescriptor(field.descriptor, field)

        when (field.default?.value) {
            null -> {}
            is Int -> {
                if (field.descriptor != "I")
                    addError(InvalidDefaultValueForType, field.default)
            }
            is Double -> {
                if (field.descriptor != "D")
                    addError(InvalidDefaultValueForType, field.default)
            }
            is Long -> {
                if (field.descriptor != "J")
                    addError(InvalidDefaultValueForType, field.default)
            }
            is Float -> {
                if (field.descriptor != "F")
                    addError(InvalidDefaultValueForType, field.default)
            }
            is String -> {
                if (field.descriptor != "L${"java/lang/String"};")
                    addError(InvalidDefaultValueForType, field.default)
            }
        }

        var hadSignature = false
        var hadDeprecated = false

        for (attribute in field.attributes) {
            when (attribute) {
                is FieldDeprecated -> {
                    if (hadDeprecated) addError(TwoOrMoreDeprecated, attribute)
                    hadDeprecated = true
                }
                is FieldSignature -> {
                    if (hadSignature) addError(TwoOrMoreSignature, attribute)
                    else verifyFieldSignature(attribute.signature, attribute)
                    hadSignature = true
                }
                is FieldAnnotation -> {
                    verify(attribute.annotation, isMethod = false)
                }
            }
        }
    }

    // endregion field

    // region utils

    private fun verify(annotation: Annotation, isMethod: Boolean) {
        if (!isMethod && annotation.type is AnnotationType.Default)
            addError(AnnotationDefaultNotSupported, annotation)
    }

    internal fun verifyInternalName(internalName: String, errorAt: Node) {
        if (internalName.isEmpty())
            return addError(ClassNameEmpty, errorAt)
        for (c in internalName) {
            if (c == '.' || c == ';' || c == '[' || c == '<' || c == '>')
                return addError(InvalidClassName, errorAt)
        }
    }

    private fun verifyMethodName(methodName: String, errorAt: Node) {
        if (methodName.isEmpty())
            return addError(MethodNameEmpty, errorAt)
        if (methodName == "<init>") return
        if (methodName == "<clinit>") return
        for (c in methodName) {
            if (c == '.' || c == ';' || c == '[' || c == '/' || c == '<' || c == '>')
                return addError(InvalidMethodName, errorAt)
        }
    }

    // endregion

    // region access

    private fun verifyClassAccessFlag(accessFlags: AccessFlags) {
        verify(accessFlags.flags.size == accessFlags.flags.toSet().size, FlagDuplicated, accessFlags)
        var hadClassType = false
        var erroredClassType = false
        loop@ for (flag in accessFlags.flags) {
            when (flag) {
                is AccessFlag.Public,
                is AccessFlag.Final,
                is AccessFlag.Synthetic -> {
                    // no operation
                }

                is AccessFlag.Interface,
                is AccessFlag.Abstract,
                is AccessFlag.Annotation,
                is AccessFlag.Enum -> {
                    if (erroredClassType) continue@loop
                    if (hadClassType) {
                        addError(ClassTypeDuplicated, accessFlags)
                        erroredClassType = true
                    }
                    hadClassType = true
                }

                is AccessFlag.Private,
                is AccessFlag.Protected,
                is AccessFlag.Static,
                is AccessFlag.Super,
                is AccessFlag.Synchronized,
                is AccessFlag.Volatile,
                is AccessFlag.Bridge,
                is AccessFlag.Varargs,
                is AccessFlag.Transient,
                is AccessFlag.Native,
                is AccessFlag.Strict,
                is AccessFlag.Mandated -> {
                    addError(UnsupportedAccessForClass, flag)
                }
            }
        }
    }

    private fun verifyInnerClassAccessFlag(accessFlags: AccessFlags) {
        verify(accessFlags.flags.size == accessFlags.flags.toSet().size, FlagDuplicated, accessFlags)
        var hadClassType = false
        var erroredClassType = false
        var hadAccessType = false
        var erroredAccessType = false
        loop@ for (flag in accessFlags.flags) {
            when (flag) {
                is AccessFlag.Public,
                is AccessFlag.Private,
                is AccessFlag.Protected -> {
                    if (erroredAccessType) continue@loop
                    if (hadAccessType) {
                        addError(AccessDuplicated, accessFlags)
                        erroredAccessType = true
                    }
                    hadAccessType = true
                }

                is AccessFlag.Static,
                is AccessFlag.Final,
                is AccessFlag.Synthetic -> {
                    // no operation
                }

                is AccessFlag.Interface,
                is AccessFlag.Abstract,
                is AccessFlag.Annotation,
                is AccessFlag.Enum -> {
                    if (erroredClassType) continue@loop
                    if (hadClassType) {
                        addError(ClassTypeDuplicated, accessFlags)
                        erroredClassType = true
                    }
                    hadClassType = true
                }

                is AccessFlag.Super,
                is AccessFlag.Synchronized,
                is AccessFlag.Volatile,
                is AccessFlag.Bridge,
                is AccessFlag.Varargs,
                is AccessFlag.Transient,
                is AccessFlag.Native,
                is AccessFlag.Strict,
                is AccessFlag.Mandated -> {
                    addError(UnsupportedAccessForInnerClass, flag)
                }
            }
        }
    }

    private fun verifyMethodFlag(accessFlags: AccessFlags) {
        verify(accessFlags.flags.size == accessFlags.flags.toSet().size, FlagDuplicated, accessFlags)
        var hadAccessType = false
        var erroredAccessType = false
        loop@ for (flag in accessFlags.flags) {
            when (flag) {
                is AccessFlag.Public,
                is AccessFlag.Private,
                is AccessFlag.Protected -> {
                    if (erroredAccessType) continue@loop
                    if (hadAccessType) {
                        addError(AccessDuplicated, accessFlags)
                        erroredAccessType = true
                    }
                    hadAccessType = true
                }

                is AccessFlag.Static,
                is AccessFlag.Final,
                is AccessFlag.Synchronized,
                is AccessFlag.Bridge,
                is AccessFlag.Varargs,
                is AccessFlag.Native,
                is AccessFlag.Abstract,
                is AccessFlag.Strict,
                is AccessFlag.Synthetic
                -> {
                    // no operation
                }


                is AccessFlag.Super,
                is AccessFlag.Volatile,
                is AccessFlag.Transient,
                is AccessFlag.Interface,
                is AccessFlag.Annotation,
                is AccessFlag.Enum,
                is AccessFlag.Mandated -> {
                    addError(UnsupportedAccessForMethod, flag)
                }
            }
        }
    }

    // endregion access

    // region singautre

    internal fun verifyClassSignature(signature: String, errorAt: Node) {
        if (!SignatureVerifier(signature).classSignature())
            addError(InvalidClassSignature, errorAt)
    }

    internal fun verifyMethodDescriptor(descriptor: String, errorAt: Node) {
        if (!SignatureVerifier(descriptor).methodDescriptor())
            addError(InvalidMethodDescriptor, errorAt)
    }

    internal fun verifyMethodSignature(signature: String, errorAt: Node) {
        if (!SignatureVerifier(signature).methodSignature())
            addError(InvalidMethodSignature, errorAt)
    }

    internal fun verifyFieldDescriptor(descriptor: String, errorAt: Node) {
        if (!SignatureVerifier(descriptor).fieldDescriptor())
            addError(InvalidFieldDescriptor, errorAt)
    }

    internal fun verifyFieldSignature(signature: String, errorAt: Node) {
        if (!SignatureVerifier(signature).fieldSignature())
            addError(InvalidFieldSignature, errorAt)
    }

    // endregion

    private fun verify(boolean: Boolean, error: VerifyingErrorType, at: Node) {
        if (!boolean)
            addError(error, at)
    }

    val errors = mutableListOf<VerifyingError>()
    val warns = mutableListOf<VerifyingWarn>()

    private fun addError(error: VerifyingErrorType, at: Node) {
        errors.add(VerifyingError(error, at, emptyArray()))
    }

    private fun addWarn(warn: VerifyingWarnType, at: Node) {
        warns.add(VerifyingWarn(warn, at, emptyArray()))
    }
}
