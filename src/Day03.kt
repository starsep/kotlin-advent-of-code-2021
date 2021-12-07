fun main() {
    val input = readInput("Day03")
    val length = input.first().length
    val solution = (0 until length).fold(0 to 0) { (a, b), column ->
        input
            .partition { it[column] == '1' }
            .run { (2 * a + if (first.size >= second.size) 1 else 0) to (2 * b + if (first.size >= second.size) 0 else 1) }
    }.run { first * second }
    println(solution)
    val solutionB = (0 until length).fold(input to input) { (oxygen, carbonDioxide), column ->
        val (oxygen0, oxygen1) = oxygen.partition { it[column] == '0' }
        val newOxygen = if (oxygen1.size >= oxygen0.size) oxygen1 else oxygen0
        val (carbonDioxide0, carbonDioxide1) = carbonDioxide.partition { it[column] == '0' }
        val newCarbonDioxide =
            if (carbonDioxide0.isEmpty()) carbonDioxide1
            else if (carbonDioxide1.isEmpty()) carbonDioxide0
            else if (carbonDioxide0.size <= carbonDioxide1.size) carbonDioxide0 else carbonDioxide1
        newOxygen to newCarbonDioxide
    }.run { first.first().toInt(2) * second.first().toInt(2) }
    println(solutionB)
}
