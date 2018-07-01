@file:JvmName("main")
package arrow.ank

import arrow.core.Either
import arrow.core.monadError
import arrow.data.ListK
import arrow.free.fix
import arrow.core.fix
import java.io.File

typealias Target<A> = Either<Throwable, A>

fun main(vararg args: String) {
    when {
        args.size > 1 -> {
            val ME = Either.monadError<Throwable>()
            ank(
                source = File(args[0]),
                target = File(args[1]),
                compilerArgs = ListK(args.drop(2).flattenDirectoryPath())
            ).fix()
                .run(ME.ankMonadErrorInterpreter(), ME).fix()
            .fold({ ex ->
                throw ex
            }, { files ->
                println("ΛNK Generated:\n\t${files.joinToString(separator = "\n\t")}")
            })
        }
        else -> throw IllegalArgumentException("Required first 2 args as directory paths in this order: <required: source> <required: destination> <optional: classpath entries, one per arg..>")
    }
}

private fun List<String>.flattenDirectoryPath(): List<String> = flatMap {
    listOf(it) + File(it).walk().map { it.absolutePath }
}
