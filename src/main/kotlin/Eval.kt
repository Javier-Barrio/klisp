package klisp

import klisp.objects.*
import klisp.objects.Number
import klisp.objects.KString
import klisp.env.Environment
import klisp.env.Fun
import klisp.env.Procedure
import java.lang.IllegalStateException

private fun define(def: Define, env: Environment): Nil {
    val symbol = def.symbol
    val expr = def.expression
    env.symbols[symbol] = eval(expr, env)
    return Nil
}

private fun if_(head: If, env: Environment): Expression {
    val expr = eval(head.test, env)
    assert(expr is Number)
    val test = expr.numeric()

    if (test != 0.0)
        return eval(head.c, env)

    return eval(head.a, env)
}

private fun sets(head: Sets, env: Environment): Nil {
    env.symbols[head.symbol.name] = eval(head.expression, env)
    return Nil
}

private fun procedure(ast: Sexpression, env: Environment): Expression {
    val first = eval(ast.list[0], env)

    if (first !is Procedure)
        throw IllegalStateException("eval: $first is not a procedure")

    val args = ast.list.drop(1)
    return first(*args.map { eval(it, env) }.toTypedArray())
}

private fun quote(ast: Sexpression): Expression {
    return Sexpression(ast.list.map {
        if (it is Symbol)
            KString(it.name)
        else
            it
    })
}

fun eval(ast: Expression, env: Environment): Expression = when (ast) {
    // atoms
    is Number -> ast
    is Symbol -> env.find(ast.name)
    is Nil -> Nil
    is KString -> ast

    // s-expression and special forms
    is Sexpression -> {
        val head = ast.list[0]
        when (head) {
            is Define -> define(head, env)
            is If -> if_(head, env)
            is Lambda -> Fun(head.expression, head.args, env)
            is Sets -> sets(head, env)
            is Quote -> quote(head.expression as Sexpression)
            else -> procedure(ast, env)
        }
    }

    else -> {
        throw IllegalStateException("eval $ast")
    }
}