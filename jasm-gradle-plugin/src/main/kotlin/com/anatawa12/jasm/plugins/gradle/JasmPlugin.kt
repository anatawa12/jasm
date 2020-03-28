package com.anatawa12.jasm.plugins.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.HasConvention
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import javax.inject.Inject

open class JasmPlugin @Inject constructor(val resolver: FileResolver) : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(JavaPlugin::class.java)
        project.convention.getByType(SourceSetContainer::class.java).all { sourceSet ->
            val extension = JasmSourceSetExtension(project, resolver)
            (sourceSet as HasConvention).convention.add("jasm", extension)
            extension.jasm.srcDirs(project.projectDir.resolve("src/${sourceSet.name}/jasm"))
            extension.jasm.srcDirs(project.projectDir.resolve("src/${sourceSet.name}/java"))
            extension.jasm.filter.include("**/*.jasm")
            extension.jasm.outputDir = project.buildDir.resolve("classes/jasm/${sourceSet.name}")

            sourceSet.output.dir(project.provider { extension.jasm.outputDir })

            val taskName = sourceSet.getCompileTaskName("jasm")
            val compileJasm = project.tasks.create(taskName, CompileJasmTask::class.java).apply {
                this.extension = extension
            }

            project.tasks.getByName(sourceSet.compileJavaTaskName).dependsOn(compileJasm)
            project.tasks.getByName(sourceSet.classesTaskName).dependsOn(compileJasm)
        }
    }
}
