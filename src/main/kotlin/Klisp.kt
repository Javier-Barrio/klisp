// Klisp. A simple lisp interpreter based on lispy.py (see http://norvig.com/lispy.html)

package klisp

import klisp.ast.*
import klisp.ast.Number
import klisp.env.Environment
import klisp.env.Fun
import klisp.env.Procedure
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.NumberFormatException

var tokens = emptyList<String>()

fun read(): StringBuilder {
    val line = readLine()
    return StringBuilder(line)
}

fun pop(): String {
    val ret = tokens.first()
    tokens = tokens.drop(1)
    return ret
}

fun peek(): String = tokens.first()

fun form(token: String): Form {
    if ("if" == token)
        return if_()
    if ("define" == token)
        return define()
    if ("lambda" == token)
        return lambda()

    throw IllegalArgumentException("not a form: $token")
}

// if (+ a b) c a
fun if_(): Form {
    val test = expression()
    val c = expression()
    val a = expression()
    return If(test, c, a)
}

// define foo expr
fun define(): Form {
    return Define(pop(), expression())
}

// lambda (n) (+ 1 n)
fun lambda(): Form {
    var params: List<String> = mutableListOf()

    if (peek() != "(")
        throw IllegalArgumentException("expected open parenthesis")

    pop()
    while (peek() != ")") {
        params += pop()
    }
    pop()

    return Lambda(params, expression())
}

fun expression(): Expression {
    if (peek() == "(") {
        pop()
        var exprs = emptyList<Expression>()
        while (peek() != ")")
            exprs += expression()
        pop()
        return Sexpression(exprs)
    }

    if (peek() == ")")
        throw IllegalArgumentException("Unexpected ) token")

    val token = pop()

    try {
        val num = token.toInt()
        return Number(num.toDouble())
    } catch (ex: NumberFormatException) {
        try {
            val double = token.toDouble()
            return Number(double)
        } catch (ex: NumberFormatException) {
            try {
                return form(token)
            } catch (ex: IllegalArgumentException) {
                return Symbol(token)
            }
        }
    }
}

fun parse(buffer: StringBuilder): Expression {
    tokens = buffer.replace(Regex("\\("), " ( ")
        .replace(")", " ) ")
        .split(" ").filter {
            !it.isBlank() && !it.isEmpty()
        }

    return expression() // root
}

fun eval(ast: Expression, env: Environment): Expression = when (ast) {
    is Number -> ast
    is Symbol -> env.find(ast.name)

    is Sexpression -> {
        val head = ast.list[0]
        when (head) {
            is Define -> {
                val symbol = head.symbol
                val expr = head.expression
                env.symbols[symbol] = eval(expr, env)
                Nil()
            }

            is If -> {
                val expr = eval(head.test, env)
                assert(expr is Number)
                val test = expr.numeric()

                if (test != 0.0)
                    eval(head.c, env)
                else
                    eval(head.a, env)
            }

            is Lambda -> Fun(head.expression, head.args, env)

            else -> {
                val proc = eval(ast.list[0], env) as Procedure
                val args = ast.list.subList(1, ast.list.size)
                proc(*args.map { eval(it, env) }.toTypedArray())
            }
        }
    }

    else -> {
        throw IllegalStateException("eval $ast")
    }
}

fun main() {
    println("Hello, this is Klisp 1.0 (Cntl-D to exit)")
    val global = Environment(null)
    while (true)
        print(eval(parse(read()), global))
}
