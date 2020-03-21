package com.anatawa12.jasm.assembler

import com.anatawa12.jasm.tree.*
import com.anatawa12.jasm.tree.Annotation
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*

class Assembler(val options: AssemblerOptions) {
    val classWriter = ClassWriter(0)

    fun assemble(file: JasmFile) {
        classWriter.visit(assembleVersion(file.jasmHeader.bytecode), assembleAccess(file.jasmHeader.className.accessFlags),
            file.jasmHeader.className.internalName, file.jasmHeader.signature?.signature,
            file.jasmHeader.superName?.internalName, file.jasmHeader.implements.map { it.internalName }.toTypedArray())
        assembleHeader(file.jasmHeader)
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

    private fun assembleHeader(jasmHeader: JasmHeader) {
        jasmHeader.enclosing?.let { enclosing ->
            when (enclosing) {
                is EnclosingClassDirective -> classWriter.visitOuterClass(enclosing.owner, null, null)
                is EnclosingMethodDirective -> classWriter.visitOuterClass(enclosing.owner, enclosing.name, enclosing.descriptor)
            }
        }

        if (jasmHeader.source != null || jasmHeader.debug != null) {
            classWriter.visitSource(jasmHeader.source?.sourceFile, jasmHeader.debug?.debugData)
        }

        for (innerClass in jasmHeader.innerClasses) {
            classWriter.visitInnerClass(innerClass.name, innerClass.outerName, innerClass.innerName, assembleAccess(innerClass.accessFlags))
        }
    }

    private fun assembleVersion(bytecode: BytecodeDirective?): Int {
        if (bytecode == null) return V1_8
        return bytecode.minor shl 16 + bytecode.major
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

        // TODO: Deprecated attribute

        if (maxLocal != -1)
            assembleCode(methodWriter, method.statements, maxStack, maxLocal)

        methodWriter.visitEnd()
    }

    private fun assembleCode(methodWriter: MethodVisitor, statements: List<MethodStatement>, maxStack: Int, maxLocal: Int) {
        methodWriter.visitCode()
        var lastLocals: List<StackFrameType>? = null
        val table = LabelTable()
        for (statement in statements) {
            when (statement) {
                is LineNumber -> {
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
                    methodWriter.visitFrame(F_FULL, locals.size, assembleFrame(locals, table), stacks.size, assembleFrame(stacks, table))
                }
                is Insn -> TODO()
                is IntInsn -> TODO()
                is FieldInsn -> TODO()
                is IincInsn -> TODO()
                is InvokeDynamicInsn -> TODO()
                is JumpInsn -> TODO()
                is LdcInsn -> TODO()
                is LookupSwitchInsn -> TODO()
                is MethodInsn -> TODO()
                is MultiANewArrayInsn -> TODO()
                is TableSwitchInsn -> TODO()
                is TypeInsn -> TODO()
                is VarInsn -> TODO()

                is StackLimit, is LocalLimit, is Throws, is MethodSignature, is MethodDeprecated, is MethodAnnotation -> {}
            }
        }
        methodWriter.visitMaxs(maxStack, maxLocal)
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

        // TODO: Deprecated attribute

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
