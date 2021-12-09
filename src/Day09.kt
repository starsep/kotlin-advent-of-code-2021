fun main() {
    val input = readInput("Day09").map { it.split("").filterNot(String::isBlank).map(String::toInt) }
    val height = input.size
    val width = input.first().size
    val result = input.indices.sumOf { y ->
        input[y].indices.sumOf { x ->
            val current = input[y][x]
            if (
                (y == 0 || input[y - 1][x] > current) &&
                (x == 0 || input[y][x - 1] > current) &&
                (y == height - 1 || input[y + 1][x] > current) &&
                (x == width - 1 || input[y][x + 1] > current)
            ) current + 1 else 0
        }
    }
    println(result)

    val visited = Array(height) { BooleanArray(width) { false } }
    fun dfs(y: Int, x: Int): Int {
        if (visited[y][x] || input[y][x] == 9) return 1
        visited[y][x] = true
        return 1 + listOf(-1 to 0, 0 to -1, 1 to 0, 0 to 1).sumOf { (dy, dx) ->
            val ny = y + dy
            val nx = x + dx
            if (ny in input.indices && nx in input.first().indices && !visited[ny][nx] && input[ny][nx] < 9)
                dfs(ny, nx)
            else 0
        }
    }
    val resultB = input.indices.flatMap { y ->
        input[y].indices.map { x -> dfs(y, x) }
    }.sorted().takeLast(3).reduce(Int::times)
    println(resultB)
}
