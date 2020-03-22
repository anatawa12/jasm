package com.anatawa12.jasm

import org.objectweb.asm.*

object DeprecatedAttribute : Attribute("Deprecated") {
    override fun read(
        cr: ClassReader?,
        off: Int,
        len: Int,
        buf: CharArray?,
        codeOff: Int,
        labels: Array<out Label>?
    ): Attribute = this

    override fun write(cw: ClassWriter?, code: ByteArray?, len: Int, maxStack: Int, maxLocals: Int): ByteVector = ByteVector(0)
}
