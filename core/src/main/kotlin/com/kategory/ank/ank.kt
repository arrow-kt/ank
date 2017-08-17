package com.kategory.ank

import kategory.*
import org.intellij.markdown.ast.ASTNode
import java.io.File

const val AnkBlock = "kotlin:ank"

fun ank(source: File, target: File, compilerArgs: ListKW<String>): HK<FreeHK<AnkOpsHK>, ListKW<File>> =
        AnkOps.binding {
            val targetDirectory: File = createTarget(source, target).bind()
            val files: ListKW<File> = getFileCandidates(targetDirectory).bind()
            val filesContents: ListKW<String> = files.map(::readFile).sequence().bind()
            val parsedMarkDowns: ListKW<ASTNode> = filesContents.map(::parseMarkdown).sequence().bind()
            val sources: ListKW<String> = ListKW(parsedMarkDowns.mapIndexed { n, tree ->
                extractCode(filesContents.list[n], tree)
            }).sequence().bind()
            ListKW(sources.mapIndexed { n, s -> compileCode(files.list[n], s, compilerArgs) }).sequence().bind()
            val replacedResults: ListKW<String> = filesContents.map(::replaceAnkToKotlin).sequence().bind()
            val resultingFiles: ListKW<File> = generateFiles(files, replacedResults).bind()
            yields(resultingFiles)
        }