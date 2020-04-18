package klisp.env

import klisp.ast.Expression
import klisp.ast.Nil
import klisp.ast.Number
import klisp.eval

// Convenience method to convert a `klisp` expression list to a par of doubles for evaluation.
fun toDoublePair(vararg args: Expression): Pair<Double, Double> {
    assert(args.size == 2)

    return Pair((args[0] as Number).value, (args[1] as Number).value)
}

fun toDoubleOne(vararg args: Expression): Double {
    assert(args.size == 1)
    return (args.first() as Number).value
}

interface Procedure: Expression {
    operator fun invoke(vararg args: Expression): Expression
}

// Default environment with some standard symbols
class Environment(val parent: Environment?) {
    val symbols = HashMap<String, Expression>()

    init {
        // stdlib
        symbols["begin"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                var last: Expression = Nil()

                for (i in 0 until args.size) {
                    if (i == 0) continue
                    last = eval(args[i], this@Environment)
                }

                return last
            }
        }

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