// Klisp. A simple lisp interpreter based on lispy.py (see http://norvig.com/lispy.html)

package klisp.main

import klisp.env.Environment
import klisp.eval
import klisp.parser.parse
import klisp.parser.read
import java.io.File
import java.lang.RuntimeException
import java.lang.StringBuilder

fun repl(global: Environment): Nothing {
    while (true)
        try {
            println(eval(parse(read()), global))
        } catch (ex: RuntimeException) {
            println("runtime error: $ex")
        }
}

fun load(file: String): StringBuilder {
    val buf = StringBuilder()
    val reader = File(file).bufferedReader()

    while (true) {
        val line = reader.readLine()
        buf.append(line ?: break)
    }

    return buf
}

fun main(vararg args: String) {
    println("Hello, this is Klisp 1.0 (`exit` to quit)")

    if (!(args.size == 1 || args.size == 0))
        throw IllegalArgumentException("usage: java -jar klisp.jar [file.kl]")

    val global = Environment(null)

    if (args.size == 1) {
        eval(parse(load(args[0])), global)
        return
    }

    repl(global)
}