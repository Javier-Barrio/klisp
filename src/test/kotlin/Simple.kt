package klisp.test

import org.junit.Test
import klisp.*
import klisp.env.*
import klisp.ast.Number
import java.lang.StringBuilder
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimpleTest {
    @Test
    fun simple() {
        val global = Environment(null)
        assertTrue {
            val expr = eval(parse(StringBuilder(
                    "(begin (define fact (lambda (n) (if (< n 2) 1 (* n (fact (- n 1))))))\n" +
                "(fact 4))")), global)
            expr == Number(24.0)
        }

        assertEquals(
            eval(parse(StringBuilder(
                    "(begin (define fact (lambda (n) (if (< n 2) 1 (* n (fact (- n 1))))))\n" +
                            "(fact 10))")), global),
            Number(3628800.0))

        assertEquals(
            eval(parse(StringBuilder(
                    "(begin (define circle-radius (lambda (r) (* pi (* r r))))\n" +
                                "(circle-radius 3))")), global),
            Number(28.274333882308138))
    }
}
