package com.carbonblack.intellij.rpmmacro.psi

import com.carbonblack.intellij.rpmmacro.RpmMacroReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService

abstract class RpmMacroMacroElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), RpmMacroMacroElement {
    override fun getNameIdentifier(): PsiElement? =
        node.findChildByType(RpmMacroTypes.IDENTIFIER)?.psi


    override fun setName(name: String): PsiElement {
        val keyNode = node.findChildByType(RpmMacroTypes.IDENTIFIER)

        if (keyNode != null) {
            val macro = RpmMacroElementFactory.createMacro(project, name)
            val newKeyNode = macro.firstChild.node
            node.replaceChild(keyNode, newKeyNode)
        }
        return this
    }

    override fun getName(): String {
        val valueNode = node.findChildByType(RpmMacroTypes.IDENTIFIER)
        return valueNode!!.text
    }

    override fun getReference(): PsiReference {
        return RpmMacroReference(this, TextRange(0, name.length))
    }
}
