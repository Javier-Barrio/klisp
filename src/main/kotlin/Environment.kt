package klisp.env

import klisp.ast.Expression
import klisp.ast.Number
import klisp.ast.Integer
import klisp.eval

// Convenience method to convert a `klisp` expression list to a par of doubles for evaluation.
fun toDoublePair(vararg args: Expression): Pair<Double, Double> {
    assert(args.size == 2)

    val a: Double = if (args[0] is Number) {
        (args[0] as Number).value
    } else  { (args[0] as Integer).value.toDouble() }
    val b: Double = if (args[1] is Number) {
        (args[1] as Number).value
    } else { (args[1] as Integer).value.toDouble() }

    return Pair(a, b)
}

fun toDoubleOne(vararg args: Expression): Double {
    assert(args.size == 1)

    val expr = args.first()

    return if (expr is Number)
        expr.value
    else (expr as Integer).value.toDouble()
}

interface Procedure: Expression {
    operator fun invoke(vararg args: Expression): Expression
}

// Default environment with some standard symbols
class Environment(val parent: Environment?) {
    val symbols = HashMap<String, Expression>()

    init {
        // arith and relational ops
        symbols["+"] = object: Procedure {
                override operator fun invoke(vararg args: Expression): Expression {
                    val (a, b) = toDoublePair(*args)
                    return Number(a + b)
                }
        }

        symbols["-"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                val (a, b) = toDoublePair(*args)
                return Number(a - b)
            }
        }

        symbols["*"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                val (a, b) = toDoublePair(*args)
                return Number(a * b)
            }
        }

        symbols["/"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                val (a, b) = toDoublePair(*args)
                return Number(a / b)
            }
        }

        symbols["="] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                val (a, b) = toDoublePair(*args)
                return Number(if (a == b) 1.0 else 0.0)
            }
        }

        symbols["<="] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                val (a, b) = toDoublePair(*args)
                return Number(if (a <= b) 1.0 else 0.0)
            }
        }

        symbols[">="] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                val (a, b) = toDoublePair(*args)
                return Number(if (a >= b) 1.0 else 0.0)
            }
        }

        symbols["<"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                val (a, b) = toDoublePair(*args)
                return Number(if (a < b) 1.0 else 0.0)
            }
        }

        symbols[">"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                val (a, b) = toDoublePair(*args)
                return Number(if (a > b) 1.0 else 0.0)
            }
        }

        // math
        symbols["pi"] = Number(Math.PI)
        symbols["e"] = Number(Math.E)

        symbols["cos"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                return Number(Math.cos(toDoubleOne(*args)))
            }
        }

        symbols["sin"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                return Number(Math.sin(toDoubleOne(*args)))
            }
        }

        symbols["abs"] =  object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                return Number(Math.abs(toDoubleOne(*args)))
            }
        }

        symbols["pow"] =  object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                val (a, b) = toDoublePair(*args)
                return Number(Math.pow(a, b))
            }
        }
    }

    fun find(name: String): Expression {
        if (name in symbols)
            return symbols[name]!!
        return parent!!.find(name)
    }
}

// Lambda evaluation. Maps `params` to `args` and applies `body` in the new environment
class Fun(val body: Expression, val params: List<String>, val outer: Environment): Procedure {
    override operator fun invoke(vararg args: Expression): Expression {
        assert(args.size == params.size)

        val env = Environment(outer)
        var i = 0

        for (param in params)
            env.symbols[param] = args[i++]

        return eval(body, env)
    }
}