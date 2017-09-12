package io.kategory.ank

import kategory.*
import org.intellij.markdown.MarkdownElementTypes.CODE_FENCE
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import javax.script.ScriptEngineManager
import javax.script.ScriptException

val extensionMappings = mapOf(
        "java" to "java",
        "kotlin" to "kts"
)

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
                    is AnkOps.CompileCode -> ME.catch({ compileCodeImpl(op.origin, op.snippets, op.compilerArgs) })
                    is AnkOps.ReplaceAnkToKotlin -> ME.catch({ replaceAnkToKotlinImpl(op.compilationResults) })
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

data class CompilationException(
        val snippet: Snippet,
        val underlying: Throwable,
        val msg: String = """
            |
            |
            |### ΛNK Compilation Error ###
            |```
            |${snippet.code}
            |```
            |
            |${underlying.message}
            |##############################
            |
        """.trimMargin()) : ScriptException(msg) {
    override fun toString(): String = msg
}

data class CompiledMarkdown(val origin: File, val snippets: ListKW<Snippet>)

data class Snippet(
        val lang: String,
        val silent: Boolean,
        val startOffset: Int,
        val endOffset: Int,
        val code: String,
        val result: Option<String> = Option.None)

fun extractCodeImpl(source: String, tree: ASTNode): ListKW<Snippet> {
    val sb = mutableListOf<Snippet>()
    tree.accept(object : RecursiveVisitor() {
        override fun visitNode(node: ASTNode) {
            if (node.type == CODE_FENCE) {
                val fence = node.getTextInNode(source)
                val lang = fence.takeWhile { it != ':' }.toString().replace("```", "")
                if (fence.startsWith("```$lang$AnkBlock")) {
                    val code = fence.split("\n").drop(1).dropLast(1).joinToString("\n")
                    sb.add(Snippet(lang, fence.startsWith("```$AnkSilentBlock"), node.startOffset, node.endOffset, code))
                }
            }
            super.visitNode(node)
        }
    })
    return sb.k()
}

fun compileCodeImpl(origin: File, snippets: ListKW<Snippet>, compilerArgs: ListKW<String>): CompiledMarkdown {
    val classLoader = URLClassLoader(compilerArgs.map { URL(it) }.ev().list.toTypedArray())
    val seManager = ScriptEngineManager(classLoader)
    val evaledSnippets = snippets.list.map { snippet ->
        println("Extension found -> ${snippet.lang}")
        val result = Try {
            val engine = seManager.getEngineByExtension(extensionMappings.getOrDefault(snippet.lang, "kts"))!!
            println("Loaded script engine: $engine : extension -> ${snippet.lang}")
            engine.eval(snippet.code) }.fold({
            throw CompilationException(snippet, it)
        }, { it })
        val resultString = Option.fromNullable(result).fold({ "Unit" }, { "$it" })
        if (snippet.silent) snippet
        else snippet.copy(result = "\n```\n$resultString\n```".some())
    }.k()
    return CompiledMarkdown(origin, evaledSnippets)
}

fun replaceAnkToKotlinImpl(compiledMarkdown: CompiledMarkdown): String =
        compiledMarkdown.origin.readText().toCharArray().foldIndexed(emptyList<Char>(), { n, acc, char ->
            compiledMarkdown.snippets
                    .find { it.endOffset == n }
                    .flatMap { it.result }
                    .map { it.toCharArray().map { it } }
                    .fold({ acc + char }, { (acc + it) + char })
        }).joinToString("")
                .replace(AnkSilentBlock, "")
                .replace(AnkBlock, "")

fun generateFilesImpl(candidates: ListKW<File>, newContents: ListKW<String>): ListKW<File> =
        ListKW(candidates.mapIndexed { n, file ->
            file.printWriter().use {
                it.print(newContents.list[n])
            }
            file
        })
