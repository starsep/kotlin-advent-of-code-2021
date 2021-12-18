import io.uuddlrlrba.ktalgs.datastructures.PriorityQueue

const val RESIZE_RATIO = 5

fun main() {
    val input = readInput("Day15").map { it.split("").filterNot(String::isBlank).map(String::toInt).toIntArray() }.toTypedArray()
    fun solve(costs: Array<IntArray>): Int {
        val result = Array(costs.size) { IntArray(costs.first().size) { Int.MAX_VALUE - 10 } }
        data class Vertex(val y: Int, val x: Int) : Comparable<Vertex> {
            override fun compareTo(other: Vertex): Int {
                val resultCompare = result[y][x].compareTo(result[other.y][other.x])
                val yCompare = y.compareTo(other.y)
                val xCompare = x.compareTo(other.x)
                return if (resultCompare != 0) resultCompare
                else if(yCompare != 0) yCompare
                else xCompare
            }
        }

        val indices = costs.indices.flatMap { y -> costs.first().indices.map { x -> Vertex(y, x) } }.toMutableSet()
        val queue = PriorityQueue<Vertex>(indices.size) { a, b -> a.compareTo(b) }
        indices.forEach { queue.add(it) }
        result[0][0] = 0
        while (queue.isNotEmpty()) {
            val (y, x) = queue.poll()
            listOf(-1 to 0, 0 to -1, 0 to 1, 1 to 0).forEach { (dy, dx) ->
                val ny = y + dy
                val nx = x + dx
                if (ny in result.indices && nx in result[ny].indices) {
                    if (result[ny][nx] > result[y][x] + costs[ny][nx]) {
                        result[ny][nx] = result[y][x] + costs[ny][nx]
                        queue.add(Vertex(ny, nx))
                    }
                }
            }
        }
        return result.last().last()
    }
    println(solve(input))
    val input2 = Array(input.size * RESIZE_RATIO) { y -> IntArray(input.first().size * RESIZE_RATIO) { x ->
        val y0 = y % input.size
        val x0 = x % input.first().size
        val dy = y / input.size
        val dx = x / input.first().size
        (input[y0][x0] + dx + dy - 1) % 9 + 1
    } }
    println(solve(input2))
}