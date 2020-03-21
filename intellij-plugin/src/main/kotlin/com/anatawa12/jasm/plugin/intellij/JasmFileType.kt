package com.anatawa12.jasm.plugin.intellij

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object JasmFileType : LanguageFileType(JasmLanguage) {
    override fun getIcon(): Icon = JasmIcons.FILE

    override fun getName(): String = "jasm file"

    override fun getDefaultExtension(): String = "jasm"

    override fun getDescription(): String = "jasm file"
}
