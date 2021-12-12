const val SIZE = 10
val INDICES = 0 until SIZE
const val STEPS = 100
fun main() {
    val input = readInput("Day11").map { it.map(Char::toString).map(String::toInt).toIntArray() }
    val flashed = Array(SIZE) { BooleanArray(SIZE) { false } }
    var result = 0
    repeat(Int.MAX_VALUE) { step ->
        for (i in INDICES) for (j in INDICES) flashed[i][j] = false
        for (i in INDICES) for (j in INDICES) input[i][j]++
        fun flash(y: Int, x: Int) {
            if (input[y][x] <= 9 || flashed[y][x]) return
            flashed[y][x] = true
            for ((dy, dx) in listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)) {
                val ny = y + dy
                val nx = x + dx
                if (ny in INDICES && nx in INDICES) {
                    input[ny][nx]++
                    flash(ny, nx)
                }
            }
        }
        for (i in INDICES) for (j in INDICES) flash(i, j)
        for (i in INDICES) for (j in INDICES) if (flashed[i][j]) input[i][j] = 0
        val stepFlashes = flashed.sumOf { row -> row.count { it } }
        if (stepFlashes == SIZE * SIZE) {
            println(result)
            println(step + 1)
            return
        }
        if (step < STEPS) result += stepFlashes
    }
}