package com.anatawa12.jasm.plugins.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory

open class JasmSourceSetExtension(factory: ObjectFactory) {
    val jasm: SourceDirectorySet = factory.sourceDirectorySet("jasm", "jasm")

    fun jasm(block: Action<SourceDirectorySet>) {
        block.execute(jasm)
    }
}
