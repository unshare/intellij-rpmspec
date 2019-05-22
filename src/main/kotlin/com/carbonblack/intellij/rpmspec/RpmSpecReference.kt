package com.carbonblack.intellij.rpmspec

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

import java.util.*

class RpmSpecReference(element: PsiElement, textRange: TextRange) : PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {
    private val key: String = element.text.substring(textRange.startOffset, textRange.endOffset)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = myElement.project
        val macros = RpmSpecUtil.findMacros(project, key)
        val results = ArrayList<ResolveResult>()
        for (macro in macros) {
            results.add(PsiElementResolveResult(macro))
        }
        return results.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.isNotEmpty()) resolveResults[0].element else null
    }

    override fun getVariants(): Array<Any> {
        val project = myElement.project
        val macros = RpmSpecUtil.findMacros(project)
        val variants = ArrayList<LookupElement>()
        for (macro in macros) {
            if (macro.name.isNotEmpty()) {
                variants.add(LookupElementBuilder.create(macro).withIcon(RpmSpecIcons.FILE).withTypeText(macro.containingFile.name)
                )
            }
        }
        return variants.toTypedArray()
    }
}