package com.anatawa12.jasm.disassembler

import com.anatawa12.jasm.DeprecatedAttribute
import com.anatawa12.jasm.tree.AnnotationType
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

class Disassembler {
    val file = Printer()
    fun disassemble(classNode: ClassNode) {
        file.key(".bytecode").key("${classNode.version and 0xFFFFF}.${classNode.version shr 16}").nl()
        if (classNode.sourceFile != null) file.key(".source").str(classNode.sourceFile).nl()
        file.key(".class").also { disassembleAccess(classNode.access, AccessTo.Class) }.name(classNode.name).nl()
        if (classNode.superName != null) file.key(".super").name(classNode.superName).nl()
        for (interfaceName in classNode.interfaces) file.key(".implements").name(interfaceName).nl()
        if (classNode.signature != null) file.key(".signature").str(classNode.signature).nl()
        if (classNode.outerClass != null) {
            if (classNode.outerMethod != null && classNode.outerMethodDesc != null)
                file.key(".enclosing").key("method").name(classNode.outerClass).name(classNode.outerMethod).name(classNode.outerMethodDesc).nl()
            else
                file.key(".enclosing").key("class").name(classNode.outerClass).nl()
        }
        if (classNode.sourceDebug != null) file.key(".debug").str(classNode.sourceDebug).nl()
        for (inner in classNode.innerClasses) {
            file.key(".inner").also { disassembleAccess(inner.access, AccessTo.Class) }.name(inner.name)
                .also { if (inner.innerName != null) it.key("inner").name(inner.innerName) }
                .also { if (inner.outerName != null) it.key("outer").name(inner.outerName) }.nl()
        }

        file.nl()

        if (classNode.visibleAnnotations != null)
            for (annotation in classNode.visibleAnnotations) {
                disassembleAnnotation(annotation, AnnotationType.Visible())
                file.nl()
            }
        if (classNode.invisibleAnnotations != null)
            for (annotation in classNode.invisibleAnnotations) {
                disassembleAnnotation(annotation, AnnotationType.Invisible())
                file.nl()
            }
        for (method in classNode.methods) {
            disassembleMethod(method)
            file.nl()
        }
        for (field in classNode.fields) {
            disassembleField(field)
            file.nl()
        }
    }

    private fun disassembleAnnotationValue(value: Any) {
        when (value) {
            is Byte -> file.key("B").key("=").key("$value").nl()
            is Char -> file.key("C").key("=").key("${value.toInt()}").nl()
            is Double -> file.key("D").key("=").key("$value").nl()
            is Float -> file.key("F").key("=").key("${value}f").nl()
            is Int -> file.key("I").key("=").key("$value").nl()
            is Long -> file.key("J").key("=").key("${value}l").nl()
            is Short -> file.key("S").key("=").key("$value").nl()

            is Boolean -> file.key("z").key("=").key("$value").nl()
            is String -> file.key("s").key("=").str(value).nl()
            is Type -> file.key("c").key("=").str(value.descriptor).nl()
            is Array<*> -> {
                file.key("e").key("=").name(Type.getType(value[0] as String).internalName).name(value[1] as String).nl()
            }
            is AnnotationNode -> {
                file.key("@").key("=")
                disassembleAnnotation(value, null)
            }
            is List<*> -> {
                file.key("[").key("=")
                disassembleAnnotationArray(value)
            }
        }
    }

    private fun disassembleAnnotationArray(values: List<*>) {
        file.key(".annotation").key("array").nl()
        file.indent()
        for (value in values) {
            disassembleAnnotationValue(value!!)
        }
        file.outdent()
        file.key(".end").key("annotation").nl()
    }

    private fun disassembleAnnotation(method: AnnotationNode, type: AnnotationType?) {
        file.key(".annotation").also {
            when (type) {
                is AnnotationType.Visible -> it.key("visible")
                is AnnotationType.Invisible -> it.key("invisible")
                is AnnotationType.VisiblePararm -> it.key("visiblepararm").key("${type.pararmIndex}")
                is AnnotationType.InvisiblePararm -> it.key("invisiblepararm").key("${type.pararmIndex}")
                is AnnotationType.Default -> it.key("default")
                null -> {}
            }
        }.name(Type.getType(method.desc).internalName).nl()
        file.indent()
        if (type is AnnotationType.Default) {
            // TODO
        } else {
            for ((key, value) in method.values.orEmpty().chunked(2)) {
                file.name(key as String)
                disassembleAnnotationValue(value)
            }
        }
        file.outdent()
        file.key(".end").key("annotation").nl()
    }

    private fun disassembleMethod(method: MethodNode) {
        file.key(".method").also { disassembleAccess(method.access, AccessTo.Method) }
            .name(method.name).mDesc(method.desc).nl()
        file.indent()

        if (method.signature != null)
            file.key(".signature").str(method.signature).nl()

        for (exception in method.exceptions) {
            file.key(".throws").name(exception).nl()
        }

        if (method.visibleAnnotations != null)
            for (annotation in method.visibleAnnotations) {
                disassembleAnnotation(annotation, AnnotationType.Visible())
            }
        if (method.invisibleAnnotations != null)
            for (annotation in method.invisibleAnnotations) {
                disassembleAnnotation(annotation, AnnotationType.Invisible())
            }
        if (method.visibleParameterAnnotations != null)
            for ((pararm, annotations) in method.visibleParameterAnnotations.withIndex()) {
                for (annotation in annotations.orEmpty())
                    disassembleAnnotation(annotation, AnnotationType.VisiblePararm(pararm))
            }
        if (method.invisibleParameterAnnotations != null)
            for ((pararm, annotations) in method.invisibleParameterAnnotations.withIndex()) {
                for (annotation in annotations.orEmpty())
                    disassembleAnnotation(annotation, AnnotationType.InvisiblePararm(pararm))
            }
        if (method.attrs.orEmpty().contains(DeprecatedAttribute))
            file.key(".deprecated").nl()

        if (method.instructions != null) {
            fun disassembleLabel(label: LabelNode?) = "L_" + method.instructions.indexOf(label).toString(16).padStart(4, '0')

            file.key(".limit").key("stack").key("${method.maxStack}").nl()
            file.key(".limit").key("local").key("${method.maxLocals}").nl()

            val lineNumber = mutableMapOf<LabelNode, Int>()
            for (instruction in method.instructions) {
                when (instruction) {
                    is LineNumberNode -> {
                        lineNumber[instruction.start] = instruction.line
                    }
                }
            }

            var lastLocal: List<Any>? = null

            for (tryCatchBlockNode in method.tryCatchBlocks) {
                file.key(".catch").also { if(tryCatchBlockNode.type == null)it.key("*") else it.name(tryCatchBlockNode.type) }
                    .key("from").key(disassembleLabel(tryCatchBlockNode.start))
                    .key("to").key(disassembleLabel(tryCatchBlockNode.end))
                    .key("using").key(disassembleLabel(tryCatchBlockNode.handler))
                    .nl()
            }

            for (instruction in method.instructions) {
                val opcode = if (instruction.opcode == -1) ""
                else org.objectweb.asm.util.Printer.OPCODES[instruction.opcode].toLowerCase()

                when (instruction) {
                    is InsnNode -> {
                        file.key(opcode).nl()
                    }
                    is IntInsnNode -> {
                        if (instruction.opcode == Opcodes.NEWARRAY) {
                            file.key(opcode).also {
                                when (instruction.operand) {
                                    Opcodes.T_BOOLEAN -> it.name("boolean")
                                    Opcodes.T_CHAR -> it.name("char")
                                    Opcodes.T_FLOAT -> it.name("float")
                                    Opcodes.T_DOUBLE -> it.name("double")
                                    Opcodes.T_BYTE -> it.name("byte")
                                    Opcodes.T_SHORT -> it.name("short")
                                    Opcodes.T_INT -> it.name("int")
                                    Opcodes.T_LONG -> it.name("long")
                                    else -> error("")
                                }
                            }.nl()
                        } else {
                            file.key(opcode).key("${instruction.operand}").nl()
                        }
                    }
                    is VarInsnNode -> {
                        file.key(opcode).key("${instruction.`var`}").nl()
                    }
                    is TypeInsnNode -> {
                        file.key(opcode).name(instruction.desc).nl()
                    }
                    is FieldInsnNode -> {
                        file.key(opcode).name(instruction.owner).c('/').name(instruction.name)
                            .name(instruction.desc).nl()
                    }
                    is MethodInsnNode -> {
                        file.key(opcode)
                            .also {
                                if (instruction.itf && instruction.opcode != Opcodes.INVOKEINTERFACE)
                                    it.name("interface")
                            }
                            .name(instruction.owner).c('/').name(instruction.name)
                            .mDesc(instruction.desc).nl()
                    }
                    is InvokeDynamicInsnNode -> {
                        file.key(opcode).name(instruction.name).mDesc(instruction.desc).nl()
                        file.indent()
                        disassembleHandle(instruction.bsm)
                        for (bsmArg in instruction.bsmArgs) {
                            disassembleLdcConstant(bsmArg)
                        }
                        file.outdent()
                    }
                    is JumpInsnNode -> {
                        file.key(opcode).key(disassembleLabel(instruction.label)).nl()
                    }
                    is LdcInsnNode -> {
                        file.key(opcode)
                        disassembleLdcConstant(instruction.cst)
                    }
                    is IincInsnNode -> {
                        file.key(opcode).key("${instruction.`var`}").key("${instruction.incr}").nl()
                    }
                    is TableSwitchInsnNode -> {
                        file.key(opcode).key("${instruction.min}").nl()
                        file.indent()
                        for (label in instruction.labels) {
                            file.key(disassembleLabel(label)).nl()
                        }
                        file.key("default:").key(disassembleLabel(instruction.dflt)).nl()
                        file.outdent()
                    }
                    is LookupSwitchInsnNode -> {
                        file.key(opcode).nl()
                        file.indent()
                        for ((key, label)in instruction.keys.zip(instruction.labels)) {
                            file.key("$key:").key(disassembleLabel(label)).nl()
                        }
                        file.key("default:").key(disassembleLabel(instruction.dflt)).nl()
                        file.outdent()
                    }
                    is MultiANewArrayInsnNode -> {
                        file.key(opcode).name(instruction.desc).key("${instruction.dims}").nl()
                    }

                    is LabelNode -> {
                        file.outdent()
                        file.key("${disassembleLabel(instruction)}:").nl()
                        file.indent()

                        val line = lineNumber[instruction]
                        if (line != null) {
                            file.key(".line").key("$line").nl()
                        }
                        for (localVariableNode in method.localVariables.filter { it.start == instruction }) {
                            file.key(".var").key("${localVariableNode.index}").key("is")
                                .name(localVariableNode.name).name(localVariableNode.desc)
                                .also {
                                    if (localVariableNode.signature != null)
                                        it.key("signature").str(localVariableNode.signature)
                                }
                                .key("from").name(disassembleLabel(localVariableNode.start))
                                .key("to").name(disassembleLabel(localVariableNode.end))
                                .nl()
                        }
                    }
                    is FrameNode -> {
                        check(instruction.type == Opcodes.F_NEW)
                        val useLocal = lastLocal == instruction.local
                        lastLocal = instruction.local
                        fun disassembleFrame(frame: Any) {
                            when (frame) {
                                Opcodes.TOP -> file.key("Top")
                                Opcodes.INTEGER -> file.key("Integer")
                                Opcodes.FLOAT -> file.key("Float")
                                Opcodes.LONG -> file.key("Long")
                                Opcodes.DOUBLE -> file.key("Double")
                                Opcodes.NULL -> file.key("Null")
                                Opcodes.UNINITIALIZED_THIS -> file.key("UninitializedThis")
                                is String -> file.key("Object").name(frame)
                                is LabelNode -> file.key("Uninitialized").key(disassembleLabel(frame))
                            }
                        }
                        if (useLocal) {
                            file.key(".stack").key("use").key("locals").nl()
                            file.indent()
                        } else {
                            file.key(".stack").nl()
                            file.indent()
                            for (frame in instruction.local) {
                                file.key("locals")
                                disassembleFrame(frame)
                                file.nl()
                            }
                        }
                        for (frame in instruction.stack) {
                            file.key("stacks")
                            disassembleFrame(frame)
                            file.nl()
                        }
                        file.outdent()
                        file.key(".end").key("stack").nl()
                    }
                }
            }
        }

        file.outdent()
        file.key(".end").key("method").nl()
    }

    private fun disassembleLdcConstant(value: Any?) {
        value!!
        when (value) {
            is Int -> file.key("$value").nl()
            is Double -> file.key("$value").nl()
            is Long -> file.key("${value}L").nl()
            is Float -> file.key("${value}f").nl()
            is String -> file.str(value).nl()
            is Type -> {
                if (value.sort == Type.METHOD)
                    file.key("methodtype").name(value.descriptor).nl()
                else
                    file.key("class").name(value.descriptor).nl()
            }
            is Handle -> file.key("handle").also { disassembleHandle(value) }
            else -> error("")
        }
    }

    private fun disassembleHandle(handle: Handle) {
        when (handle.tag) {
            Opcodes.H_GETFIELD -> file.key("getfield")
            Opcodes.H_GETSTATIC -> file.key("getstatic")
            Opcodes.H_PUTFIELD -> file.key("putfield")
            Opcodes.H_PUTSTATIC -> file.key("putstatic")
            Opcodes.H_INVOKEVIRTUAL -> file.key("invokevirtual")
            Opcodes.H_INVOKESTATIC -> file.key("invokestatic")
            Opcodes.H_INVOKESPECIAL -> file.key("invokespecial")
            Opcodes.H_NEWINVOKESPECIAL -> file.key("newinvokespecial")
            Opcodes.H_INVOKEINTERFACE -> file.key("invokeinterface")
        }
        if (handle.isInterface) file.key("interface")
        file.name(handle.owner).c('/').name(handle.name).mDesc(handle.desc).nl()
    }

    private fun disassembleField(field: FieldNode) {
        file.key(".field").also { disassembleAccess(field.access, AccessTo.Field) }
            .name(field.name).name(field.desc)
            .also {
                when (field.value) {
                    is String -> file.key("=").str("${field.value}")
                    is Int -> file.key("=").key("${field.value}")
                    is Double -> file.key("=").key("${field.value}")
                    is Float -> file.key("=").key("${field.value}f")
                    is Long -> file.key("=").key("${field.value}L")
                }
            }.nl()
        file.indent()

        if (field.signature != null)
            file.key(".signature").str(field.signature).nl()

        if (field.visibleAnnotations != null)
            for (annotation in field.visibleAnnotations) {
                disassembleAnnotation(annotation, AnnotationType.Visible())
            }
        if (field.invisibleAnnotations != null)
            for (annotation in field.invisibleAnnotations) {
                disassembleAnnotation(annotation, AnnotationType.Invisible())
            }
        if (field.attrs.orEmpty().contains(DeprecatedAttribute))
            file.key(".deprecated").nl()

        file.outdent()
        file.key(".end").key("field").nl()
    }

    private fun disassembleAccess(access: Int, accessTo: AccessTo) {
        if (0 != access and Opcodes.ACC_PUBLIC) {
            file.key("public")
        }
        if (0 != access and Opcodes.ACC_PRIVATE) {
            file.key("private")
        }
        if (0 != access and Opcodes.ACC_PROTECTED) {
            file.key("protected")
        }
        if (0 != access and Opcodes.ACC_STATIC) {
            file.key("static")
        }
        if (0 != access and Opcodes.ACC_FINAL) {
            file.key("final")
        }
        if (0 != access and Opcodes.ACC_SYNCHRONIZED) {
            if (accessTo != AccessTo.Class)
                file.key("synchronized")
        }
        if (0 != access and Opcodes.ACC_VOLATILE) {
            if (accessTo == AccessTo.Field)
                file.key("volatile")
            else
                file.key("bridge")
        }
        if (0 != access and Opcodes.ACC_VARARGS) {
            if (accessTo == AccessTo.Field)
                file.key("transient")
            else
                file.key("varargs")
        }
        if (0 != access and Opcodes.ACC_NATIVE) {
            file.key("native")
        }
        if (0 != access and Opcodes.ACC_INTERFACE) {
            file.key("interface")
        }
        if (0 != access and Opcodes.ACC_ABSTRACT) {
            file.key("abstract")
        }
        if (0 != access and Opcodes.ACC_STRICT) {
            file.key("strict")
        }
        if (0 != access and Opcodes.ACC_SYNTHETIC) {
            file.key("synthetic")
        }
        if (0 != access and Opcodes.ACC_ANNOTATION) {
            file.key("annotation")
        }
        if (0 != access and Opcodes.ACC_ENUM) {
            file.key("enum")
        }
        if (0 != access and Opcodes.ACC_MANDATED) {
            file.key("mandated")
        }
    }

    private enum class AccessTo {
        Class,
        Field,
        Method,
    }
}
