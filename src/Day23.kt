import java.util.PriorityQueue
import kotlin.math.abs

const val LETTERS = "ABCD"
val targetIndices = listOf(2, 4, 6, 8)
val costs = listOf(1, 10, 100, 1000)
val solutions = mutableMapOf<String, Int>()
data class Board(
    val top: String = "...........",
    val rows: List<String>,
): Comparable<Board> {
    private val stringCache = """
        #############
        #$top#
        ###${rows.first().toList().joinToString("#")}###
        ${rows.drop(1).joinToString("\n") { "  #" + it.toList().joinToString("#") + "#  " }}
          #########  
    """.trimIndent()
    override fun toString() = stringCache

    fun moves(): List<Pair<Board, Int>> = buildList {
        fun String.replaceChar(index: Int, char: Char) = replaceRange(index..index, char.toString())
        top.withIndex().filterNot { it.value == '.' }.forEach { (index, letter) ->
            val letterIndex = LETTERS.indexOf(letter)
            val targetIndex = targetIndices[letterIndex]
            val cost = costs[letterIndex]
            val distance = abs(targetIndex - index)
            val needsEmpty = if (index <= targetIndex) (index + 1)..targetIndex else targetIndex until index
            if (needsEmpty.any { top[it] != '.' }) return@forEach
            rows.forEachIndexed { targetRowIndex, targetRow ->
                if ((0..targetRowIndex).all { rows[it][letterIndex] == '.' } &&
                    ((targetRowIndex + 1) until rows.size).all { rows[it][letterIndex] == letter }) {
                    add(copy(
                        top = top.replaceChar(index, '.'),
                        rows = rows.replaceIndex(targetRowIndex, targetRow.replaceChar(letterIndex, letter))
                    ) to cost * (distance + targetRowIndex + 1))
                }
            }
        }
        rows.withIndex().forEach { (rowIndex, row) ->
            row.withIndex().filterNot { it.value == '.' }.forEach { (index, letter) ->
                val letterIndex = LETTERS.indexOf(letter)
                val completed = index == letterIndex && (rowIndex..rows.lastIndex).all { rows[it][index] == letter }
                val hasSpace = (0 until rowIndex).all { rows[it][index] == '.' }
                if (!completed && hasSpace) {
                    val cost = costs[letterIndex]
                    val column = targetIndices[index]
                    (top.indices - targetIndices).forEach { moveIndex ->
                        val range = if (column <= moveIndex) column..moveIndex else moveIndex..column
                        val distance = range.length().toInt() + rowIndex
                        if (range.all { top[it] == '.' }) {
                            add(copy(
                                top = top.replaceChar(moveIndex, letter),
                                rows = rows.replaceIndex(rowIndex, row.replaceChar(index, '.'))
                            ) to distance * cost)
                        }
                    }
                }
            }
        }
    }

    override fun compareTo(other: Board): Int {
        val cost = cost()
        val otherCost = other.cost()
        if (cost != null && otherCost != null && cost != otherCost) {
            return cost.compareTo(otherCost)
        }
        val topCompare = top.compareTo(other.top)
        if (topCompare != 0) return topCompare
        for ((row, otherRow) in rows.zip(other.rows)) {
            val compareRow = row.compareTo(otherRow)
            if (compareRow != 0) return compareRow
        }
        return 0
    }

    fun cost(): Int? = solutions[toString()]
    fun solvedBoard() = Board(rows = rows.map { LETTERS })
}

class Solver {
    fun solve(initialBoard: Board): Int {
        solutions.clear()
        solutions[initialBoard.toString()] = 0
        val queue = PriorityQueue<Board>()
        queue.add(initialBoard)
        val visited = mutableSetOf<Board>()
        while (queue.isNotEmpty()) {
            val board = queue.poll()
            if (board in visited) continue
            visited.add(board)
            val cost = board.cost()!!
            board.moves().forEach { (newBoard, extraCost) ->
                if (newBoard.cost() == null || cost + extraCost < newBoard.cost()!!) {
                    solutions[newBoard.toString()] = cost + extraCost
                    queue.add(newBoard)
                }
            }
        }
        return initialBoard.solvedBoard().cost()!!
    }
}

fun main() {
    val example = Board(rows = listOf("BCBD", "ADCA"))
    val input = Board(rows = listOf("ACBA", "DDBC"))
    val solver = Solver()
    measure { println(solver.solve(example)) }
    measure { println(solver.solve(input)) }
    val extraLines = listOf("DCBA", "DBAC")
    val example2 = Board(rows = example.rows.take(1) + extraLines + example.rows.takeLast(1))
    val input2 = Board(rows = input.rows.take(1) + extraLines + input.rows.takeLast(1))
    measure { println(solver.solve(example2)) }
    measure { println(solver.solve(input2)) }
}