import Instruction.*
import Operation.*
import org.sosy_lab.common.ShutdownManager
import org.sosy_lab.common.configuration.Configuration
import org.sosy_lab.common.log.BasicLogManager
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions

enum class Variable { W, X, Y, Z }
enum class Operation { Add, Mul, Div, Mod, Eql }

sealed class Instruction(open val variable: Variable) {
    data class Input(override val variable: Variable): Instruction(variable)
    data class Var(override val variable: Variable, val operation: Operation, val other: Variable): Instruction(variable)
    data class Const(override val variable: Variable, val operation: Operation, val number: Long): Instruction(variable)
}

fun String.parse(): Instruction {
    val parts = split(" ")
    val resultVariable = Variable.values().first { it.name.lowercase() == parts[1] }
    if (parts.first() == "inp") return Input(resultVariable)
    val arg = parts.last()
    val argVariable = Variable.values().find { it.name.lowercase() == arg }
    return if (argVariable != null) {
        when (parts.first()) {
            "add" -> Var(resultVariable, Add, argVariable)
            "mul" -> Var(resultVariable, Mul, argVariable)
            "div" -> Var(resultVariable, Div, argVariable)
            "mod" -> Var(resultVariable, Mod, argVariable)
            "eql" -> Var(resultVariable, Eql, argVariable)
            else -> error("Couldn't parse $this")
        }
    } else {
        val value = arg.toLong()
        when (parts.first()) {
            "add" -> Const(resultVariable, Add, value)
            "mul" -> Const(resultVariable, Mul, value)
            "div" -> Const(resultVariable, Div, value)
            "mod" -> Const(resultVariable, Mod, value)
            "eql" -> Const(resultVariable, Eql, value)
            else -> error("Couldn't parse $this")
        }
    }
}
class SmtSolver {
    private val config = Configuration.fromCmdLineArguments(emptyArray())
    private val context = SolverContextFactory(
        config,
        BasicLogManager.create(config),
        ShutdownManager.create().notifier
    ) { System.loadLibrary(it) }.generateContext(Solvers.Z3)
    private val booleanFormulaManager = context.formulaManager.booleanFormulaManager
    private val integerManager = context.formulaManager.integerFormulaManager
    private val inputExpressions = (0..13).map { integerManager.makeVariable("input-$it") }
    private lateinit var valueZ: IntegerFormula

    private val values: MutableMap<Variable, IntegerFormula> = Variable.values().associateWith { integerManager.makeNumber(0) }.toMutableMap()
    private var inputIndex = 0
    private fun integerFormula(left: IntegerFormula, operation: Operation, right: IntegerFormula): IntegerFormula = when (operation) {
        Add -> integerManager.add(left, right)
        Mul -> integerManager.multiply(left, right)
        Div -> integerManager.divide(left, right)
        Mod -> integerManager.modulo(left, right)
        Eql -> booleanFormulaManager.ifThenElse(integerManager.equal(left, right), integerManager.makeNumber(1), integerManager.makeNumber(0))
    }

    private fun processInstruction(instruction: Instruction) {
        val variable = instruction.variable
        val current = values.getValue(variable)
        fun assign(variable: Variable, integerFormula: IntegerFormula) {
            values[variable] = integerFormula
        }
        when (instruction) {
            is Input -> assign(variable, inputExpressions[inputIndex++])
            is Var -> {
                val otherValue = values.getValue(instruction.other)
                assign(variable, integerFormula(current, instruction.operation, otherValue))
            }
            is Const -> {
                val number = integerManager.makeNumber(instruction.number)
                if (instruction.operation == Mul && instruction.number == 0L) {
                    return assign(variable, integerManager.makeNumber(0L))
                }
                assign(variable, integerFormula(current, instruction.operation, number))
            }
        }
    }
    fun process(instructions: List<Instruction>) {
        instructions.forEach(::processInstruction)
        valueZ = values.getValue(Variable.Z)
    }
    fun solve(searchMax: Boolean): Long {
        val minValue = 1111_1111_1111_11L
        val maxValue = 9999_9999_9999_99L
        return binSearch(minValue, maxValue, if (searchMax) minValue else maxValue, searchMax)
    }

    private fun binSearch(begin: Long, end: Long, best: Long, searchMax: Boolean): Long {
        if (begin > end) return best
        val med = (begin + end) / 2L
        debug("Testing med: $med, begin=$begin, end=$end, best=$best, searchMax=$searchMax")
        context.newProverEnvironment(ProverOptions.GENERATE_MODELS).use { prover ->
            inputExpressions.forEach { inputVariable ->
                prover.addConstraint(integerManager.greaterOrEquals(inputVariable, integerManager.makeNumber(1)))
                prover.addConstraint(integerManager.lessOrEquals(inputVariable, integerManager.makeNumber(9)))
            }
            val zippedDigits = begin.toString().map(Char::digitToInt).zip(end.toString().map(Char::digitToInt))
            for ((inputVariable, digits) in inputExpressions.zip(zippedDigits)) {
                if (digits.first != digits.second) break
                prover.addConstraint(integerManager.equal(inputVariable, integerManager.makeNumber(digits.first.toLong())))
            }
            prover.addConstraint(integerManager.equal(valueZ, integerManager.makeNumber(0L)))
            val modelExpr = inputExpressions.fold(integerManager.makeNumber(0L)) { acc, expr ->
                integerManager.add(integerManager.multiply(integerManager.makeNumber(10L), acc), expr)
            }
            val medNumber = integerManager.makeNumber(med)
            val searchConstraint = if (searchMax) integerManager.greaterOrEquals(modelExpr, medNumber) else integerManager.lessOrEquals(modelExpr, medNumber)
            prover.addConstraint(searchConstraint)
            if (!prover.isUnsat) {
                prover.model.use { model ->
                    val newBest = model.evaluate(modelExpr)!!.toLong()
                    val newBegin = if (searchMax) newBest + 1 else begin
                    val newEnd = if (searchMax) end else newBest - 1
                    debug("SAT: $newBest")
                    return binSearch(newBegin, newEnd, newBest, searchMax)
                }
            } else {
                val newBegin = if (searchMax) begin else med + 1
                val newEnd = if (searchMax) med - 1 else end
                return binSearch(newBegin, newEnd, best, searchMax)
            }
        }
    }
}

fun main() {
    val input = readInput("Day24").map(String::parse)
    with (SmtSolver()) {
        measure { process(input) }
        measure { println(solve(true)) }
        measure { println(solve( false)) }
    }
}