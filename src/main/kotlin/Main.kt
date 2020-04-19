// Klisp. A simple lisp interpreter based on lispy.py (see http://norvig.com/lispy.html)

package klisp.main

import klisp.env.Environment
import klisp.eval
import klisp.parser.parse
import klisp.parser.read
import java.lang.RuntimeException

fun main() {
    println("Hello, this is Klisp 1.0 (`exit` to quit)")
    val global = Environment(null)
    while (true)
        try {
            println(eval(parse(read()), global))
        } catch (ex: RuntimeException) {
            println("runtime error: $ex")
        }
}