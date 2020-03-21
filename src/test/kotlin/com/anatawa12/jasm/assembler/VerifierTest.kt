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
            val verifier = Verifier()
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
            val verifier = Verifier()
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
            val verifier = Verifier()
            verifier.verifyFieldDescriptor(descriptor, Token.TEST)
            assertEquals(errors, verifier.errors)
        }
    }
}
