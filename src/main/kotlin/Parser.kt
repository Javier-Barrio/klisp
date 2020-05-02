package klisp.parser

import klisp.objects.*
import klisp.objects.Number

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
fun form(token: String): Expression {
    if ("if" == token)
        return if_()
    if ("define" == token)
        return define()
    if ("lambda" == token)
        return lambda()
    if ("set!" == token)
        return set()
    if ("quote" == token)
        return quote()

    throw IllegalArgumentException("not a form: $token")
}

fun quote(): Expression {
    return Quote(expression())
}

// set! symbol expr
fun set(): Expression {
    val symbol = Symbol(pop())
    return Sets(symbol, expression())
}

// if (+ a b) c a
fun if_(): Expression {
    val test = expression()
    val c = expression()
    val a = expression()
    return If(test, c, a)
}

// define foo expr
fun define(): Expression {
    return Define(pop(), expression())
}

// lambda (n) (+ 1 n)
fun lambda(): Expression {
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