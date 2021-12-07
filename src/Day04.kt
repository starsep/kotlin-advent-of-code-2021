fun main() {
    val input = readInput("Day04").filterNot(String::isEmpty)
    val numbers = input.first().split(",").map(String::toInt)
    val bingos = input
        .drop(1)
        .windowed(5, 5)
        .map { bingo ->
            bingo.map { row ->
                row
                    .split(" ")
                    .filterNot(String::isBlank)
                    .map(String::toInt)
            }
        }
    fun bingoResult(bingo: List<List<Int>>, crossed: Set<Int>, called: Int): Int? {
        val result = bingo.sumOf { row -> row.filterNot { it in crossed }.sum() } * called
        for (row in bingo) if (row.filterNot { it in crossed }.isEmpty()) return result
        for (columnIndex in (0 until bingo.first().size)) {
            if (bingo.map { it[columnIndex] }.filterNot { it in crossed }.isEmpty()) return result
        }
        return null
    }
    val crossed = mutableSetOf<Int>()
    loop@ for (called in numbers) {
        crossed.add(called)
        for (bingo in bingos) {
            val result = bingoResult(bingo, crossed, called)
            if (result != null) {
                println(result)
                break@loop
            }
        }
    }

    crossed.clear()
    val bingosWon = mutableSetOf<Int>()
    loop@ for (called in numbers) {
        crossed.add(called)
        for ((bingoIndex, bingo) in bingos.withIndex()) {
            val result = bingoResult(bingo, crossed, called)
            if (result != null) {
                bingosWon.add(bingoIndex)
                if (bingosWon.size == bingos.size) {
                    println(result)
                    break@loop
                }
            }
        }
    }
}
