import kotlin.math.abs

fun main() {
    val input = readInput("Day07").first().split(",").map(String::toInt)
    val sorted = input.sorted()
    var prefix = 0
    var suffix = sorted.drop(1).sum()
    var best = Int.MAX_VALUE
    val n = sorted.size
    for (i in sorted.indices) {
        val current = sorted[i]
        suffix -= current
        // current * i - prefix + suffix - current * (n - i - 1)
        val result = suffix - prefix + current * (2 * i + 1 - n)
        best = minOf(best, result)
        prefix += current
    }
    println(best)

    fun squareSum(x: Int) = x * (x + 1) / 2
    val resultB = sorted.minOf { current -> input.sumOf { squareSum(abs(current - it)) } }
    println(resultB)
}
