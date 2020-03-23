package com.anatawa12.jasm.assembler

import com.anatawa12.jasm.DeprecatedAttribute
import com.anatawa12.jasm.tree.*
import com.anatawa12.jasm.tree.Annotation
import com.anatawa12.jasm.tree.Handle
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*

class Assembler(val options: AssemblerOptions) {
    val classWriter = ClassWriter(0)

    fun assemble(file: JasmFile) {
        classWriter.visit(assembleVersion(file.classHeader.bytecode), assembleAccess(file.classHeader.className.accessFlags),
            file.classHeader.className.internalName, file.classHeader.signature?.signature,
            file.classHeader.superName?.internalName, file.classHeader.implements.map { it.internalName }.toTypedArray())
        assembleHeader(file.classHeader)
        for (element in file.elements) {
            when (element) {
                is ClassAnnotation-> {
                    when (element.annotation.type) {
                        is AnnotationType.Visible -> {
                            assembleAnnotation(element.annotation, classWriter.visitAnnotation("L${element.annotation.internalName};", true))
                        }
                        is AnnotationType.Invisible -> {
                            assembleAnnotation(element.annotation, classWriter.visitAnnotation("L${element.annotation.internalName};", false))
                        }
                        is AnnotationType.VisiblePararm,
                        is AnnotationType.InvisiblePararm,
                        is AnnotationType.Default -> error("invalid annotation type for field")
                    }
                }
                is ClassDeprecated -> {
                    classWriter.visitAttribute(DeprecatedAttribute)
                }
            }
        }
        for (element in file.elements) {
            when (element) {
                is MethodBlock -> {
                    assembleMethod(element)
                }
                is FieldBlock -> {
                    assembleField(element)
                }
            }
        }
        classWriter.visitEnd()
    }

    private fun assembleHeader(classHeader: ClassHeader) {
        classHeader.enclosing?.let { enclosing ->
            when (enclosing) {
                is EnclosingClassDirective -> classWriter.visitOuterClass(enclosing.owner, null, null)
                is EnclosingMethodDirective -> classWriter.visitOuterClass(enclosing.owner, enclosing.name, enclosing.descriptor)
            }
        }

        if (classHeader.source != null || classHeader.debug != null) {
            classWriter.visitSource(classHeader.source?.sourceFile, classHeader.debug?.debugData)
        }

        for (innerClass in classHeader.innerClasses) {
            classWriter.visitInnerClass(innerClass.name, innerClass.outerName, innerClass.innerName, assembleAccess(innerClass.accessFlags))
        }
    }

    private fun assembleVersion(bytecode: BytecodeDirective?): Int {
        if (bytecode == null) return V1_8
        return bytecode.minor shl 16 or bytecode.major
    }

    private fun assembleMethod(method: MethodBlock) {
        var signature: String? = null
        var maxStack = -1
        var maxLocal = -1
        val throws = mutableListOf<String>()
        var isDeprecated = false
        var annotationDefault: Annotation? = null
        val annotations = mutableListOf<Annotation>()

        for (statement in method.statements) {
            when (statement) {
                is StackLimit -> maxStack = statement.maxStack
                is LocalLimit -> maxLocal = statement.maxLocal
                is MethodSignature -> signature = statement.methodSignature
                is MethodDeprecated -> isDeprecated = true
                is MethodAnnotation -> {
                    if (statement.annotation.type is AnnotationType.Default) annotationDefault = statement.annotation
                    else annotations += statement.annotation
                }

                is LineNumber,
                is LocalVar,
                is Throws,
                is TryCatch,
                is LabelDefinition,
                is StackFrame,
                is Instruction -> {
                }
            }
        }

        val methodWriter = classWriter.visitMethod(assembleAccess(method.accessFlags),
            method.name, method.descriptor, signature, throws.toTypedArray())

        if (annotationDefault != null) {
            assembleAnnotation(annotationDefault, methodWriter.visitAnnotationDefault())
        }

        for (annotation in annotations) {
            when (annotation.type) {
                is AnnotationType.Visible -> {
                    assembleAnnotation(annotation, methodWriter.visitAnnotation("L${annotation.internalName};", true))
                }
                is AnnotationType.Invisible -> {
                    assembleAnnotation(annotation, methodWriter.visitAnnotation("L${annotation.internalName};", false))
                }
                is AnnotationType.VisiblePararm -> {
                    assembleAnnotation(annotation, methodWriter.visitParameterAnnotation(annotation.type.pararmIndex, "L${annotation.internalName};", true))
                }
                is AnnotationType.InvisiblePararm -> {
                    assembleAnnotation(annotation, methodWriter.visitParameterAnnotation(annotation.type.pararmIndex, "L${annotation.internalName};", false))
                }
                is AnnotationType.Default -> error("invalid annotation type for method")
            }
        }

        if (isDeprecated) methodWriter.visitAttribute(DeprecatedAttribute)

        if (maxLocal != -1)
            assembleCode(methodWriter, method.statements, maxStack, maxLocal)

        methodWriter.visitEnd()
    }

    private fun assembleCode(methodWriter: MethodVisitor, statements: List<MethodStatement>, maxStack: Int, maxLocal: Int) {
        methodWriter.visitCode()
        var lastLocals: List<StackFrameType>? = null
        val table = LabelTable()
        loop@ for (statement in statements) {
            if (options.autoLine && statement is Instruction) {
                val label = Label()
                methodWriter.visitLabel(label)
                methodWriter.visitLineNumber(statement.tokens.first().begin.line, label)
            }
            when (statement) {
                is LineNumber -> {
                    if (options.autoLine) continue@loop
                    val label = Label()
                    methodWriter.visitLabel(label)
                    methodWriter.visitLineNumber(statement.line, label)
                }
                is LocalVar -> {
                    methodWriter.visitLocalVariable(statement.name, statement.descriptor, statement.signature,
                        table[statement.from], table[statement.to], statement.variable)
                }
                is TryCatch -> {
                    methodWriter.visitTryCatchBlock(table[statement.from], table[statement.to],
                        table[statement.using], statement.exception)
                }
                is LabelDefinition -> {
                    methodWriter.visitLabel(table[statement.name])
                }
                is StackFrame -> {
                    val locals = statement.locals ?: lastLocals!!
                    val stacks = statement.stacks
                    lastLocals = locals
                    methodWriter.visitFrame(F_NEW, locals.size, assembleFrame(locals, table), stacks.size, assembleFrame(stacks, table))
                }

                is Insn -> {
                    methodWriter.visitInsn(statement.opcode)
                }
                is IntInsn -> {
                    methodWriter.visitIntInsn(statement.opcode, statement.operand)
                }
                is VarInsn -> {
                    methodWriter.visitVarInsn(statement.opcode, statement.variable)
                }
                is TypeInsn -> {
                    methodWriter.visitTypeInsn(statement.opcode, statement.type)
                }
                is FieldInsn -> {
                    methodWriter.visitFieldInsn(statement.opcode, statement.owner, statement.name, statement.desc)
                }
                is MethodInsn -> {
                    methodWriter.visitMethodInsn(statement.opcode, statement.owner, statement.name, statement.desc, statement.itf || statement.opcode == INVOKEINTERFACE)
                }
                is InvokeDynamicInsn -> {
                    methodWriter.visitInvokeDynamicInsn(
                        statement.name, statement.desc,
                        assembleHandle(statement.bsm),
                        *Array(statement.bsmArgs.size) { assembleLdcConstant(statement.bsmArgs[it]) }
                    )
                }
                is JumpInsn -> {
                    methodWriter.visitJumpInsn(statement.opcode, table[statement.label])
                }
                is LdcInsn -> {
                    methodWriter.visitLdcInsn(assembleLdcConstant(statement.cst))
                }
                is IincInsn -> {
                    methodWriter.visitIincInsn(statement.variable, statement.increment)
                }
                is TableSwitchInsn -> {
                    methodWriter.visitTableSwitchInsn(statement.min, statement.min + statement.labels.size - 1,
                        table[statement.dflt], *Array(statement.labels.size) { table[statement.labels[it]] })
                }
                is LookupSwitchInsn -> {
                    methodWriter.visitLookupSwitchInsn(table[statement.dflt],
                        IntArray(statement.pairs.size) { statement.pairs[it].first },
                        Array(statement.pairs.size) { table[statement.pairs[it].second] })

                }
                is MultiANewArrayInsn -> {
                    methodWriter.visitMultiANewArrayInsn(statement.desc, statement.dims)
                }

                is StackLimit, is LocalLimit, is Throws, is MethodSignature, is MethodDeprecated, is MethodAnnotation -> {}
            }
        }
        methodWriter.visitMaxs(maxStack, maxLocal)
    }

    private fun assembleLdcConstant(cst: BranchNode): Any? = when (cst) {
        is Value<*> -> cst.value
        is ClassName -> Type.getType(cst.desc)
        is MethodType -> Type.getType(cst.desc)
        is Handle -> assembleHandle(cst)
        else -> error("unsupported cst type: ${cst.javaClass}")
    }

    private fun assembleHandle(bsm: Handle): org.objectweb.asm.Handle? {
        val type = when (bsm.type) {
            HandleType.GetField -> H_GETFIELD
            HandleType.GetStatic -> H_GETSTATIC
            HandleType.PutField -> H_PUTFIELD
            HandleType.PutStatic -> H_PUTSTATIC
            HandleType.InvokeVirtual -> H_INVOKEVIRTUAL
            HandleType.InvokeStatic -> H_INVOKESTATIC
            HandleType.InvokeSpecial -> H_INVOKESPECIAL
            HandleType.NewInvokeSpecial -> H_NEWINVOKESPECIAL
            HandleType.InvokeInterface -> H_INVOKEINTERFACE
        }
        return org.objectweb.asm.Handle(type, bsm.owner, bsm.name, bsm.desc, bsm.itf)
    }

    private fun assembleFrame(locals: List<StackFrameType>, table: LabelTable): Array<Any?> {
        return Array(locals.size) {
            when (val local = locals[it]) {
                is StackFrameType.Top -> TOP
                is StackFrameType.Integer -> INTEGER
                is StackFrameType.Float -> FLOAT
                is StackFrameType.Long -> LONG
                is StackFrameType.Double -> DOUBLE
                is StackFrameType.Null -> NULL
                is StackFrameType.UninitializedThis -> UNINITIALIZED_THIS
                is StackFrameType.Object -> local.internalName
                is StackFrameType.Uninitialized -> table[local.madeAt]
            }
        }
    }

    private fun assembleField(field: FieldBlock) {
        var signature: String? = null
        var isDeprecated = false
        val annotations = mutableListOf<Annotation>()
        for (attribute in field.attributes) {
            when (attribute) {
                is FieldDeprecated -> isDeprecated = true
                is FieldSignature -> signature = attribute.signature
                is FieldAnnotation -> annotations += attribute.annotation
            }
        }

        val fieldWriter = classWriter.visitField(assembleAccess(field.accessFlags),
            field.name, field.descriptor, signature, field.default?.value)

        if (isDeprecated) fieldWriter.visitAttribute(DeprecatedAttribute)

        for (annotation in annotations) {
            when (annotation.type) {
                is AnnotationType.Visible -> {
                    assembleAnnotation(annotation, fieldWriter.visitAnnotation("L${annotation.internalName};", true))
                }
                is AnnotationType.Invisible -> {
                    assembleAnnotation(annotation, fieldWriter.visitAnnotation("L${annotation.internalName};", false))
                }
                is AnnotationType.VisiblePararm,
                is AnnotationType.InvisiblePararm,
                is AnnotationType.Default -> error("invalid annotation type for field")
            }
        }

        fieldWriter.visitEnd()
    }

    private fun assembleAnnotationValue(key: String?, value: AnnotationValue, annotationWriter: AnnotationVisitor) {
        when (value) {
            is Value<*> -> annotationWriter.visit(key, value.value)
            is AnnotationEnum -> annotationWriter.visitEnum(key, "L${value.internalName};", value.name)
            is ClassName -> annotationWriter.visit(key, Type.getType(value.desc))
            is Annotation -> assembleAnnotation(value, annotationWriter.visitAnnotation(key, "L${value.internalName};"))
            is AnnotationArray -> assembleAnnotationArray(value, annotationWriter.visitArray(key))
        }
    }

    private fun assembleAnnotation(annotation: Annotation, annotationWriter: AnnotationVisitor) {
        for ((name, value) in annotation.pairs) {
            assembleAnnotationValue(name, value, annotationWriter)
        }
        annotationWriter.visitEnd()
    }

    private fun assembleAnnotationArray(array: AnnotationArray, annotationWriter: AnnotationVisitor) {
        for (value in array) {
            assembleAnnotationValue(null, value, annotationWriter)
        }
        annotationWriter.visitEnd()
    }

    private inline fun <reified E> List<*>.firstOrNull() = firstOrNull<E> { true }
    private inline fun <reified E> List<*>.firstOrNull(filter: (E) -> Boolean) = (this as Iterable<*>).firstOrNull { it is E && filter(it) } as E?

    private fun assembleAccess(accessFlags: AccessFlags): Int {
        var access = 0
        for (flag in accessFlags.flags) {
            access = access or when (flag) {
                is AccessFlag.Public -> ACC_PUBLIC
                is AccessFlag.Private -> ACC_PRIVATE
                is AccessFlag.Protected -> ACC_PROTECTED
                is AccessFlag.Static -> ACC_STATIC
                is AccessFlag.Final -> ACC_FINAL
                is AccessFlag.Super -> ACC_SUPER
                is AccessFlag.Synchronized -> ACC_SYNCHRONIZED
                is AccessFlag.Volatile -> ACC_VOLATILE
                is AccessFlag.Bridge -> ACC_BRIDGE
                is AccessFlag.Varargs -> ACC_VARARGS
                is AccessFlag.Transient -> ACC_TRANSIENT
                is AccessFlag.Native -> ACC_NATIVE
                is AccessFlag.Interface -> ACC_INTERFACE
                is AccessFlag.Abstract -> ACC_ABSTRACT
                is AccessFlag.Strict -> ACC_STRICT
                is AccessFlag.Synthetic -> ACC_SYNTHETIC
                is AccessFlag.Annotation -> ACC_ANNOTATION
                is AccessFlag.Enum -> ACC_ENUM
                is AccessFlag.Mandated -> ACC_MANDATED
            }
        }
        return access
    }

    private class LabelTable {
        private val table = mutableMapOf<String, Label>()
        operator fun get(name: LabelName): Label = table.getOrPut(name.name, ::Label)
    }
}
