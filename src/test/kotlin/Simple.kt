package klisp.test

import org.junit.Test
import klisp.*
import klisp.objects.KString
import klisp.env.*
import klisp.objects.Expression
import klisp.objects.Number
import klisp.objects.Sexpression
import klisp.parser.parse
import java.lang.AssertionError
import java.lang.Exception
import java.lang.StringBuilder
import kotlin.test.assertEquals

class SimpleTest {
    @Test
    fun simple() {
        val global = Environment(null)
        val code = "(begin (define fact (lambda (n) (if (< n 2) 1 (* n (fact (- n 1)))))) (fact 4))"
        val expr = eval(parse(StringBuilder(code)), global)

        assertEquals(Number(24.0), expr)

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

    @Test
    fun strings() {
        val global = Environment(null)
        val expr = eval(parse(StringBuilder(
                "(begin (define str \"foo\") (string-append str \"bar\"))")), global)
        assertEquals(expr, KString("foobar"))
    }

    @Test
    fun quote1() {
        val code = "(quote (1 2 3))"
        val env = Environment(null)
        val ret = eval(parse(StringBuilder(code)), env)
        assertEquals(Sexpression(listOf(Number(1.0), Number(2.0), Number(3.0))), ret)
    }

    @Test
    fun setsymbol() {
        val code = "(begin (define foo 1) (set! foo (lambda (x) (* x x))) (foo 2))"
        val env = Environment(null)
        val ret = eval(parse(StringBuilder(code)), env)
        assertEquals(Number(4.0), ret)
    }
}
