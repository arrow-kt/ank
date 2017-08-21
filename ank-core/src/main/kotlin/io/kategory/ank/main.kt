@file:JvmName("main")
package io.kategory.ank

import kategory.*
import java.io.File

typealias Target<A> = Either<Throwable, A>

fun main(vararg args: String) {
    println("ank args: \n${args.joinToString("\n")}")
    when {
        args.size > 1 -> {
            val ME = Either.monadError<Throwable>()
            ank(File(args[0]), File(args[1]), ListKW(args.drop(2)))
                    .ev()
                    .run(ankMonadErrorInterpreter(ME), ME).ev()
                    .fold({ ex ->
                        throw ex
                    }, { files ->
                        println("Generated docs: \n ${files.joinToString(separator = "\n")}")
                    })
        }
        else -> throw IllegalArgumentException("Required first 2 args as directory paths in this order: <required: source> <required: destination> <optional: compilerArgs..>")
    }
}
