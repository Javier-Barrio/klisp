package klisp.env

import klisp.ast.*
import klisp.ast.Number
import klisp.eval
import kotlin.system.exitProcess

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
                var last: Expression = Nil

                for (i in 0 until args.size) {
                    if (i == 0) continue
                    last = eval(args[i], this@Environment)
                }

                return last
            }
        }

        symbols["exit"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                exitProcess(0)
            }
        }

        symbols["string-append"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                val buf = StringBuilder()
                for (e in args)
                    buf.append(e.toString())
                return KString(buf.toString())
            }
        }

        symbols["string-length"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                assert(args.size == 1)

                val str = args[0] as KString
                return Number(str.value.length.toDouble())
            }
        }

        symbols["print"] = object: Procedure {
            override operator fun invoke(vararg args: Expression): Expression {
                for (e in args) {
                    if (e is KString)
                        print("\"$e\"")
                    else
                        print(e)
                }
                println()
                return Nil
            }
        }

        fun arithop(apply: (accum: Number, arg: Number) -> Number): Expression {
            return object: Procedure {
                override operator fun invoke(vararg args: Expression): Expression {
                    var result = Number(args[0].numeric())
                    for (e in args.asIterable().drop(1).map { it as Number }) {
                        result = apply(result, e)
                    }
                    return result
                }
            }
        }

        // arith and relational ops
        symbols["+"] = arithop { acc: Number, arg: Number -> Number(acc.value + arg.value) }

        symbols["-"] = arithop {acc: Number, arg: Number -> Number(acc.value - arg.value) }

        symbols["*"] = arithop {acc: Number, arg: Number -> Number(acc.value * arg.value) }

        symbols["/"] = arithop {acc: Number, arg: Number -> Number(acc.value / arg.value) }

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
        try {
            return parent!!.find(name)
        } catch (ex: NullPointerException) {
            return Nil
        }
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