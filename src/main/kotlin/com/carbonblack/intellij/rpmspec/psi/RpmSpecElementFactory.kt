package com.carbonblack.intellij.rpmspec.psi

import com.carbonblack.intellij.rpmspec.RpmSpecFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.*

object RpmSpecElementFactory {
    fun createMacro(project: Project, name: String): RpmSpecMacro {
        val file = createFile(project, name)
        return file.firstChild as RpmSpecMacro
    }

    private fun createFile(project: Project, text: String): RpmSpecFile {
        val name = "dummy.spec"
        return PsiFileFactory.getInstance(project).createFileFromText(name, RpmSpecFileType.INSTANCE, text) as RpmSpecFile
    }
}