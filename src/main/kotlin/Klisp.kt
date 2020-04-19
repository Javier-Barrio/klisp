// Klisp. A simple lisp interpreter based on lispy.py (see http://norvig.com/lispy.html)

package klisp

import klisp.ast.*
import klisp.ast.Number
import klisp.ast.KString
import klisp.env.Environment
import klisp.env.Fun
import klisp.env.Procedure
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.NumberFormatException
import java.lang.RuntimeException

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

// simple string with no escape sequences
fun string(token: String): Expression {
    if (!token.startsWith("\""))
        throw IllegalArgumentException()
    if (!token.endsWith("\""))
        throw IllegalArgumentException()

    return KString(token.removePrefix("\"").removeSuffix("\""))
}

// special forms
fun form(token: String): Form {
    if ("if" == token)
        return if_()
    if ("define" == token)
        return define()
    if ("lambda" == token)
        return lambda()
    if ("quote" == token || token.first() == '\'')
        return quote()
    if ("set!" == token)
        return set()

    throw IllegalArgumentException("not a form: $token")
}

// set! symbol expr
fun set(): Form {
    val symbol = Symbol(pop())
    return Sets(symbol, expression())
}

// quote (1 2 3)
fun quote(): Form {
    return Quote(expression())
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
                return string(token)
            } catch (ex: IllegalArgumentException) {
                try {
                    return form(token)
                } catch (ex: IllegalArgumentException) {
                    return Symbol(token)
                }
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

    if (tokens.isEmpty())
        return Nil

    return expression() // root
}

fun eval(ast: Expression, env: Environment): Expression = when (ast) {
    is Number -> ast
    is Symbol -> env.find(ast.name)
    is Nil -> Nil
    is KString -> ast
    is Quote -> ast.expression

    is Sexpression -> {
        val head = ast.list[0]
        when (head) {
            is Define -> {
                val symbol = head.symbol
                val expr = head.expression
                env.symbols[symbol] = eval(expr, env)
                Nil
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

            is Quote -> head

            is Sets -> {
                env.symbols[head.symbol.name] = eval(head.expression, env)
                Nil
            }

            else -> {
                val first = eval(ast.list[0], env)

                if (first !is Procedure)
                    throw IllegalStateException("eval: $head is not a procedure")

                val args = ast.list.subList(1, ast.list.size)
                first(*args.map { eval(it, env) }.toTypedArray())
            }
        }
    }

    else -> {
        throw IllegalStateException("eval $ast")
    }
}

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
