package klisp.objects

import java.lang.IllegalStateException
import java.lang.StringBuilder

// EXPRESSION
interface Expression

fun Expression.numeric(): Double {
    if (this is Number)
        return this.value

    throw IllegalStateException("Expression.numeric $this")
}

data class Sexpression(val list: List<Expression>): Expression {
    override fun toString(): String {
        val buf = StringBuilder("(")
        for (e in list)
            buf.append(" $e ")
        return buf.append(")").toString()
    }
}

// ATOMS
object Nil: Expression {
    override fun toString(): String = "nil"
}

data class Symbol(val name: String): Expression {
    override fun toString(): String = "$name"
}

data class Number(val value: Double): Expression {
    override fun toString(): String = "$value"
}

data class KString(val value: String): Expression {
    override fun toString(): String = value
}

// SPECIAL FORMS
data class Define(val symbol: String, val expression: Expression): Expression
data class Lambda(val args: List<String>, val expression: Expression): Expression
data class If(val test: Expression, val c: Expression, val a: Expression): Expression
data class Quote(val expression: Expression): Expression
data class Sets(val symbol: Symbol, val expression: Expression): Expression
