package com.anatawa12.jasm.plugins.gradle

import com.anatawa12.jasm.assembleJasmFile
import com.anatawa12.jasm.assembler.AssemblerOptions
import com.anatawa12.jasm.parseJasmFile
import com.anatawa12.jasm.verifyFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CompileJasmTask : DefaultTask() {
    lateinit var extension: JasmSourceSetExtension

    @get:OutputDirectory val dir get() = extension.jasm.outputDir
    @get:InputFiles val files get() = extension.jasm

    @TaskAction
    fun compile() {
        val files = extension.jasm
        var hadError = false
        for (file in files) {
            if (!compileFile(file, files.outputDir))
                hadError = true
        }

        if (hadError)
            throw RuntimeException("some compile error has occeed")
    }

    private fun compileFile(file: File, outputDir: File): Boolean {
        val content = file.readText()

        val fileName = file.name
        val jasmFile = parseJasmFile(fileName, content) ?: return false
        val options = AssemblerOptions.parse(jasmFile.header)

        if (!verifyFile(fileName, jasmFile, options)) return false

        val classFile = assembleJasmFile(fileName, jasmFile, options)

        val classInternalName = jasmFile.classHeader.className.internalName

        outputDir.resolve("$classInternalName.class")
            .apply { parentFile.mkdirs() }
            .writeBytes(classFile)

        return true
    }
}
