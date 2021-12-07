fun main() {
    val input = readInput("Day01").map(String::toInt)
    val solutionA = input
        .windowed(2)
        .count { (prev, next) -> next > prev }
    println(solutionA)

    val solutionB = input
        .windowed(3)
        .map(List<Int>::sum)
        .windowed(2)
        .count { (prev, next) -> next > prev }
    println(solutionB)
}
