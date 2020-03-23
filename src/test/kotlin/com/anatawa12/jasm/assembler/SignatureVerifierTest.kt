package com.anatawa12.jasm.assembler

import com.anatawa12.jasm.tree.Token
import com.anatawa12.jasm.withTesting
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SignatureVerifierTest {
    private inline fun readTest(tests: List<Triple<String, Boolean, Char>>, tester: (SignatureVerifier) -> Boolean) {
        for ((test, result, next) in tests) withTesting(test) {
            val verifier = SignatureVerifier(test)
            if (result) {
                assertTrue(tester(verifier))
            } else {
                assertFalse(tester(verifier))
            }
            assertEquals(next, verifier.get())
        }
    }

    @Test
    fun readIdentifier() {
        val identifiers = listOf(
            Triple("java;", true, ';'),
            Triple("java/lang", true, '/'),
            Triple("/", false, '/')
        )
        readTest(identifiers) { it.readIdentifier() }
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
        val signatures = listOf(
            Triple("Lorg/objectweb/asm/MethodVisitor;-=", true, '-'),
            Triple("TType;-=", true, '-'),
            Triple("[I-=", true, '-')
        )
        readTest(signatures) { it.readReferenceTypeSignature() }
    }

    @Test
    fun readClassTypeSignature() {
        val signatures = listOf(
            Triple("Lorg/objectweb/asm/MethodVisitor;-=", true, '-'),
            Triple("Ljava/util/List<+Lcom/anatawa12/jasm/tree/MethodStatement;>;-=", true, '-')
        )
        readTest(signatures) { it.readClassTypeSignature() }
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
        val signatures = listOf(
            Triple("TType;-=", true, '-')
        )
        readTest(signatures) { it.readTypeVariableSignature() }
    }

    @Test
    fun readArrayTypeSignature() {
        val signatures = listOf(
            Triple("[I-=", true, '-'),
            Triple("[Ljava/util/List<+Lcom/anatawa12/jasm/tree/MethodStatement;>;-=", true, '-')
        )
        readTest(signatures) { it.readArrayTypeSignature() }
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
        val signatures = listOf(
            Triple("(Lorg/objectweb/asm/MethodVisitor;Ljava/util/List<+Lcom/anatawa12/jasm/tree/MethodStatement;>;II)V-", true, '-')
        )
        readTest(signatures) { it.readMethodSignature() }
    }

    @Test
    fun readThrowsSignature() {

    }

    @Test
    fun readFieldSignature() {

    }
}
