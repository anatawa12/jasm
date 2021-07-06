package com.anatawa12.jasm.assembler

import com.anatawa12.jasm.assembler.VerifyingErrorType.*
import com.anatawa12.jasm.assembler.VerifyingWarnType.*
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
                if (header.className.internalName.substringAfterLast('$', "-").all { it in '0'..'9' }) {
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
                is LineNumber -> {
                    hadInstructionMetadata = true
                    if (options.autoLine)
                        addWarn(LineNumberWillIgnoredOnAutoLineMode, statement)
                }
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
        if (hadInstruction)
            verifyCode(method)
    }

    private fun verifyCode(method: MethodBlock) {
        verifyCodeLabels(method)
        verifyCodeStackFrames(method)
    }

    private fun verifyCodeLabels(method: MethodBlock) {
        val labels = mutableSetOf<String>()

        for (statement in method.statements.asSequence().filterIsInstance<LabelDefinition>()) {
            if (statement.name.name in labels) {
                addError(LabelDuplicated, statement.name)
            } else {
                labels.add(statement.name.name)
            }
        }

        for (statement in method.statements) {
            when (statement) {
                is LabelDefinition -> { /* no labels */ }
                is StackLimit -> { /* no labels */ }
                is LocalLimit -> { /* no labels */ }
                is LineNumber -> { /* no labels */ }
                is Throws -> { /* no labels */ }
                is MethodSignature -> { /* no labels */ }
                is MethodDeprecated -> { /* no labels */ }
                is MethodAnnotation -> { /* no labels */ }
                is Insn -> { /* no labels */ }
                is IntInsn -> { /* no labels */ }
                is FieldInsn -> { /* no labels */ }
                is IincInsn -> { /* no labels */ }
                is InvokeDynamicInsn -> { /* no labels */ }
                is LdcInsn -> { /* no labels */ }
                is MethodInsn -> { /* no labels */ }
                is MultiANewArrayInsn -> { /* no labels */ }
                is TypeInsn -> { /* no labels */ }
                is VarInsn -> { /* no labels */ }

                is LocalVar -> {
                    if (statement.from.name !in labels)
                        addError(LabelIsNotDefined, statement.from)
                    if (statement.to.name !in labels)
                        addError(LabelIsNotDefined, statement.to)
                }
                is TryCatch -> {
                    if (statement.from.name !in labels)
                        addError(LabelIsNotDefined, statement.from)
                    if (statement.to.name !in labels)
                        addError(LabelIsNotDefined, statement.to)
                    if (statement.using.name !in labels)
                        addError(LabelIsNotDefined, statement.using)
                }
                is StackFrame -> {
                    val uninitializeds = (statement.locals?.asSequence().orEmpty() + statement.stacks.asSequence())
                        .filterIsInstance<StackFrameType.Uninitialized>()
                    for (uninitialized in uninitializeds) {
                        if (uninitialized.madeAt.name !in labels)
                            addError(LabelIsNotDefined, uninitialized.madeAt)
                    }
                }
                is JumpInsn -> {
                    if (statement.label.name !in labels)
                        addError(LabelIsNotDefined, statement.label)
                }
                is LookupSwitchInsn -> {
                    if (statement.dflt.name !in labels)
                        addError(LabelIsNotDefined, statement.dflt)
                    for ((_, label) in statement.pairs) {
                        if (label.name !in labels)
                            addError(LabelIsNotDefined, label)
                    }
                }
                is TableSwitchInsn -> {
                    if (statement.dflt.name !in labels)
                        addError(LabelIsNotDefined, statement.dflt)
                    for (label in statement.labels) {
                        if (label.name !in labels)
                            addError(LabelIsNotDefined, label)
                    }
                }
            }
        }
    }

    private fun verifyCodeStackFrames(method: MethodBlock) {
        val jumpableLabels = mutableSetOf<String>()

        var hadFrame = false
        val currentLabels = mutableSetOf<String>()
        for (statement in method.statements.asSequence()) {
            when (statement) {
                is StackLimit -> { /*nop*/ }
                is LocalLimit -> { /*nop*/ }
                is LineNumber -> { /*nop*/ }
                is LocalVar -> { /*nop*/ }
                is TryCatch -> { /*nop*/ }
                is Throws -> { /*nop*/ }
                is MethodSignature -> { /*nop*/ }
                is MethodDeprecated -> { /*nop*/ }
                is MethodAnnotation -> { /*nop*/ }
                is LabelDefinition -> {
                    if (hadFrame)
                        jumpableLabels += statement.name.name
                    else
                        currentLabels += statement.name.name
                }
                is StackFrame -> { 
                    hadFrame = true
                    jumpableLabels += currentLabels
                }
                is Instruction -> {
                    currentLabels.clear()
                    hadFrame = false
                }
            }
        }

        for (statement in method.statements.asSequence().filterIsInstance<Instruction>()) {
            when (statement) {
                is Insn -> { /* no labels */ }
                is IntInsn -> { /* no labels */ }
                is FieldInsn -> { /* no labels */ }
                is IincInsn -> { /* no labels */ }
                is InvokeDynamicInsn -> { /* no labels */ }
                is LdcInsn -> { /* no labels */ }
                is MethodInsn -> { /* no labels */ }
                is MultiANewArrayInsn -> { /* no labels */ }
                is TypeInsn -> { /* no labels */ }
                is VarInsn -> { /* no labels */ }

                is JumpInsn -> {
                    if (statement.label.name !in jumpableLabels)
                        addError(StackIsNotDefined, statement.label)
                }
                is LookupSwitchInsn -> {
                    if (statement.dflt.name !in jumpableLabels)
                        addError(StackIsNotDefined, statement.dflt)
                    for ((_, label) in statement.pairs) {
                        if (label.name !in jumpableLabels)
                            addError(StackIsNotDefined, label)
                    }
                }
                is TableSwitchInsn -> {
                    if (statement.dflt.name !in jumpableLabels)
                        addError(StackIsNotDefined, statement.dflt)
                    for (label in statement.labels) {
                        if (label.name !in jumpableLabels)
                            addError(StackIsNotDefined, label)
                    }
                }
            }
        }
    }

    // endregion method

    // region field

    private fun verify(field: FieldBlock) {
        verifyFieldFlag(field.accessFlags)
        verifyFieldName(field.name, field)
        verifyFieldDescriptor(field.descriptor, field)

        when (field.default?.value) {
            null -> {}
            is Int -> {
                if (field.descriptor != "I"
                    && field.descriptor != "S"
                    && field.descriptor != "C"
                    && field.descriptor != "B"
                    && field.descriptor != "Z")
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

    private fun verifyFieldName(methodName: String, errorAt: Node) {
        if (methodName.isEmpty())
            return addError(FieldNameEmpty, errorAt)
        for (c in methodName) {
            if (c == '.' || c == ';' || c == '[' || c == '/' || c == '<' || c == '>')
                return addError(InvalidFieldName, errorAt)
        }
    }

    // endregion

    // region access

    private fun verifyClassAccessFlag(accessFlags: AccessFlags) {
        verify(accessFlags.flags.size == accessFlags.flags.toSet().size, FlagDuplicated, accessFlags)
        loop@ for (flag in accessFlags.flags) {
            when (flag) {
                is AccessFlag.Public,
                is AccessFlag.Final,
                is AccessFlag.Interface,
                is AccessFlag.Abstract,
                is AccessFlag.Synthetic,
                is AccessFlag.Annotation,
                is AccessFlag.Enum -> {
                    // no operation
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
                is AccessFlag.Interface,
                is AccessFlag.Abstract,
                is AccessFlag.Synthetic,
                is AccessFlag.Annotation,
                is AccessFlag.Enum -> {
                    // no operation
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

    private fun verifyFieldFlag(accessFlags: AccessFlags) {
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
                is AccessFlag.Volatile,
                is AccessFlag.Transient,
                is AccessFlag.Synthetic,
                is AccessFlag.Enum
                -> {
                    // no operation
                }


                is AccessFlag.Super,
                is AccessFlag.Synchronized,
                is AccessFlag.Interface,
                is AccessFlag.Annotation,
                is AccessFlag.Bridge,
                is AccessFlag.Varargs,
                is AccessFlag.Native,
                is AccessFlag.Abstract,
                is AccessFlag.Strict,
                is AccessFlag.Mandated -> {
                    addError(UnsupportedAccessForField, flag)
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
