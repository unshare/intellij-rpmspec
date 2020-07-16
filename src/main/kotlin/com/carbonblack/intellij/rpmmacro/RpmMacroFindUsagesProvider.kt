package com.carbonblack.intellij.rpmmacro

import com.carbonblack.intellij.rpmmacro.psi.RpmMacroMacro
import com.carbonblack.intellij.rpmmacro.psi.RpmMacroTypes
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.*
import com.intellij.psi.tree.TokenSet

class RpmMacroFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner? {
        return DefaultWordsScanner(RpmMacroLexerAdapter(),
                TokenSet.create(RpmMacroTypes.IDENTIFIER),
                TokenSet.create(RpmMacroTypes.COMMENT),
                TokenSet.EMPTY)
    }

    override fun canFindUsagesFor(psiElement: PsiElement) = psiElement is PsiNamedElement
    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String {
        return if (element is RpmMacroMacro) {
            "macro"
        } else {
            ""
        }
    }

    override fun getDescriptiveName(element: PsiElement) = (element as? RpmMacroMacro)?.name ?: ""

    override fun getNodeText(element: PsiElement, useFullName: Boolean) = (element as? RpmMacroMacro)?.name ?: ""
}
