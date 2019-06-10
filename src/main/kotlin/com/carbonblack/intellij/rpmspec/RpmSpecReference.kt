package com.carbonblack.intellij.rpmspec

import com.carbonblack.intellij.rpmmacro.RpmMacroFileType
import com.carbonblack.intellij.rpmmacro.psi.RpmMacroMacro
import com.carbonblack.intellij.rpmspec.psi.RpmSpecMacroDefinition
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import java.util.*
import java.util.concurrent.TimeUnit

import kotlin.collections.ArrayList

class RpmSpecReference(element: PsiElement, textRange: TextRange) :
        PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {
    private val key: String = element.text.substring(textRange.startOffset, textRange.endOffset)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val definitions = PsiTreeUtil.findChildrenOfType(myElement.containingFile, RpmSpecMacroDefinition::class.java)
        val result : MutableList<PsiElement> = definitions.filter { it.macro.name == key }.map { it.macro }.toMutableList()

        val virtualFiles = FileTypeIndex.getFiles(RpmMacroFileType, GlobalSearchScope.everythingScope(myElement.project))
        val rpmMacroFiles  = virtualFiles.map { PsiManager.getInstance(myElement.project).findFile(it) }
        for (file in rpmMacroFiles) {
            val macros = PsiTreeUtil.findChildrenOfType(file, RpmMacroMacro::class.java).filter { it.name == key }
            result += macros
        }

        val results = result.map { PsiElementResolveResult(it) }
        return results.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val definitions = PsiTreeUtil.findChildrenOfType(myElement.containingFile, RpmSpecMacroDefinition::class.java)
        val result  = definitions.filter { it.macro.name == key }.map { it.macro }
        if (result.isNotEmpty()) {
            return result.first()
        }

        return systemMacrosCache.get(Pair(key, myElement.project)).orElse(null)
    }

    override fun getVariants(): Array<Any> {
        val file = myElement.containingFile
        val macros = RpmSpecUtil.findMacros(file)
        val variants = macros.filter { it.name.isNotEmpty() }.map {
            it.name to LookupElementBuilder.create(it)
                .withIcon(RpmSpecIcons.FILE)
                .withTypeText(it.containingFile.name) }.toMap()
        return ArrayList(variants.values).toTypedArray()
    }

    companion object {
        val systemMacrosCache: LoadingCache<Pair<String, Project>, Optional<PsiElement>> = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(object : CacheLoader<Pair<String, Project>, Optional<PsiElement>>() {
                    override fun load(pair: Pair<String, Project>): Optional<PsiElement> {
                        val virtualFiles = FileTypeIndex.getFiles(RpmMacroFileType, GlobalSearchScope.everythingScope(pair.second))
                        val rpmMacroFiles  = virtualFiles.map { PsiManager.getInstance(pair.second).findFile(it) }
                        for (file in rpmMacroFiles) {
                            val macros = PsiTreeUtil.findChildrenOfType(file, RpmMacroMacro::class.java).filter { it.name == pair.first }
                            if (macros.isNotEmpty()) {
                                return Optional.of(macros.first())
                            }
                        }
                        return Optional.empty()
                    }
                })
    }
}
