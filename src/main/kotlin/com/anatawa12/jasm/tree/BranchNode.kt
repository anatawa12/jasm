package com.anatawa12.jasm.tree

abstract class BranchNode : Node() {
    override lateinit var tokens: Array<out Token<*>>
        internal set
}
