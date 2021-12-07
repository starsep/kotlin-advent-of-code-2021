fun main() {
    val input = readInput("Day02").map { commands -> commands.split(" ").run { first() to last().toInt() }}
    val solutionA = input.fold(0 to 0) {
            (horizontal, vertical), (direction, distance) ->
        when (direction) {
            "forward" -> (horizontal + distance) to vertical
            "down" -> horizontal to (vertical + distance)
            "up" -> horizontal to (vertical - distance)
            else -> TODO(direction)
        }
    }.run { first * second }
    println(solutionA)

    val solutionB = input.fold(listOf(0, 0, 0)) {
            (horizontal, vertical, aim), (direction, distance) ->
        when (direction) {
            "forward" -> listOf(horizontal + distance, vertical + aim * distance, aim)
            "down" -> listOf(horizontal, vertical, aim + distance)
            "up" -> listOf(horizontal, vertical, aim - distance)
            else -> TODO(direction)
        }
    }.run { first() * this[1] }
    println(solutionB)
}
