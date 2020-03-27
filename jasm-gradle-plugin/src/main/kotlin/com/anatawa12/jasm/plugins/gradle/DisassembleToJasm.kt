package com.anatawa12.jasm.plugins.gradle

import com.anatawa12.jasm.disassemble
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.lang.RuntimeException

open class DisassembleToJasm : DefaultTask() {
    @InputFiles var files: FileTree = project.files().asFileTree
    @OutputDirectory var outputTo: File? = null

    @TaskAction
    fun disassemble() {
        outputTo ?: error("outputTo not inited")

        outputTo!!.deleteRecursively()

        val fileList = mutableListOf<Pair<File, String>>()

        files.visit {
            if (it.isDirectory) return@visit
            fileList += it.file to it.path
        }

        for ((file, path) in fileList) {
            try {
                val sourcePath = path.removeSuffix(".class") + ".jasm"
                outputTo!!.resolve(sourcePath)
                    .apply { parentFile.mkdirs() }
                    .writeText(disassemble(file.readBytes()))
            } catch (exception: Exception) {
                throw RuntimeException("can't decompile ${path}")
            }
        }
    }
}
