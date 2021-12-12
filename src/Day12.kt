fun main() {
    val input = readInput("Day12").map { it.split("-") }
    val graph = input.flatMap { listOf(it.first() to mutableListOf<String>(), it.last() to mutableListOf()) }.distinct().toMap()
    input.forEach {
        graph.getValue(it.first()).add(it.last())
        graph.getValue(it.last()).add(it.first())
    }
    fun dfs(path: List<String>, nextPredicate: (String, List<String>) -> Boolean): Int {
        val current = path.last()
        if (current == "end") return 1
        return graph
            .getValue(current)
            .filter { nextPredicate(it, path) }
            .sumOf { dfs(path + it, nextPredicate) }
    }
    println(dfs(listOf("start")) { v, path -> v.lowercase() != v || v !in path })
    println(dfs(listOf("start")) { v, path ->
        val small = path.filter { it.lowercase() == it }
        val smallDoubled = small.size != small.distinct().size
        v != "start" && (v.lowercase() != v || v !in path || !smallDoubled)
    })
}