package com.anatawa12.jasm.tree

class LabelName(val name: String) : BranchNode()
class Handle(val type: HandleType, val owner: String, val name: String, val desc: String, val itf: Boolean) : BranchNode()
class MethodType(val desc: String) : BranchNode()

enum class HandleType {
    GetField, GetStatic,
    PutField, PutStatic,
    InvokeVirtual, InvokeStatic,
    InvokeSpecial, NewInvokeSpecial,
    InvokeInterface,
}
