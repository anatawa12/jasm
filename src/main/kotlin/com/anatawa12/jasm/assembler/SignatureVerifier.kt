package com.anatawa12.jasm.assembler

import com.anatawa12.jasm.tree.Node
import kotlin.system.measureTimeMillis

class SignatureVerifier(val signature: String) {
    var index = 0
    fun get() = signature[index]
    fun getAndNext() = signature[index++]
    fun next() { index++ }

    fun readIdentifier(): Boolean {
        if (get() in ".;[/<>:") return false
        while (get() !in ".;[/<>:") getAndNext()
        return true
    }

    fun readFieldDescriptor(): Boolean = readFieldType()

    fun readFieldType(): Boolean {
        return when (get()) {
            'L' -> readObjectType()
            '[' -> readArrayType()
            else -> readBaseType()
        }
    }

    fun readBaseType(): Boolean {
        return getAndNext() in "BCDFIJSZ"
    }

    fun readObjectType(): Boolean {
        if (getAndNext() != 'L') return false
        readInternalName()
        if (getAndNext() != ';') return false
        return true
    }

    fun readArrayType(): Boolean {
        if (getAndNext() != '[') return false
        readFieldType().ifFalse { return false }
        return true
    }

    fun readMethodDescriptor(): Boolean {
        if (getAndNext() != '(') return false
        while (get() != ')')
            readFieldType().ifFalse { return false }
        if (getAndNext() != ')') return false
        if (get() == 'V') {
            next()
        } else {
            readFieldType().ifFalse { return false }
        }
        return true
    }

    fun readJavaTypeSignature(): Boolean {
        return if (get() in "L[T") readReferenceTypeSignature() else readBaseType()
    }

    fun readReferenceTypeSignature(): Boolean {
        return when (get()) {
            'L' -> readClassTypeSignature()
            'T' -> readTypeVariableSignature()
            '[' -> readArrayTypeSignature()
            else -> false
        }
    }

    fun readClassTypeSignature(): Boolean {
        if (getAndNext() != 'L') return false
        readInternalName().ifFalse { return false }
        if (get() == '<') readTypeArguments().ifFalse { return false }
        while (get() == '.') readClassTypeSignatureSuffix().ifFalse { return false }
        if (getAndNext() != ';') return false
        return true
    }

    fun readInternalName(): Boolean {
        readIdentifier().ifFalse { return false }
        while (get() == '/') {
            next()
            readIdentifier().ifFalse { return false }
        }
        return true
    }

    fun readSimpleClassTypeSignature(): Boolean {
        readIdentifier().ifFalse { return false }
        readTypeParameters().ifFalse { return false }
        return true
    }

    fun readTypeArguments(): Boolean {
        if (getAndNext() != '<') return false
        while (get() != '>') readTypeArgument().ifFalse { return false }
        if (getAndNext() != '>') return false
        return true
    }

    fun readTypeArgument(): Boolean {
        if (get() == '+') {
            next()
        } else if (get() == '-') {
            next()
        } else if (get() == '*') {
            next()
            return true
        }
        readReferenceTypeSignature().ifFalse { return false }
        return true
    }

    fun readClassTypeSignatureSuffix(): Boolean {
        if (getAndNext() != '.') return false
        readSimpleClassTypeSignature().ifFalse { return false }
        return true
    }

    fun readTypeVariableSignature(): Boolean {
        if (getAndNext() != 'T') return false
        readIdentifier().ifFalse { return false }
        if (getAndNext() != ';') return false
        return true
    }

    fun readArrayTypeSignature(): Boolean {
        if (getAndNext() != '[') return false
        readJavaTypeSignature().ifFalse { return false }
        return true
    }

    fun readClassSignature(): Boolean {
        if (get() == '<') readTypeParameters()
        readSuperclassSignature()
        readSuperinterfaceSignature()
        return true
    }

    fun readTypeParameters(): Boolean {
        if (getAndNext() != '<') return false
        while (get() != '>') readTypeParameter().ifFalse { return false }
        if (getAndNext() != '>') return false
        return true
    }

    fun readTypeParameter(): Boolean {
        readInternalName().ifFalse { return false }
        readClassBound().ifFalse { return false }
        while (get() == ':')
            readInterfaceBound().ifFalse { return false }
        return true
    }

    fun readClassBound(): Boolean {
        if (getAndNext() != ':') return false
        if (get() in "LT[") readReferenceTypeSignature().ifFalse { return false }
        return true
    }

    fun readInterfaceBound(): Boolean {
        if (getAndNext() != ':') return false
        readReferenceTypeSignature().ifFalse { return false }
        return true
    }

    fun readSuperclassSignature() = readClassTypeSignature()
    fun readSuperinterfaceSignature() = readClassTypeSignature()

    fun readMethodSignature(): Boolean {
        if (get() == '<')
            readTypeParameters().ifFalse { return false }
        if (getAndNext() != '(') return false
        while (get() != ')')
            readJavaTypeSignature().ifFalse { return false }
        if (getAndNext() != ')') return false
        if (get() == 'V') next()
        else readJavaTypeSignature().ifFalse { return false }
        while (get() == '^')
            readThrowsSignature().ifFalse { return false }
        return true
    }

    fun readThrowsSignature(): Boolean {
        if (getAndNext() != '^') return false
        when (get()) {
            'L' -> readClassTypeSignature().ifFalse { return false }
            'T' -> readTypeVariableSignature().ifFalse { return false }
            else -> return false
        }
        return true
    }

    fun readFieldSignature(): Boolean {
        val result = kotlin.runCatching {
            readReferenceTypeSignature().ifFalse { return false }
        }
        return index == signature.length
    }

    //.ifFalse { return false }
    fun classSignature(): Boolean {
        val result = kotlin.runCatching {
            readClassSignature().ifFalse { return false }
        }
        return index == signature.length
    }

    fun methodDescriptor(): Boolean {
        val result = kotlin.runCatching {
            readMethodDescriptor().ifFalse { return false }
        }
        return index == signature.length
    }

    fun methodSignature(): Boolean {
        val result = kotlin.runCatching {
            readMethodSignature().ifFalse { return false }
        }
        return index == signature.length
    }

    fun fieldDescriptor(): Boolean {
        val result = kotlin.runCatching {
            readFieldDescriptor().ifFalse { return false }
        }
        return index == signature.length
    }

    fun fieldSignature(): Boolean {
        val result = kotlin.runCatching {
            readFieldSignature().ifFalse { return false }
        }
        return index == signature.length
    }

    private inline fun Boolean.ifFalse(block: () -> Nothing) {
        if (!this) block()
    }
}
