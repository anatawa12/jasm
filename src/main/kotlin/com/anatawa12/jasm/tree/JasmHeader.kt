package com.anatawa12.jasm.tree

data class JasmHeader(
    val bytecode: BytecodeDirective?,
    val source: SourceDirective?,
    val className: ClassDirective,
    val superName: SuperDirective?,
    val implements: List<ImplementsDirective>,
    val signature: SignatureDirective?,
    val enclosing: EnclosingDirective?,
    val debug: DebugDirective?,
    val innerClasses: List<InnerClassDirective>
) : BranchNode()

data class BytecodeDirective(val major: Int, val minor: Int) : BranchNode()
data class SourceDirective(val sourceFile: String) : BranchNode()
data class ClassDirective(val accessFlags: AccessFlags, val internalName: String) : BranchNode()
data class SuperDirective(val internalName: String) : BranchNode()
data class ImplementsDirective(val internalName: String) : BranchNode()
data class SignatureDirective(val signature: String) : BranchNode()
sealed class EnclosingDirective : BranchNode() {
    abstract val owner: String
}
data class EnclosingClassDirective(override val owner: String) : EnclosingDirective()
data class EnclosingMethodDirective(override val owner: String, val name: String, val descriptor: String) : EnclosingDirective()
data class DebugDirective(val debugData: String) : BranchNode()
data class InnerClassDirective(val accessFlags: AccessFlags, val name: String, val outerName: String?, val innerName: String?) : BranchNode()
