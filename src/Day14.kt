import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import java.util.Collections.max
import java.util.Collections.min

fun main() {
    val input = readInput("Day14")
    val template = input.first()
    val insertionRules = input.drop(2).associate {
        val parts = it.split(" -> ")
        parts.first() to parts.last()
    }
    val pairOrdering = insertionRules.keys.toList().sorted()
    val matrix = mk.zeros<Long>(pairOrdering.size, pairOrdering.size)
    insertionRules.forEach { (pair, middle) ->
        val pairIndex = pairOrdering.indexOf(pair)
        val firstIndex = pairOrdering.indexOf(pair.first() + middle)
        val secondIndex = pairOrdering.indexOf(middle + pair.last())
        matrix[firstIndex, pairIndex]++
        matrix[secondIndex, pairIndex]++
    }
    val vector = mk.zeros<Long>(pairOrdering.size)
    template.windowed(2).forEach {
        vector[pairOrdering.indexOf(it)]++
    }
    fun solve(times: Int): Long {
        val matrixPower = (0 until times).fold(mk.identity<Long>(pairOrdering.size)) { acc, _ -> matrix dot acc }
        val resultVector = matrixPower dot vector
        val counts = pairOrdering.flatMap { listOf(it.first(), it.last()) }.distinct().associateWith { 0L }.toMutableMap()
        resultVector.toList().forEachIndexed { index, value ->
            counts[pairOrdering[index].first()] = counts.getValue(pairOrdering[index].first()) + value
        }
        counts[template.last()] = counts.getValue(template.last()) + 1
        return max(counts.values) - min(counts.values)
    }
    println(solve(10))
    println(solve(40))
}
