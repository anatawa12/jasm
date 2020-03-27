package com.anatawa12.jasm.assembler

import com.anatawa12.jasm.tree.Token
import com.anatawa12.jasm.withTesting
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class VerifierTest {
    @Test
    fun verifyInternalName() {
        val internalNames = listOf(
            "java/lang/String",
            "java/lang/Object"
        )
        for (internalName in internalNames) withTesting(internalName) {
            val verifier = Verifier(AssemblerOptions.default)
            verifier.verifyInternalName(internalName, Token.TEST)
            assertEquals(emptyList<Nothing>(), verifier.errors)
        }
    }

    @Test
    fun verifyMethodDescriptor() {
        val descriptors = listOf(
            "(I)L${"java/lang/Object"};",
            "(I)V",
            "()Ljava/lang/String;"
        )
        for (descriptor in descriptors) withTesting(descriptor) {
            val verifier = Verifier(AssemblerOptions.default)
            verifier.verifyMethodDescriptor(descriptor, Token.TEST)
            assertEquals(emptyList<Nothing>(), verifier.errors)
        }
    }

    @Test
    fun verifyFieldDescriptor() {
        val descriptors = listOf(
            "L${"java/lang/Object"};" to emptyList(),
            "I" to emptyList(),
            "V" to listOf(VerifyingError(VerifyingErrorType.InvalidFieldDescriptor, Token.TEST, emptyArray()))
        )
        for ((descriptor, errors) in descriptors) withTesting(descriptor) {
            val verifier = Verifier(AssemblerOptions.default)
            verifier.verifyFieldDescriptor(descriptor, Token.TEST)
            assertEquals(errors, verifier.errors)
        }
    }

    @Test
    fun verifyMethodSignature() {
        val signatures = listOf(
            "(Lorg/objectweb/asm/MethodVisitor;Ljava/util/List<+Lcom/anatawa12/jasm/tree/MethodStatement;>;II)V"
        )
        for (signature in signatures) withTesting(signature) {
            val verifier = Verifier(AssemblerOptions.default)
            verifier.verifyMethodSignature(signature, Token.TEST)
            assertEquals(emptyList<Nothing>(), verifier.errors)
        }
    }

    @Test
    fun verifyClassSignature() {
        val signatures = listOf(
            "Lcom/anatawa12/jasm/tree/AnnotationValue;Ljava/util/List<Lcom/anatawa12/jasm/tree/AnnotationValue;>;Lkotlin/jvm/internal/markers/KMappedMarker;"
        )
        for (signature in signatures) withTesting(signature) {
            val verifier = Verifier(AssemblerOptions.default)
            verifier.verifyClassSignature(signature, Token.TEST)
            assertEquals(emptyList<Nothing>(), verifier.errors)
        }
    }
}
