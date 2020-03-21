package com.anatawa12.jasm.parser

import com.anatawa12.jasm.tree.*

class Parser(lex: ILexer) : AbstractParser(lex) {
    val jasm_file = grammar({ jasm_header.start }) {
        JasmFile(jasm_header(), class_element.many())
    }

    val jasm_header = grammar({ bytecode_directive.start + source_directive.start + class_directive.start }) {
        JasmHeader(
            bytecode_directive.optional(),
            source_directive.optional(),
            class_directive(),
            super_directive.optional(),
            implements_directive.many(),
            signature_directive.optional(),
            enclosing_directive.optional(),
            debug_directive.optional(),
            inner_class_directive.many()
        )
    }

    val bytecode_directive = grammar(dotBytecode) {
        lex.read(dotBytecode)
        val major = lex.read(TokenType.Integer)
        lex.read(dot)
        val minor = lex.read(TokenType.Integer)
        BytecodeDirective(major, minor)
    }

    val source_directive = grammar(dotSource) {
        lex.read(dotSource)
        val source = lex.read(TokenType.String)
        SourceDirective(source)
    }

    val class_directive = grammar(dotClass) {
        lex.read(dotClass)
        val flags = access_flags()
        val name = lex.read(TokenType.InternalName)
        ClassDirective(flags, name)
    }

    val super_directive = grammar(dotSuper) {
        lex.read(dotSuper)
        val name = lex.read(TokenType.InternalName)
        SuperDirective(name)
    }

    val implements_directive = grammar(dotImplements) {
        lex.read(dotImplements)
        val name = lex.read(TokenType.InternalName)
        ImplementsDirective(name)
    }

    val signature_directive = grammar(dotSignature) {
        lex.read(dotSignature)
        val signature = lex.read(TokenType.String)
        SignatureDirective(signature)
    }

    val enclosing_directive = grammar(dotEnclosing) {
        lex.read(dotEnclosing)
        if (lex.isNext(keyMethod)) {
            lex.read(keyMethod)
            val owner = lex.read(TokenType.InternalName)
            val name = lex.read(TokenType.Name)
            val descriptor = lex.read(TokenType.MethodDescriptor)
            EnclosingMethodDirective(owner, name, descriptor)
        } else if (lex.isNext(keyClass)) {
            lex.read(keyClass)
            val owner = lex.read(TokenType.InternalName)
            EnclosingClassDirective(owner)
        } else lex.unexpectTokenError(keyMandated, keyClass)
    }

    val debug_directive = grammar(dotDebug) {
        lex.read(dotDebug)
        val debugData = lex.read(TokenType.String)
        DebugDirective(debugData)
    }

    val inner_class_directive = grammar(dotInner) {
        lex.read(dotInner)
        val access = access_flags()
        val name = lex.read(TokenType.InternalName)
        val outerName = if (lex.isNext(keyInner)) {
            lex.read(keyInner)
            lex.read(TokenType.Name)
        } else null
        val innerName = if (lex.isNext(keyOuter)) {
            lex.read(keyOuter)
            lex.read(TokenType.Name)
        } else null
        InnerClassDirective(access, name, outerName, innerName)
    }

    val class_element = grammar({ method_block.start + field_block.start }) {
        when {
            lex.isNext(method_block.start) -> method_block()
            lex.isNext(field_block.start) -> field_block()
            else -> lex.unexpectTokenError(method_block.start, field_block.start)
        }
    }

    val method_block = grammar(dotMethod) {
        lex.read(dotMethod)
        val access = access_flags()
        val name = lex.read(TokenType.Name)
        val desc = lex.read(TokenType.MethodDescriptor)
        val statements = method_statement.many()
        lex.read(dotEnd)
        lex.read(keyMethod)
        MethodBlock(access, name, desc, statements)
    }

    val method_statement = grammar({
        annotation_block.start +
                stack_block.start +
                instruction.start +
                label_definition.start +
                annotation_block.start +
                setOf(
                    dotLimit,
                    dotLine,
                    dotVar,
                    dotThrows,
                    dotCatch,
                    dotSignature,
                    dotDeprecated
                )
    }) {
        when {
            lex.isNext(dotLimit) -> {
                lex.read(dotLimit)
                when {
                    lex.isNext(keyStack) -> {
                        lex.read(keyStack)
                        val stack = lex.read(TokenType.Integer)
                        StackLimit(stack)
                    }
                    lex.isNext(keyLocal) -> {
                        lex.read(keyLocal)
                        val local = lex.read(TokenType.Integer)
                        LocalLimit(local)
                    }
                    else -> lex.unexpectTokenError(keyStack, keyLocal)
                }
            }
            lex.isNext(dotLine) -> {
                lex.read(dotLine)
                val line = lex.read(TokenType.Integer)
                LineNumber(line)
            }
            lex.isNext(dotVar) -> {
                lex.read(dotVar)
                val variable = lex.read(TokenType.Integer)
                lex.read(keyIs)
                val name = lex.read(TokenType.Name)
                val desc = lex.read(TokenType.FieldDescriptor)
                val signature = if (lex.isNext(keySignature)) {
                    lex.read(keySignature)
                    lex.read(TokenType.String)
                } else null
                lex.read(keyFrom)
                val from = label()
                lex.read(keyTo)
                val to = label()
                LocalVar(variable, name, desc, signature, from, to)
            }
            lex.isNext(dotThrows) -> {
                lex.read(dotThrows)
                val throws = lex.read(TokenType.InternalName)
                Throws(throws)
            }
            lex.isNext(dotCatch) -> {
                lex.read(dotCatch)
                val exception = if (lex.isNext(asterisks)) {
                    lex.read(asterisks)
                    null
                } else {
                    lex.read(TokenType.InternalName)
                }
                lex.read(keyFrom)
                val from = label()
                lex.read(keyTo)
                val to = label()
                lex.read(keyUsing)
                val using = label()
                TryCatch(exception, from, to, using)
            }
            lex.isNext(dotSignature) -> {
                lex.read(dotSignature)
                val signature = lex.read(TokenType.String)
                MethodSignature(signature)
            }
            lex.isNext(dotDeprecated) -> {
                lex.read(dotDeprecated)
                MethodDeprecated()
            }
            lex.isNext(annotation_block.start) -> {
                MethodAnnotation(annotation_block())
            }
            lex.isNext(stack_block.start) -> {
                stack_block()
            }
            lex.isNext(instruction.start) -> {
                instruction()
            }
            lex.isNext(label_definition.start) -> {
                label_definition()
            }
            else -> lex.unexpectTokenError(
                annotation_block.start, stack_block.start, instruction.start,
                label_definition.start,
                setOf(
                    dotLimit,
                    dotLine,
                    dotVar,
                    dotThrows,
                    dotCatch,
                    dotSignature,
                    dotDeprecated
                )
            )
        }
    }

    val stack_block = grammar(dotStack) {
        lex.read(dotStack)
        val locals = if (lex.isNext(keyUse)) {
            lex.read(keyUse)
            lex.read(keyLocals)
            null
        } else {
            locals.many()
        }
        val stacks = stacks.many()
        StackFrame(locals, stacks)
    }

    val locals = grammar(keyLocals) {
        lex.read(keyLocals)
        stackmap_type()
    }

    val stacks = grammar(keyStacks) {
        lex.read(keyStacks)
        stackmap_type()
    }

    val stackmap_type = grammar(
        keyTop,
        keyInteger,
        keyFloat,
        keyLong,
        keyDouble,
        keyNull,
        keyUninitializedThis,
        keyObject,
        keyUninitialized
    ) {
        when {
            lex.isNext(keyTop) -> {
                lex.read(keyTop)
                StackFrameType.Top()
            }
            lex.isNext(keyInteger) -> {
                lex.read(keyInteger)
                StackFrameType.Integer()
            }
            lex.isNext(keyFloat) -> {
                lex.read(keyFloat)
                StackFrameType.Float()
            }
            lex.isNext(keyLong) -> {
                lex.read(keyLong)
                StackFrameType.Long()
            }
            lex.isNext(keyDouble) -> {
                lex.read(keyDouble)
                StackFrameType.Double()
            }
            lex.isNext(keyNull) -> {
                lex.read(keyNull)
                StackFrameType.Null()
            }
            lex.isNext(keyUninitializedThis) -> {
                lex.read(keyUninitializedThis)
                StackFrameType.UninitializedThis()
            }
            lex.isNext(keyObject) -> {
                lex.read(keyObject)
                StackFrameType.Object(lex.read(TokenType.InternalName))
            }
            lex.isNext(keyUninitialized) -> {
                lex.read(keyUninitialized)
                StackFrameType.Uninitialized(label())
            }
            else -> lex.unexpectTokenError(
                keyTop,
                keyInteger,
                keyFloat,
                keyLong,
                keyDouble,
                keyNull,
                keyUninitializedThis,
                keyObject,
                keyUninitialized
            )
        }
    }

    val instruction = grammar({ GrammarInstrucion.values().mapTo(mutableSetOf()) { it.keyword } }) {
        for (insn in GrammarInstrucion.values()) {
            if (!lex.isNext(insn.keyword)) continue
            lex.read(insn.keyword)
            when (insn.type) {
                InstrucionType.Insn -> {
                    return@grammar Insn(insn.opcode)
                }
                InstrucionType.IntInsn -> {
                    val operand = lex.read(TokenType.Integer)
                    return@grammar IntInsn(insn.opcode, operand)
                }
                InstrucionType.VarInsn -> {
                    val variable = lex.read(TokenType.Integer)
                    return@grammar VarInsn(insn.opcode, variable)
                }
                InstrucionType.TypeInsn -> {
                    val type = lex.read(TokenType.InternalName)
                    return@grammar TypeInsn(insn.opcode, type)
                }
                InstrucionType.FieldInsn -> {
                    val (owner, name) = lex.read(TokenType.OwnerAndName)
                    val desc = lex.read(TokenType.FieldDescriptor)
                    return@grammar FieldInsn(insn.opcode, owner, name, desc)
                }
                InstrucionType.MethodInsn -> {
                    val isInterfaceMethod = if (lex.isNext(keyInterface)) {
                        lex.read(keyInterface)
                        true
                    } else false
                    val (owner, name) = lex.read(TokenType.OwnerAndName)
                    val desc = lex.read(TokenType.MethodDescriptor)
                    return@grammar MethodInsn(insn.opcode, owner, name, desc, isInterfaceMethod)
                }
                InstrucionType.InvokeDynamicInsn -> {
                    val name = lex.read(TokenType.Name)
                    val desc = lex.read(TokenType.MethodDescriptor)
                    val bsm = method_handle()
                    val args = ldc_constant.many()
                    return@grammar InvokeDynamicInsn(name, desc, bsm, args)
                }
                InstrucionType.JumpInsn -> {
                    return@grammar JumpInsn(insn.opcode, label())
                }
                InstrucionType.LdcInsn -> {
                    return@grammar LdcInsn(ldc_constant())
                }
                InstrucionType.IincInsn -> {
                    return@grammar IincInsn(
                        variable = lex.read(TokenType.Integer),
                        increment = lex.read(TokenType.Integer)
                    )
                }
                InstrucionType.TableSwitchInsn -> {
                    val min = lex.read(TokenType.Integer)
                    val labels = mutableListOf<LabelName>()
                    while (!lex.isNext(keyDefault)) {
                        labels.add(label())
                    }
                    lex.read(keyDefault)
                    lex.read(colon)
                    val default = label()
                    return@grammar TableSwitchInsn(min, default, labels)
                }
                InstrucionType.LookupSwitchInsn -> {
                    val pairs = mutableListOf<Pair<Int, LabelName>>()
                    while (!lex.isNext(keyDefault)) {
                        val value = lex.read(TokenType.Integer)
                        val label = label()
                        pairs.add(value to label)
                    }
                    lex.read(keyDefault)
                    lex.read(colon)
                    val default = label()
                    return@grammar LookupSwitchInsn(default, pairs)
                }
                InstrucionType.MultiANewArrayInsn -> {
                    val arrayDesc = lex.read(TokenType.FieldDescriptor)
                    val dimensions = lex.read(TokenType.Integer)
                    return@grammar MultiANewArrayInsn(arrayDesc, dimensions)
                }
            }
        }
        lex.unexpectTokenError(GrammarInstrucion.values().mapTo(mutableSetOf()) { it.keyword } as Set<TokenType<*>>)
    }

    val ldc_constant = grammar(
        TokenType.Integer,
        TokenType.Double,
        TokenType.Long,
        TokenType.Float,
        TokenType.String,
        keyClass,
        TokenType.FieldDescriptor,
        keyHandle,
        keyMethodType,
        TokenType.MethodDescriptor
    ) {
        when {
            lex.isNext(TokenType.Integer) -> {
                Value(lex.read(TokenType.Integer))
            }
            lex.isNext(TokenType.Double) -> {
                Value(lex.read(TokenType.Double))
            }
            lex.isNext(TokenType.Long) -> {
                Value(lex.read(TokenType.Long))
            }
            lex.isNext(TokenType.Float) -> {
                Value(lex.read(TokenType.Float))
            }
            lex.isNext(TokenType.String) -> {
                Value(lex.read(TokenType.String))
            }
            lex.isNext(keyClass) -> {
                lex.read(keyClass)
                ClassName(lex.read(TokenType.FieldDescriptor))
            }
            lex.isNext(TokenType.FieldDescriptor) -> {
                ClassName(lex.read(TokenType.FieldDescriptor))
            }
            lex.isNext(keyHandle) -> {
                lex.read(keyHandle)
                method_handle()
            }
            lex.isNext(keyMethodType) -> {
                lex.read(keyMethodType)
                MethodType(lex.read(TokenType.MethodDescriptor))
            }
            lex.isNext(TokenType.MethodDescriptor) -> {
                MethodType(lex.read(TokenType.MethodDescriptor))
            }
            else -> lex.unexpectTokenError(
                TokenType.Integer,
                TokenType.Double,
                TokenType.Long,
                TokenType.Float,
                TokenType.String,
                keyClass,
                TokenType.FieldDescriptor,
                keyHandle,
                keyMethodType,
                TokenType.MethodDescriptor
            )
        }
    }

    val method_handle = grammar() {
        val handleType = when {
            lex.isNext(keyGetField) -> {
                lex.read(keyGetField)
                HandleType.GetField
            }
            lex.isNext(keyGetStatic) -> {
                lex.read(keyGetStatic)
                HandleType.GetStatic
            }
            lex.isNext(keyPutField) -> {
                lex.read(keyPutField)
                HandleType.PutField
            }
            lex.isNext(keyPutStatic) -> {
                lex.read(keyPutStatic)
                HandleType.PutStatic
            }
            lex.isNext(keyInvokeVirtual) -> {
                lex.read(keyInvokeVirtual)
                HandleType.InvokeVirtual
            }
            lex.isNext(keyInvokeStatic) -> {
                lex.read(keyInvokeStatic)
                HandleType.InvokeStatic
            }
            lex.isNext(keyInvokeSpecial) -> {
                lex.read(keyInvokeSpecial)
                HandleType.InvokeSpecial
            }
            lex.isNext(keyNewInvokeSpecial) -> {
                lex.read(keyNewInvokeSpecial)
                HandleType.NewInvokeSpecial
            }
            lex.isNext(keyInvokeInterface) -> {
                lex.read(keyInvokeInterface)
                HandleType.InvokeInterface
            }
            else -> lex.unexpectTokenError(keyGetField, keyGetStatic, keyPutField, keyPutStatic, keyInvokeVirtual, keyInvokeStatic, keyInvokeSpecial, keyNewInvokeSpecial, keyInvokeInterface)
        }

        val isInterfaceMethod = if (lex.isNext(keyInterface)) {
            lex.read(keyInterface)
            true
        } else false
        val (owner, name) = lex.read(TokenType.OwnerAndName)
        val desc = lex.read(TokenType.MethodDescriptor)
        Handle(handleType, owner, name, desc, isInterfaceMethod)
    }

    val label = grammar(TokenType.Name) { LabelName(lex.read(TokenType.Name)) }

    val label_definition = grammar(TokenType.Name) {
        val name = lex.read(TokenType.Name)
        lex.read(colon)
        LabelDefinition(LabelName(name))
    }

    val field_block = grammar(dotField) {
        lex.read(dotField)
        val access = access_flags()
        val name = lex.read(TokenType.Name)
        val descriptor = lex.read(TokenType.FieldDescriptor)
        val default = if (lex.isNext(equal)) {
            lex.read(equal)
            field_constant_value()
        } else {
            null
        }
        if (!lex.isNext(field_attribute.start) && lex.isNext(dotEnd)) return@grammar FieldBlock(
            access,
            name,
            descriptor,
            default,
            emptyList()
        )
        val attributes = field_attribute.many()
        lex.read(dotEnd)
        lex.read(keyField)
        FieldBlock(access, name, descriptor, default, attributes)
    }

    val field_attribute = grammar({
        setOf(
            dotDeprecated,
            dotSignature
        ) + member_annotation_block.start
    }) {
        when {
            lex.isNext(dotDeprecated) -> {
                lex.read(dotDeprecated)
                FieldDeprecated()
            }
            lex.isNext(dotSignature) -> {
                lex.read(dotSignature)
                FieldSignature(lex.read(TokenType.String))
            }
            lex.isNext(member_annotation_block.start) -> {
                FieldAnnotation(member_annotation_block())
            }
            else -> lex.unexpectTokenError(
                setOf(
                    dotDeprecated,
                    dotSignature
                ), annotation_block.start
            )
        }
    }

    val member_annotation_block = grammar(dotAnnotation) {
        lex.read(dotAnnotation)
        val type = annotation_type()
        val className = lex.read(TokenType.InternalName)
        val pairs = mutableListOf<Pair<String, AnnotationValue>>()
        if (type is AnnotationType.Default) {
            pairs.add("" to annotation_pair())
        } else {
            while (!lex.isNext(dotEnd)) {
                val name = lex.read(TokenType.Name)
                val pair = annotation_pair()
                pairs.add(name to pair)
            }
        }
        lex.read(dotEnd)
        lex.read(keyAnnotation)
        Annotation(type, className, pairs)
    }

    val annotation_block = grammar(dotAnnotation) {
        lex.read(dotAnnotation)
        val className = lex.read(TokenType.InternalName)
        val pairs = mutableListOf<Pair<String, AnnotationValue>>()
        while (!lex.isNext(dotEnd)) {
            val name = lex.read(TokenType.Name)
            val pair = annotation_pair()
            pairs.add(name to pair)
        }
        lex.read(dotEnd)
        lex.read(keyAnnotation)
        Annotation(AnnotationType.Invisible(), className, pairs)
    }

    val annotation_array_block = grammar(dotAnnotation) {
        lex.read(dotAnnotation)
        lex.read(keyArray)
        val list = annotation_pair.many()
        lex.read(dotEnd)
        lex.read(keyAnnotation)
        AnnotationArray(list)
    }

    val annotation_type = grammar(keyVisible, keyInvisible, keyVisiblePararm, keyInvisiblePararm, keyDefault) {
        when {
            lex.isNext(keyVisible) -> {
                lex.read(keyVisible)
                AnnotationType.Visible()
            }
            lex.isNext(keyInvisible) -> {
                lex.read(keyInvisible)
                AnnotationType.Invisible()
            }
            lex.isNext(keyVisiblePararm) -> {
                lex.read(keyVisiblePararm)
                val pararm = lex.read(TokenType.Integer)
                AnnotationType.VisiblePararm(pararm)
            }
            lex.isNext(keyInvisiblePararm) -> {
                lex.read(keyInvisiblePararm)
                val pararm = lex.read(TokenType.Integer)
                AnnotationType.InvisiblePararm(pararm)
            }
            lex.isNext(keyDefault) -> {
                lex.read(keyDefault)
                AnnotationType.Default()
            }
            else -> lex.unexpectTokenError(keyVisible, keyInvisible, keyVisiblePararm, keyInvisiblePararm, keyDefault)
        }
    }

    val annotation_pair: Grammar<AnnotationValue> = grammar(v_byte, v_char, v_double, v_float, v_int, v_long, v_short, v_boolean, v_String, v_Enum, v_Class, v_Annotation, v_Array) {
        when {
            lex.isNext(v_byte) -> {
                lex.read(v_byte)
                lex.read(equal)
                Value(lex.read(TokenType.Integer).toByte())
            }
            lex.isNext(v_char) -> {
                lex.read(v_char)
                lex.read(equal)
                Value(lex.read(TokenType.Integer).toChar())
            }
            lex.isNext(v_double) -> {
                lex.read(v_double)
                lex.read(equal)
                Value(lex.read(TokenType.Double))
            }
            lex.isNext(v_float) -> {
                lex.read(v_float)
                lex.read(equal)
                Value(lex.read(TokenType.Float))
            }
            lex.isNext(v_int) -> {
                lex.read(v_int)
                lex.read(equal)
                Value(lex.read(TokenType.Integer))
            }
            lex.isNext(v_long) -> {
                lex.read(v_long)
                lex.read(equal)
                Value(lex.read(TokenType.Long))
            }
            lex.isNext(v_short) -> {
                lex.read(v_short)
                lex.read(equal)
                Value(lex.read(TokenType.Integer).toShort())
            }
            lex.isNext(v_boolean) -> {
                lex.read(v_boolean)
                lex.read(equal)
                boolean()
            }
            lex.isNext(v_String) -> {
                lex.read(v_String)
                lex.read(equal)
                Value(lex.read(TokenType.String))
            }
            lex.isNext(v_Enum) -> {
                lex.read(v_Enum)
                lex.read(equal)
                annotation_enum()
            }
            lex.isNext(v_Class) -> {
                lex.read(v_Class)
                lex.read(equal)
                ClassName(lex.read(TokenType.FieldDescriptor))
            }
            lex.isNext(v_Annotation) -> {
                lex.read(v_Annotation)
                lex.read(equal)
                annotation_block()
            }
            lex.isNext(v_Array) -> {
                lex.read(v_Array)
                lex.read(equal)
                annotation_array_block()
            }
            else -> lex.unexpectTokenError(v_byte, v_char, v_double, v_float, v_int, v_long, v_short, v_boolean, v_String, v_Enum, v_Class, v_Annotation, v_Array)
        }
    }

    val annotation_enum = grammar(TokenType.InternalName) {
        val internalName = lex.read(TokenType.InternalName)
        val name = lex.read(TokenType.Name)
        AnnotationEnum(internalName, name)
    }

    val boolean = grammar(keyTrue, keyFalse) {
        when {
            lex.isNext(keyTrue) -> {
                lex.read(keyTrue)
                Value(true)
            }
            lex.isNext(keyFalse) -> {
                lex.read(keyFalse)
                Value(false)
            }
            else -> lex.unexpectTokenError(keyTrue, keyFalse)
        }
    }

    val field_constant_value = grammar(
        TokenType.Integer,
        TokenType.Double,
        TokenType.Long,
        TokenType.Float,
        TokenType.String
    ) {
        when {
            lex.isNext(TokenType.Integer) -> Value(lex.read(TokenType.Integer))
            lex.isNext(TokenType.Double) -> Value(lex.read(TokenType.Double))
            lex.isNext(TokenType.Long) -> Value(lex.read(TokenType.Long))
            lex.isNext(TokenType.Float) -> Value(lex.read(TokenType.Float))
            lex.isNext(TokenType.String) -> Value(lex.read(TokenType.String))
            else -> lex.unexpectTokenError(
                TokenType.Integer,
                TokenType.Double,
                TokenType.Long,
                TokenType.Float,
                TokenType.String
            )
        }
    }

    val access_flags = grammar() { AccessFlags(access_flag.many()) }

    val access_flag = grammar(
        keyPublic,
        keyPrivate,
        keyProtected,
        keyStatic,
        keyFinal,
        keySuper,
        keySynchronized,
        keyVolatile,
        keyBridge,
        keyVarargs,
        keyTransient,
        keyNative,
        keyInterface,
        keyAbstract,
        keyStrict,
        keySynthetic,
        keyAnnotation,
        keyEnum,
        keyMandated
    ) {
        when {
            lex.isNext(keyPublic) -> {
                lex.read(keyPublic)
                AccessFlag.Public()
            }
            lex.isNext(keyPrivate) -> {
                lex.read(keyPrivate)
                AccessFlag.Private()
            }
            lex.isNext(keyProtected) -> {
                lex.read(keyProtected)
                AccessFlag.Protected()
            }
            lex.isNext(keyStatic) -> {
                lex.read(keyStatic)
                AccessFlag.Static()
            }
            lex.isNext(keyFinal) -> {
                lex.read(keyFinal)
                AccessFlag.Final()
            }
            lex.isNext(keySuper) -> {
                lex.read(keySuper)
                AccessFlag.Super()
            }
            lex.isNext(keySynchronized) -> {
                lex.read(keySynchronized)
                AccessFlag.Synchronized()
            }
            lex.isNext(keyVolatile) -> {
                lex.read(keyVolatile)
                AccessFlag.Volatile()
            }
            lex.isNext(keyBridge) -> {
                lex.read(keyBridge)
                AccessFlag.Bridge()
            }
            lex.isNext(keyVarargs) -> {
                lex.read(keyVarargs)
                AccessFlag.Varargs()
            }
            lex.isNext(keyTransient) -> {
                lex.read(keyTransient)
                AccessFlag.Transient()
            }
            lex.isNext(keyNative) -> {
                lex.read(keyNative)
                AccessFlag.Native()
            }
            lex.isNext(keyInterface) -> {
                lex.read(keyInterface)
                AccessFlag.Interface()
            }
            lex.isNext(keyAbstract) -> {
                lex.read(keyAbstract)
                AccessFlag.Abstract()
            }
            lex.isNext(keyStrict) -> {
                lex.read(keyStrict)
                AccessFlag.Strict()
            }
            lex.isNext(keySynthetic) -> {
                lex.read(keySynthetic)
                AccessFlag.Synthetic()
            }
            lex.isNext(keyAnnotation) -> {
                lex.read(keyAnnotation)
                AccessFlag.Annotation()
            }
            lex.isNext(keyEnum) -> {
                lex.read(keyEnum)
                AccessFlag.Enum()
            }
            lex.isNext(keyMandated) -> {
                lex.read(keyMandated)
                AccessFlag.Mandated()
            }
            else -> lex.unexpectTokenError(
                keyPublic,
                keyPrivate,
                keyProtected,
                keyStatic,
                keyFinal,
                keySuper,
                keySynchronized,
                keyVolatile,
                keyBridge,
                keyVarargs,
                keyTransient,
                keyNative,
                keyInterface,
                keyAbstract,
                keyStrict,
                keySynthetic,
                keyAnnotation,
                keyEnum,
                keyMandated
            )
        }
    }

    companion object {
        val dotBytecode = TokenType.DotedKeyWord("bytecode")
        val dotSource = TokenType.DotedKeyWord("source")
        val dotClass = TokenType.DotedKeyWord("class")
        val dotSuper = TokenType.DotedKeyWord("super")
        val dotImplements = TokenType.DotedKeyWord("implements")
        val dotSignature = TokenType.DotedKeyWord("signature")
        val dotEnclosing = TokenType.DotedKeyWord("enclosing")
        val dotDebug = TokenType.DotedKeyWord("debug")
        val dotInner = TokenType.DotedKeyWord("inner")
        val dotMethod = TokenType.DotedKeyWord("method")
        val dotEnd = TokenType.DotedKeyWord("end")
        val dotLimit = TokenType.DotedKeyWord("limit")
        val dotLine = TokenType.DotedKeyWord("line")
        val dotVar = TokenType.DotedKeyWord("var")
        val dotThrows = TokenType.DotedKeyWord("throws")
        val dotCatch = TokenType.DotedKeyWord("catch")
        val dotDeprecated = TokenType.DotedKeyWord("deprecated")
        val dotStack = TokenType.DotedKeyWord("stack")
        val dotAnnotation = TokenType.DotedKeyWord("annotation")
        val dotField = TokenType.DotedKeyWord("field")

        val keyTrue = TokenType.KeyWord("true")
        val keyFalse = TokenType.KeyWord("false")

        val keyMethod = TokenType.KeyWord("method")
        val keyClass = TokenType.KeyWord("class")
        val keyInner = TokenType.KeyWord("inner")
        val keyOuter = TokenType.KeyWord("outer")
        val keyStack = TokenType.KeyWord("stack")
        val keyLocal = TokenType.KeyWord("local")
        val keyIs = TokenType.KeyWord("is")
        val keySignature = TokenType.KeyWord("signature")
        val keyFrom = TokenType.KeyWord("from")
        val keyTo = TokenType.KeyWord("to")
        val keyUsing = TokenType.KeyWord("using")
        val keyField = TokenType.KeyWord("field")
        val keyHandle = TokenType.KeyWord("handle")
        val keyMethodType = TokenType.KeyWord("methodtype")

        val keyUse = TokenType.KeyWord("use")
        val keyLocals = TokenType.KeyWord("locals")
        val keyStacks = TokenType.KeyWord("stacks")
        val keyDefault = TokenType.KeyWord("default")

        // handle type
        val keyGetField = TokenType.KeyWord("getfield")
        val keyGetStatic = TokenType.KeyWord("getstatic")
        val keyPutField = TokenType.KeyWord("putfield")
        val keyPutStatic = TokenType.KeyWord("putstatic")
        val keyInvokeVirtual = TokenType.KeyWord("invokevirtual")
        val keyInvokeStatic = TokenType.KeyWord("invokestatic")
        val keyInvokeSpecial = TokenType.KeyWord("invokespecial")
        val keyNewInvokeSpecial = TokenType.KeyWord("newinvokespecial")
        val keyInvokeInterface = TokenType.KeyWord("invokeinterface")

        // annotation target
        val keyVisible = TokenType.KeyWord("visible")
        val keyInvisible = TokenType.KeyWord("invisible")
        val keyVisiblePararm = TokenType.KeyWord("visiblepararm")
        val keyInvisiblePararm = TokenType.KeyWord("invisiblepararm")
        //val keyDefault = Token.KeyWord("default")

        val keyTop = TokenType.KeyWord("Top")
        val keyInteger = TokenType.KeyWord("Integer")
        val keyFloat = TokenType.KeyWord("Float")
        val keyLong = TokenType.KeyWord("Long")
        val keyDouble = TokenType.KeyWord("Double")
        val keyNull = TokenType.KeyWord("Null")
        val keyUninitializedThis = TokenType.KeyWord("UninitializedThis")
        val keyObject = TokenType.KeyWord("Object")
        val keyUninitialized = TokenType.KeyWord("Uninitialized")

        val keyPublic = TokenType.KeyWord("public")
        val keyPrivate = TokenType.KeyWord("private")
        val keyProtected = TokenType.KeyWord("protected")
        val keyStatic = TokenType.KeyWord("static")
        val keyFinal = TokenType.KeyWord("final")
        val keySuper = TokenType.KeyWord("super")
        val keySynchronized = TokenType.KeyWord("synchronized")
        val keyVolatile = TokenType.KeyWord("volatile")
        val keyBridge = TokenType.KeyWord("bridge")
        val keyVarargs = TokenType.KeyWord("varargs")
        val keyTransient = TokenType.KeyWord("transient")
        val keyNative = TokenType.KeyWord("native")
        val keyInterface = TokenType.KeyWord("interface")
        val keyAbstract = TokenType.KeyWord("abstract")
        val keyStrict = TokenType.KeyWord("strict")
        val keySynthetic = TokenType.KeyWord("synthetic")
        val keyArray = TokenType.KeyWord("array")
        val keyAnnotation = TokenType.KeyWord("annotation")
        val keyEnum = TokenType.KeyWord("enum")
        val keyMandated = TokenType.KeyWord("mandated")

        val colon = TokenType.Char(':')
        val equal = TokenType.Char('=')
        val dot = TokenType.Char('.')
        val asterisks = TokenType.Char('*')

        // annotation value type
        val v_byte = TokenType.Char('B')
        val v_char = TokenType.Char('C')
        val v_double = TokenType.Char('D')
        val v_float = TokenType.Char('F')
        val v_int = TokenType.Char('I')
        val v_long = TokenType.Char('J')
        val v_short = TokenType.Char('S')
        val v_boolean = TokenType.Char('Z')
        val v_String = TokenType.Char('s')
        val v_Enum = TokenType.Char('e')
        val v_Class = TokenType.Char('c')
        val v_Annotation = TokenType.Char('@')
        val v_Array = TokenType.Char('[')
    }
}
