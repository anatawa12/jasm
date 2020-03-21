package com.anatawa12.jasm

import org.opentest4j.AssertionFailedError

inline fun withMessage (externalMessage: () -> String, block: () -> Unit) = try {
    block()
} catch (e: AssertionFailedError) {
    throw AssertionFailedError("${externalMessage()}: ${e.message}", e)
}

inline fun withTesting (name: String, block: () -> Unit) = withMessage({"testing '$name'"}, block)
