typealias Land = List<String>

fun String.charRotate() = replace("v", "x")
    .replace(">", "v")
    .replace("x", ">")
fun Land.transpose(): Land = first().indices.map { column ->
    map { it[column] }
        .joinToString("")
        .charRotate()
}

fun move(land: Land): Land = land
    .map(::moveEast)
    .transpose()
    .map(::moveEast)
    .transpose()

fun moveEast(line: String) = (line.last() + line + line.first()).windowed(3) { triplet ->
    when {
        triplet.first() == '>' && triplet[1] == '.' -> '>'
        triplet.last() == '.' && triplet[1] == '>' -> "."
        else -> triplet[1]
    }
}.joinToString("")

fun main() {
    val input = readInput("Day25")
    var prev = input
    (1..Int.MAX_VALUE).forEach { step ->
        val moved = move(prev)
        if (moved == prev) {
            println(step)
            return
        }
        prev = moved
    }
}