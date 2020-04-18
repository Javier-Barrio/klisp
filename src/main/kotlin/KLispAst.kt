package klisp.ast

import java.lang.IllegalStateException

interface Expression
sealed class Form: Expression

class Nil: Expression {
    override fun toString(): String = "nil"
}

data class Symbol(val name: String): Expression

data class Number(val value: Double): Expression {
    override fun toString(): String = "$value"
}

data class KString(val value: String): Expression {
    override fun toString(): String = value
}

fun Expression.numeric(): Double {
    if (this is Number)
        return this.value

    throw IllegalStateException("Expression.numeric $this")
}

class Sexpression(val list: List<Expression>): Expression
class Define(val symbol: String, val expression: Expression): Form()
class Lambda(val args: List<String>, val expression: Expression): Form()
class If(val test: Expression, val c: Expression, val a: Expression): Form()
