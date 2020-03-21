package com.anatawa12.jasm.tree


sealed class MethodStatement() : BranchNode()

data class StackLimit(val maxStack: Int): MethodStatement()
data class LocalLimit(val maxLocal: Int): MethodStatement()

data class LineNumber(val line: Int): MethodStatement()

data class LocalVar(val variable: Int, val name: String, val descriptor: String, val signature: String?, val from: LabelName, val to: LabelName) : MethodStatement()

data class Throws(val internalName: String) : MethodStatement()

data class TryCatch(val exception: String?, val from: LabelName, val to: LabelName, val using: LabelName) : MethodStatement()

data class MethodSignature(val methodSignature: String) : MethodStatement()

class MethodDeprecated : MethodStatement()

data class LabelDefinition(val name: LabelName): MethodStatement()

data class StackFrame(val locals: List<StackFrameType>?, val stacks: List<StackFrameType>): MethodStatement()

sealed class StackFrameType : BranchNode() {
    class Top : StackFrameType()
    class Integer : StackFrameType()
    class Float : StackFrameType()
    class Long : StackFrameType()
    class Double : StackFrameType()
    class Null : StackFrameType()
    class UninitializedThis : StackFrameType()
    data class Object(val internalName: String) : StackFrameType()
    data class Uninitialized(val madeAt: LabelName) : StackFrameType()
}

data class MethodAnnotation(val annotation: Annotation): MethodStatement()

sealed class Instruction() : MethodStatement()
data class Insn(val opcode: Int): Instruction()
data class IntInsn(val opcode: Int, val operand: Int): Instruction()
data class FieldInsn(val opcode: Int, val owner: String, val name: String, val desc: String): Instruction()
data class IincInsn(val variable: Int, val increment: Int): Instruction()
data class InvokeDynamicInsn(val name: String, val desc: String, val bsm: Handle, val bsmArgs: List<BranchNode>): Instruction()
data class JumpInsn(val opcode: Int, val label: LabelName): Instruction()
data class LdcInsn(val cst: BranchNode): Instruction()
data class LookupSwitchInsn(val dflt: LabelName, val pairs: List<Pair<Int, LabelName>>): Instruction()
data class MethodInsn(val opcode: Int, val owner: String, val name: String, val desc: String, val itf: Boolean): Instruction()
data class MultiANewArrayInsn(val desc: String, val dims: Int): Instruction()
data class TableSwitchInsn(val min: Int, val dflt: LabelName, val labels: List<LabelName>): Instruction()
data class TypeInsn(val opcode: Int, val type: String): Instruction()
data class VarInsn(val opcode: Int, val variable: Int): Instruction()
