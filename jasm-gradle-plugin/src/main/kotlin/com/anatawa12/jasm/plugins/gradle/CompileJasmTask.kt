package com.anatawa12.jasm.plugins.gradle

import com.anatawa12.jasm.assembleJasmFile
import com.anatawa12.jasm.assembler.AssemblerOptions
import com.anatawa12.jasm.parseJasmFile
import com.anatawa12.jasm.verifyFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

open class CompileJasmTask : DefaultTask() {
    @Internal
    lateinit var extension: JasmSourceSetExtension

    @get:OutputDirectory val dir get() = extension.jasm.outputDir
    @get:InputFiles val files get() = extension.jasm

    @Internal
    @Transient
    var hadError = false

    @TaskAction
    fun compile() {
        extension.jasm.outputDir.deleteRecursively()

        val files = extension.jasm
        val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        hadError = false
        val outputDir = files.outputDir

        for (file in files) {
            pool.submit {
                if (!compileFile(file, outputDir))
                    hadError = true
            }
        }

        pool.shutdown()
        while (!pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)){}

        if (hadError)
            throw RuntimeException("some compile error has occeed")
    }

    private fun compileFile(file: File, outputDir: File): Boolean {
        val content = file.readText()

        val fileName = file.path
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
