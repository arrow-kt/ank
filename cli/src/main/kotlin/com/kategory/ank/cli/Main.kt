@file:JvmName("Main")
package com.kategory.ank.cli

import com.kategory.ank.core.*

fun main(vararg args: String) {
    val ank = Ank.compute()
    println("Compile time docs verification for Kotlin with $ank :)")
}
