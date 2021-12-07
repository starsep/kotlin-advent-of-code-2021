import kotlin.math.abs

fun main() {
    val input = readInput("Day05").map { it.split("->", ",").map(String::trim).map(String::toInt) }
    val maxX = input.maxOf { maxOf(it[0], it[2]) }
    val maxY = input.maxOf { maxOf(it[1], it[3]) }
    var vents = Array(maxY + 1) { IntArray(maxX + 1) { 0 } }
    for ((x0, y0, x1, y1) in input) {
        if (x0 == x1) for (y in minOf(y0, y1)..maxOf(y0, y1)) vents[y][x0]++
        else if (y0 == y1) for (x in minOf(x0, x1)..maxOf(x0, x1)) vents[y0][x]++
    }
    println(vents.sumOf { row -> row.count { it >= 2 } })

    vents = Array(maxY + 1) { IntArray(maxX + 1) { 0 } }
    for ((x0, y0, x1, y1) in input) {
        if (x0 == x1) for (y in minOf(y0, y1)..maxOf(y0, y1)) vents[y][x0]++
        else if (y0 == y1) for (x in minOf(x0, x1)..maxOf(x0, x1)) vents[y0][x]++
        else {
            val deltaX = if (x0 < x1) 1 else -1
            val deltaY = if (y0 < y1) 1 else -1
            for (i in 0..abs(x0 - x1)) {
                vents[y0 + deltaY * i][x0 + deltaX * i]++
            }
        }
    }
    println(vents.sumOf { row -> row.count { it >= 2 } })
}
