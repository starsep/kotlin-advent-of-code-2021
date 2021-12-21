import kotlin.math.max

fun simulateGame(positions: Array<Int>): Int {
    var dice = 1
    var move = 0
    var rolls = 0
    val scores = arrayOf(0, 0)
    fun rollDice(): Int = dice.also {
        dice++
        rolls++
        if (dice == 101) dice = 1
    }
    fun movePawn() {
        val player = move % 2
        val rolled = runTimes(3, ::rollDice).sum()
        positions[player] += rolled
        positions[player] = (positions[player] - 1) % 10 + 1
        scores[player] += positions[player]
        move++
    }
    while (scores.maxOf { it } < 1000) {
        movePawn()
    }
    return rolls * scores.minOf { it }
}

fun calculate3RollCombinations(maxRoll: Int): List<Pair<Int, Int>> {
    val result = mutableMapOf<Int, Int>()
    (1..maxRoll).forEach { roll0 ->
        (1..maxRoll).forEach { roll1 ->
            (1..maxRoll).forEach { roll2 ->
                result[roll0 + roll1 + roll2] = result.getOrDefault(roll0 + roll1 + roll2, 0) + 1
            }
        }
    }
    return result.toList()
}

fun calculateGame(startPositions: Array<Int>): Long {
    val rollsCount = 3
    val maxRoll = 3
    val winningScore = 21
    val maxScore1 = winningScore + maxRoll * rollsCount + 1
    val scoreRange = 0 until maxScore1
    val maxPosition = 10
    val positionRange = 0 until maxPosition
    val game = Array(maxScore1) { Array(maxScore1) { Array(maxPosition) { Array(maxPosition) { Array(2) { 0L } } } } }
    game[0][0][startPositions.first() - 1][startPositions.last() - 1][0] = 1L
    val rolls = calculate3RollCombinations(maxRoll)
    checkEquals(calculate3RollCombinations(maxRoll), listOf(3 to 1, 4 to 3, 5 to 6, 6 to 7, 7 to 6, 8 to 3, 9 to 1))
    for (score0 in scoreRange) {
        for (score1 in scoreRange) {
            val scores = listOf(score0, score1)
            for (position0 in positionRange) {
                for (position1 in positionRange) {
                    val positions = listOf(position0, position1)
                    for ((roll, combinations) in rolls) {
                        for (turnBefore in 0..1) {
                            val turnAfter = if (turnBefore == 0) 1 else 0
                            val otherScore = scores[turnAfter]
                            val positionAfter = (positions[turnBefore] + roll) % maxPosition
                            val scoreGained = positionAfter + 1
                            val prevScore = scores[turnBefore]
                            val newScore = prevScore + scoreGained
                            if (newScore < maxScore1 && otherScore < winningScore) {
                                val current = game[score0][score1][position0][position1][turnBefore] * combinations
                                if (turnBefore == 0) {
                                    game[newScore][score1][positionAfter][position1][turnAfter] += current
                                } else {
                                    game[score0][newScore][position0][positionAfter][turnAfter] += current
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    fun sumResult(foo: (Int, Int, Int, Int) -> Long) =
        (winningScore until maxScore1).sumOf { score ->
            (0 until winningScore).sumOf { otherScore ->
                positionRange.sumOf { position0 ->
                    positionRange.sumOf { position1 ->
                        foo(score, otherScore, position0, position1)
                    }
                }
            }
        }
    val firstPlayer = sumResult { score, otherScore, position0, position1 ->
        game[score][otherScore][position0][position1][1]
    }
    val secondPlayer = sumResult { score, otherScore, position0, position1 ->
        game[otherScore][score][position0][position1][0]
    }
    return max(firstPlayer, secondPlayer)
}

fun main() {
    val input = readInput("Day21").map { it.split(": ").last().toInt() }.toTypedArray()
    println(simulateGame(input.clone()))
    println(calculateGame(input))
}