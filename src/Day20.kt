const val PADDING_SIZE = 50
fun main() {
    val input = readInput("Day20")
        .filterNot(String::isBlank)
        .map { it.replace("#", "1").replace(".", "0") }
    val algorithm =  input.first()
    val image = input.drop(1)
    val imageWithPadding = buildList {
        val padding = buildString { repeat(PADDING_SIZE) { append('0') } }
        val emptyLine = (0 until image.first().length + PADDING_SIZE * 2).map { '0' }.joinToString("")
        repeat(PADDING_SIZE) {
            add(emptyLine)
        }
        image.forEach { line ->
            add(padding + line + padding)
        }
        repeat(PADDING_SIZE) {
            add(emptyLine)
        }
    }
    val dyDx = listOf((-1 to -1), (-1 to 0), (-1 to 1), (0 to -1), (0 to 0), (0 to 1), (1 to -1), (1 to 0), (1 to 1))
    fun List<String>.applyStep(stepNumber: Int): List<String> {
        fun charValue(y: Int, x: Int): Char = when {
            y in indices && x in this[y].indices -> this[y][x]
            stepNumber % 2 == 0 -> '0'
            else -> algorithm.first()
        }
        return indices.map { y ->
            this[y].indices.map { x ->
                val pattern = dyDx.map { (dy, dx) -> charValue(y + dy, x + dx) }.joinToString("")
                algorithm[pattern.toInt(2)]
            }.joinToString("")
        }
    }
    fun List<String>.solve(steps: Int): Int {
        var result = this
        repeat(steps) { step ->
            result = result.applyStep(step)
        }
        return result.sumOf { it.count { c -> c == '1' } }
    }
    println(imageWithPadding.solve(2))
    println(imageWithPadding.solve(50))
}