package org.jetbrains.research.ml.tasktracker.services

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.Consumer

class CustomHighlightUsagesHandlerBase(editor: Editor, file: PsiFile) :
    HighlightUsagesHandlerBase<PsiElement>(editor, file) {
    override fun getTargets(): MutableList<PsiElement> = mutableListOf()

    override fun computeUsages(targets: MutableList<out PsiElement>) {}

    override fun selectTargets(
        targets: MutableList<out PsiElement>,
        selectionConsumer: Consumer<in MutableList<out PsiElement>>
    ) {
    }
}

class CustomHighlightUsagesHandlerFactory() : HighlightUsagesHandlerFactory {
    override fun createHighlightUsagesHandler(editor: Editor, file: PsiFile): HighlightUsagesHandlerBase<*> {
        return CustomHighlightUsagesHandlerBase(editor, file)
    }
}
