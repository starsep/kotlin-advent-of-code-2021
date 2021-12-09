/*
0 1 2 3 4 5 6 7 8 9
6 2 5 5 4 5 6 3 7 6
1 7 4 2 3 5 0 6 9 8
2 3 4 5 5 5 6 6 6 7
 */
fun main() {
    val input = readInput("Day08")
    val uniqueSizes = listOf(2, 3, 4, 7)
    val solution = input.sumOf { line ->
        val (_, outputValues) = line.split("|").map { it.split(" ").filterNot(String::isBlank) }
        outputValues.count { it.length in uniqueSizes }
    }
    println(solution)

    val solutionB = input.sumOf { line ->
        val (signalPatterns, outputValues) = line.split("|").map { it.split(" ").filterNot(String::isBlank).map(String::toSet) }
        val (signal1, signal7, signal4, signal8) = uniqueSizes.map { size -> signalPatterns.first { it.size == size } }
        val (signals235, signals069) = listOf(5, 6).map { size -> signalPatterns.filter { it.size == size } }
        val signal9 = signals069.first { (it - signal4).size == 2 }
        val signal6 = signals069.first { it.intersect(signal1).size == 1 }
        val signal0 = signals069.first { it !in setOf(signal6, signal9) }
        val signal5 = signals235.first { it.intersect(signal6).size == 5 }
        val signal3 = signals235.first { it.intersect(signal1).size == 2 }
        val signal2 = signals235.first { it !in setOf(signal3, signal5) }
        val mapping = mapOf(
            signal0 to 0,
            signal1 to 1,
            signal2 to 2,
            signal3 to 3,
            signal4 to 4,
            signal5 to 5,
            signal6 to 6,
            signal7 to 7,
            signal8 to 8,
            signal9 to 9,
        )
        outputValues.fold(0L) { acc, signal -> 10L * acc + mapping.getValue(signal) }
    }
    println(solutionB)
}
