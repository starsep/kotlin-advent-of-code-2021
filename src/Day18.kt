import SnailfishNumber.Companion.Token.*

sealed class SnailfishNumber {
    data class ExplodeResult(val exploded: SnailfishNumber, val left: Int?, val right: Int?)
    abstract fun split(): SnailfishNumber
    abstract fun explode(depth: Int = 0): ExplodeResult
    abstract fun reduce(): SnailfishNumber
    abstract fun magnitude(): Int
    fun add(other: SnailfishNumber) = SnailfishPair(this, other).reduce()

    data class SnailfishPair(val left: SnailfishNumber, val right: SnailfishNumber): SnailfishNumber() {
        override fun toString() = "[$left,$right]"
        override fun magnitude() = 3 * left.magnitude() + 2 * right.magnitude()
        override fun split(): SnailfishNumber {
            val leftSplit = left.split()
            val rightSplit = if (leftSplit == left) right.split() else right
            return when {
                leftSplit == left && rightSplit == right -> this
                else -> SnailfishPair(leftSplit, rightSplit)
            }
        }

        override fun reduce(): SnailfishNumber {
            val (exploded, _) = explode(0)
            if (exploded != this) return exploded.reduce()
            val split = split()
            if (split != this) return split.reduce()
            return this
        }

        override fun explode(depth: Int): ExplodeResult {
            if (depth > 4) error("depth > 4")
            if (depth == 4) {
                check(left is RegularNumber)
                check(right is RegularNumber)
                return ExplodeResult(RegularNumber(0), left.value, right.value)
            }
            val (leftExploded, leftLeft, leftRight) = left.explode(depth + 1)
            if (leftExploded != left) {
                val newRight = when {
                    leftRight != null -> when (right) {
                        is RegularNumber -> right + leftRight
                        is SnailfishPair -> right.addFirstRight(leftRight)
                    }
                    else -> right
                }
                return ExplodeResult(SnailfishPair(leftExploded, newRight), leftLeft, if (newRight != right) null else leftRight)
            }
            val (rightExploded, rightLeft, rightRight) = right.explode(depth + 1)
            if (rightExploded != right) {
                val newLeft = when {
                    rightLeft != null -> when (left) {
                        is RegularNumber -> left + rightLeft
                        is SnailfishPair -> left.addLatestLeft(rightLeft)
                    }
                    else -> left
                }
                return ExplodeResult(SnailfishPair(newLeft, rightExploded), if (newLeft != left) null else rightLeft, rightRight)
            }
            return ExplodeResult(this, null, null)
        }

        fun addLatestLeft(value: Int): SnailfishPair =
            SnailfishPair(left, when (right) {
                is RegularNumber -> right + value
                is SnailfishPair ->  right.addLatestLeft(value)
            })

        fun addFirstRight(value: Int): SnailfishPair =
            SnailfishPair(when (left) {
                is RegularNumber -> left + value
                is SnailfishPair -> left.addFirstRight(value)
            }, right)
    }

    data class RegularNumber(val value: Int): SnailfishNumber() {
        override fun toString() = value.toString()
        override fun magnitude() = value
        override fun split() = when {
            value < 10 -> this
            else -> SnailfishPair(RegularNumber(value / 2), RegularNumber(value / 2 + value % 2))
        }
        override fun explode(depth: Int) = ExplodeResult(this, null, null)
        override fun reduce() = split()
        operator fun plus(other: Int) = RegularNumber(value + other)
    }

    companion object {
        fun parse(s: String) = parse(lex(s))
        private fun parse(tokens: List<Token>) : SnailfishNumber {
            if (tokens.size == 1) {
                val number = tokens.first()
                check(number is NumberToken)
                return RegularNumber(number.value)
            }
            check(tokens.last() is ClosingBracket)
            var bracketsDifference = 0
            for ((index, token) in tokens.withIndex()) {
                when (token) {
                    is OpeningBracket -> bracketsDifference++
                    is ClosingBracket -> bracketsDifference--
                    is Comma -> {
                        if (bracketsDifference == 1) {
                            return SnailfishPair(parse(tokens.subList(1, index)), parse(tokens.subList(index + 1, tokens.size - 1)))
                        }
                    }
                    is NumberToken -> {}
                }
            }
            error("Couldn't parse $tokens")
        }

        private fun lex(s: String): List<Token> = buildList {
            s.forEach { char ->
                when (char) {
                    '[' -> add(OpeningBracket)
                    ']' -> add(ClosingBracket)
                    ',' -> add(Comma)
                    in '0'..'9' -> {
                        if (last() is NumberToken) {
                            val number = removeLast() as NumberToken
                            add(NumberToken(number.value * 10 + char.digitToInt()))
                        }
                        else {
                            add(NumberToken(char.digitToInt()))
                        }
                    }
                }
            }
        }
        sealed class Token {
            object OpeningBracket: Token() { override fun toString() = "[" }
            object ClosingBracket: Token() { override fun toString() = "]" }
            object Comma: Token() { override fun toString() = "," }
            data class NumberToken(val value: Int): Token()  { override fun toString() = value.toString() }
        }
    }
}

fun parse(s: String) = SnailfishNumber.parse(s)
fun List<String>.add() = map(SnailfishNumber.Companion::parse).reduce(SnailfishNumber::add)

fun main() {
    checkEquals((parse("[[4,0],[5,0]]") as SnailfishNumber.SnailfishPair).addLatestLeft(4), parse("[[4,0],[5,4]]"))
    checkEquals((parse("[6,[5,[4,[3,2]]]]") as SnailfishNumber.SnailfishPair).addFirstRight(3), parse("[9,[5,[4,[3,2]]]]"))
    checkEquals(parse("[[[[[9,8],1],2],3],4]").reduce(), parse("[[[[0,9],2],3],4]"))
    checkEquals(parse("[7,[6,[5,[4,[3,2]]]]]").reduce(), parse("[7,[6,[5,[7,0]]]]"))
    checkEquals(parse("[[6,[5,[4,[3,2]]]],1]").reduce(), parse("[[6,[5,[7,0]]],3]"))
    checkEquals(parse("[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]").explode().exploded, parse("[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]"))
    checkEquals(
        parse("[[[[4,0],[5,0]],[[[4,5],[2,6]],[9,5]]],[7,[[[3,7],[4,3]],[[6,3],[8,8]]]]]").explode().exploded,
        parse("[[[[4,0],[5,4]],[[0,[7,6]],[9,5]]],[7,[[[3,7],[4,3]],[[6,3],[8,8]]]]]")
    )
    checkEquals(parse("[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]").reduce(), parse("[[3,[2,[8,0]]],[9,[5,[7,0]]]]"))
    checkEquals(
        parse("[[[[0,[4,5]],[0,0]],[[[4,5],[2,6]],[9,5]]],[7,[[[3,7],[4,3]],[[6,3],[8,8]]]]]").reduce(),
        parse("[[[[4,0],[5,4]],[[7,7],[6,0]]],[[8,[7,7]],[[7,9],[5,0]]]]")
    )

    checkEquals(parse("[[[[4,3],4],4],[7,[[8,4],9]]]").add(parse("[1,1]")), parse("[[[[0,7],4],[[7,8],[6,0]]],[8,1]]"))
    val example = listOf("[1,1]", "[2,2]", "[3,3]", "[4,4]")
    checkEquals(example.add(), parse("[[[[1,1],[2,2]],[3,3]],[4,4]]"))
    checkEquals((example + "[5,5]").add(), parse("[[[[3,0],[5,3]],[4,4]],[5,5]]"))
    checkEquals((example + "[5,5]" + "[6,6]").add(), parse("[[[[5,0],[7,4]],[5,5]],[6,6]]"))

    val slightlyLargerExample = """
        [[[0,[4,5]],[0,0]],[[[4,5],[2,6]],[9,5]]]
        [7,[[[3,7],[4,3]],[[6,3],[8,8]]]]
        [[2,[[0,8],[3,4]]],[[[6,7],1],[7,[1,6]]]]
        [[[[2,4],7],[6,[0,5]]],[[[6,8],[2,8]],[[2,1],[4,5]]]]
        [7,[5,[[3,8],[1,4]]]]
        [[2,[2,2]],[8,[8,1]]]
        [2,9]
        [1,[[[9,3],9],[[9,0],[0,7]]]]
        [[[5,[7,4]],7],1]
        [[[[4,2],2],6],[8,7]]
    """.trimIndent().lines()
    val partialResults = """
        [[[[4,0],[5,4]],[[7,7],[6,0]]],[[8,[7,7]],[[7,9],[5,0]]]]
        [[[[6,7],[6,7]],[[7,7],[0,7]]],[[[8,7],[7,7]],[[8,8],[8,0]]]]
        [[[[7,0],[7,7]],[[7,7],[7,8]]],[[[7,7],[8,8]],[[7,7],[8,7]]]]
        [[[[7,7],[7,8]],[[9,5],[8,7]]],[[[6,8],[0,8]],[[9,9],[9,0]]]]
        [[[[6,6],[6,6]],[[6,0],[6,7]]],[[[7,7],[8,9]],[8,[8,1]]]]
        [[[[6,6],[7,7]],[[0,7],[7,7]]],[[[5,5],[5,6]],9]]
        [[[[7,8],[6,7]],[[6,8],[0,8]]],[[[7,7],[5,0]],[[5,5],[5,6]]]]
        [[[[7,7],[7,7]],[[8,7],[8,7]]],[[[7,0],[7,7]],9]]
        [[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]
    """.trimIndent().lines()
    for ((index, partial) in partialResults.withIndex()) {
        checkEquals(slightlyLargerExample.take(index + 2).add(), parse(partial))
    }

    val exampleHomeworkAssignment = """
        [[[0,[5,8]],[[1,7],[9,6]]],[[4,[1,2]],[[1,4],2]]]
        [[[5,[2,8]],4],[5,[[9,9],0]]]
        [6,[[[6,2],[5,6]],[[7,6],[4,7]]]]
        [[[6,[0,7]],[0,9]],[4,[9,[9,0]]]]
        [[[7,[6,4]],[3,[1,3]]],[[[5,5],1],9]]
        [[6,[[7,3],[3,2]]],[[[3,8],[5,7]],4]]
        [[[[5,4],[7,7]],8],[[8,3],8]]
        [[9,3],[[9,9],[6,[4,9]]]]
        [[2,[[7,7],7]],[[5,8],[[9,3],[0,2]]]]
        [[[[5,2],5],[8,[3,7]]],[[5,[7,5]],[4,4]]]
    """.trimIndent().lines()
    checkEquals(exampleHomeworkAssignment.add(), parse("[[[[6,6],[7,6]],[[7,7],[7,0]]],[[[7,7],[7,7]],[[7,8],[9,9]]]]"))

    val input = readInput("Day18")
    println(input.add().magnitude())

    val numbers = input.map(SnailfishNumber.Companion::parse)
    val result = numbers.maxOf { x ->
        numbers.maxOf { y -> if (x != y) x.add(y).magnitude() else 0 }
    }
    println(result)
}