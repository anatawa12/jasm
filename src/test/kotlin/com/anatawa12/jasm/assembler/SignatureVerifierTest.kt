package com.anatawa12.jasm.assembler

import com.anatawa12.jasm.withTesting
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SignatureVerifierTest {
    @Test
    fun readIdentifier() {
        val identifiers = listOf(
            Triple("java;", true, ';'),
            Triple("java/lang", true, '/'),
            Triple("/", false, '/')
        )
        for ((id, result, next) in identifiers) withTesting(id) {
            val verifier = SignatureVerifier(id)
            if (result) {
                assertTrue(verifier.readIdentifier())
            } else {
                assertFalse(verifier.readIdentifier())
            }
            assertEquals(next, verifier.get())
        }
    }

    @Test
    fun readFieldDescriptor() {

    }

    @Test
    fun readFieldType() {

    }

    @Test
    fun readBaseType() {

    }

    @Test
    fun readObjectType() {

    }

    @Test
    fun readArrayType() {

    }

    @Test
    fun readMethodDescriptor() {

    }

    @Test
    fun readJavaTypeSignature() {

    }

    @Test
    fun readReferenceTypeSignature() {

    }

    @Test
    fun readClassTypeSignature() {

    }

    @Test
    fun readInternalName() {

    }

    @Test
    fun readSimpleClassTypeSignature() {

    }

    @Test
    fun readTypeArguments() {

    }

    @Test
    fun readTypeArgument() {

    }

    @Test
    fun readClassTypeSignatureSuffix() {

    }

    @Test
    fun readTypeVariableSignature() {

    }

    @Test
    fun readArrayTypeSignature() {

    }

    @Test
    fun readClassSignature() {

    }

    @Test
    fun readTypeParameters() {

    }

    @Test
    fun readTypeParameter() {

    }

    @Test
    fun readClassBound() {

    }

    @Test
    fun readInterfaceBound() {

    }

    @Test
    fun readSuperclassSignature() {

    }

    @Test
    fun readSuperinterfaceSignature() {

    }

    @Test
    fun readMethodSignature() {

    }

    @Test
    fun readThrowsSignature() {

    }

    @Test
    fun readFieldSignature() {

    }
}
