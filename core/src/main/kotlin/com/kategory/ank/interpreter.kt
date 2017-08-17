package com.kategory.ank

import kategory.*
import org.intellij.markdown.MarkdownElementTypes.CODE_FENCE
import org.intellij.markdown.ast.*
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import java.io.File

@Suppress("UNCHECKED_CAST")
inline fun <reified F> ankMonadErrorInterpreter(ME: MonadError<F, Throwable> = monadError()): FunctionK<AnkOpsHK, F> =
        object : FunctionK<AnkOpsHK, F> {
            override fun <A> invoke(fa: HK<AnkOpsHK, A>): HK<F, A> {
                val op = fa.ev()
                return when (op) {
                    is AnkOps.CreateTarget -> ME.catch({ createTargetImpl(op.source, op.target) })
                    is AnkOps.GetFileCandidates -> ME.catch({ getFileCandidatesImpl(op.target) })
                    is AnkOps.ReadFile -> ME.catch({ readFileImpl(op.source) })
                    is AnkOps.ParseMarkdown -> ME.catch({ parseMarkDownImpl(op.markdown) })
                    is AnkOps.ExtractCode -> ME.catch({ extractCodeImpl(op.source, op.tree) })
                    is AnkOps.CompileCode -> ME.catch({ compileCodeImpl(op.origin, op.code, op.compilerArgs) })
                    is AnkOps.ReplaceAnkToKotlin -> ME.catch({ replaceAnkToKotlinImpl(op.markdown) })
                    is AnkOps.GenerateFiles -> ME.catch({ generateFilesImpl(op.candidates, op.newContents) })
                } as HK<F, A>
            }
        }

val SupportedMarkdownExtensions: Set<String> = setOf(
        "markdown",
        "mdown",
        "mkdn",
        "md",
        "mkd",
        "mdwn",
        "mdtxt",
        "mdtext",
        "text",
        "Rmd"
)

fun createTargetImpl(source: File, target: File): File {
    source.copyRecursively(target, overwrite = true)
    return target
}


fun getFileCandidatesImpl(target: File): ListKW<File> =
        ListKW(target.walkTopDown().filter {
            SupportedMarkdownExtensions.contains(it.extension.toLowerCase())
        }.toList())


fun readFileImpl(source: File): String =
        source.readText()

fun parseMarkDownImpl(markdown: String): ASTNode =
        MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(markdown)

fun extractCodeImpl(source: String, tree: ASTNode): String {
    val sb = StringBuilder()
    tree.accept(object : RecursiveVisitor() {
        override fun visitNode(node: ASTNode) {
            if (node.type == CODE_FENCE) {
                sb.append("\n")
                val fence = node.getTextInNode(source)
                val code = fence.split("\n").drop(1).dropLast(1).joinToString("\n")
                sb.append(code)
            }
            super.visitNode(node)
        }
    })
    return sb.toString()
}

fun compileCodeImpl(origin: File, source: String, compilerArgs: ListKW<String>): Unit {
    val tmpFile = createTempFile(prefix = origin.name + "__", suffix = ".kt")
    tmpFile.printWriter().use { it.println(source) }
    K2JVMCompiler.main((
            compilerArgs +
                    tmpFile.absolutePath
            ).toTypedArray())
    tmpFile.delete()
}

fun replaceAnkToKotlinImpl(markDown: String): String =
        markDown.replace(AnkBlock, "kotlin")

fun generateFilesImpl(candidates: ListKW<File>, newContents: ListKW<String>): ListKW<File> =
        ListKW(candidates.mapIndexed { n, file ->
            file.printWriter().use {
                it.print(newContents.list[n])
            }
            println("ank: processed -> ${file.absolutePath}")
            file
        })
