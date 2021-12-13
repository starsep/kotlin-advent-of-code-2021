fun main() {
    val (foldLines, dotLines) = readInput("Day13").filterNot(String::isBlank).partition { it.startsWith("fold") }
    val folds = foldLines.map {
        val parts = it.removePrefix("fold along ").split("=")
        parts.first() to parts.last().toInt()
    }
    val dots = dotLines.map { it.split(",").map(String::toInt) }.map { it.first() to it.last() }
    fun foldPoint(point: Int, ax: Int): Int? = if (point < ax) point else (ax - (point - ax)).run { if (this < 0) null else this }
    fun fold(ax: Pair<String, Int>, dots: List<Pair<Int, Int>>) = dots.mapNotNull {
        val x = if (ax.first == "x") foldPoint(it.first, ax.second) else it.first
        val y = if (ax.first == "y") foldPoint(it.second, ax.second) else it.second
        if (x == null || y == null) null else x to y
    }.distinct()
    println(fold(folds.first(), dots).size)

    val result = folds.fold(dots) { acc, ax -> fold(ax, acc)}.toSet()
    val maxX = result.maxOf(Pair<Int, Int>::first)
    val maxY = result.maxOf(Pair<Int, Int>::second)
    (0.. maxY).forEach {y ->
        (0..maxX).forEach { x -> print(if(x to y in result) "#" else "_") }
        println()
    }
}