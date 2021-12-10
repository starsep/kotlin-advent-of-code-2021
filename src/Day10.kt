fun main() {
    val input = readInput("Day10")
    val scoreMap = mapOf(')' to 3, ']' to 57, '}' to 1197, '>' to 25137)
    val matchingMap = mapOf(')' to '(', ']' to '[', '}' to '{', '>' to '<')
    val missingMap = mapOf('(' to 1, '[' to 2, '{' to 3, '<' to 4)
    val missingScores = mutableListOf<Long>()
    val result = input.sumOf { line ->
        val stack = mutableListOf<Char>()
        for (character in line) {
            if (character !in matchingMap.keys) {
                stack.add(character)
                continue
            }
            if (stack.isEmpty() || stack.last() != matchingMap[character]) {
                return@sumOf scoreMap.getValue(character)
            }
            stack.removeLast()
        }
        missingScores.add(stack.reversed().fold(0L) { acc, missingChar ->
            5L * acc + missingMap.getValue(missingChar)
        })
        0
    }
    println(result)
    println(missingScores.sorted()[missingScores.size / 2])
}