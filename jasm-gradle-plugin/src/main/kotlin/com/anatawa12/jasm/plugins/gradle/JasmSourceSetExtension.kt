package com.anatawa12.jasm.plugins.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.model.ObjectFactory
import org.gradle.util.ConfigureUtil
import org.gradle.util.GradleVersion
import java.lang.reflect.Constructor

open class JasmSourceSetExtension(project: Project, resolver: FileResolver) {
    val jasm: SourceDirectorySet = createDefaultSourceDirectorySet(project, "jasm", resolver)

    fun jasm(configureClosure: Closure<Any?>) {
        ConfigureUtil.configure(configureClosure, jasm)
    }

    fun jasm(block: Action<SourceDirectorySet>) {
        block.execute(jasm)
    }

    companion object {
        private fun createDefaultSourceDirectorySet(project: Project, name: String, resolver: FileResolver): SourceDirectorySet {
            if (GradleVersion.version("5.0") < GradleVersion.current()) {
                GradleVersion.current()
                val objects = project.objects
                val sourceDirectorySetMethod =
                    objects.javaClass.methods.single { it.name == "sourceDirectorySet" && it.parameterCount == 2 }
                return sourceDirectorySetMethod(objects, name, name) as SourceDirectorySet
            }

            val klass = DefaultSourceDirectorySet::class.java
            val defaultConstructor = klass.constructorOrNull(String::class.java, FileResolver::class.java)

            return if (defaultConstructor != null && defaultConstructor.getAnnotation(java.lang.Deprecated::class.java) == null) {
                // TODO: drop when gradle < 2.12 are obsolete
                defaultConstructor.newInstance(name, resolver)
            } else {
                val directoryFileTreeFactoryClass = Class.forName("org.gradle.api.internal.file.collections.DirectoryFileTreeFactory")
                val alternativeConstructor = klass.getConstructor(String::class.java, FileResolver::class.java, directoryFileTreeFactoryClass)

                val defaultFileTreeFactoryClass = Class.forName("org.gradle.api.internal.file.collections.DefaultDirectoryFileTreeFactory")
                val defaultFileTreeFactory = defaultFileTreeFactoryClass.getConstructor().newInstance()
                alternativeConstructor.newInstance(name, resolver, defaultFileTreeFactory)
            }
        }
        private fun <T> Class<T>.constructorOrNull(vararg parameterTypes: Class<*>): Constructor<T>? =
            try {
                getConstructor(*parameterTypes)
            } catch (e: NoSuchMethodException) {
                null
            }
    }
}
