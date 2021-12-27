import kotlinx.coroutines.*

data class CubeInstruction(val switch: Boolean, val x: IntRange, val y: IntRange, val z: IntRange)

infix fun IntRange.within(other: IntRange) = first >= other.first && last <= other.last
fun IntRange.length() = last - first + 1L

// O(n^4)
// 195864ms ~= 3m16s on 3900X
fun main() {
    val input = readInput("Day22").filterNot(String::isBlank).map { line ->
        val (onOff, rest) = line.split(" ")
        val (x, y, z) = rest.split(",").map {
            val (start, end) = it.split("=").last().split("..").map(String::toInt)
            IntRange(start, end)
        }
        CubeInstruction(onOff == "on", x, y, z)
    }
    val smallCubeRange = -50..50
    val withinCubeInstructions = input.filter {
        it.x within smallCubeRange && it.y within smallCubeRange && it.z within smallCubeRange
    }
    val cubesSwitchedOn =
        smallCubeRange.sumOf { x ->
            smallCubeRange.sumOf { y ->
                smallCubeRange.count { z ->
                    withinCubeInstructions.findLast { x in it.x && y in it.y && z in it.z }?.switch ?: false
                }
            }
        }
    println(cubesSwitchedOn)
    fun normalize(values: List<Int>): List<IntRange> = buildList {
        fun IntRange.addToList() { if (!this.isEmpty()) add(this) }
        for ((a, b) in values.zipWithNext()) {
            (a..a).addToList()
            (a+1 until b).addToList()
        }
        val end = values.last()
        (end..end).addToList()
    }
    fun List<CubeInstruction>.normalizeAlongAx(ax: (CubeInstruction) -> IntRange) =
        normalize(flatMap { listOf(ax(it).first, ax(it).last) }.distinct().sorted())
    val xNormalized = input.normalizeAlongAx(CubeInstruction::x)
    val yNormalized = input.normalizeAlongAx(CubeInstruction::y)
    val zNormalized = input.normalizeAlongAx(CubeInstruction::z)
    measure {
        runBlocking {
            xNormalized.map { x ->
                async(Dispatchers.Default) {
                    yNormalized.sumOf { y ->
                        zNormalized.sumOf { z ->
                            val q = input.findLast { x within it.x && y within it.y && z within it.z }
                            if (q?.switch == true) x.length() * y.length() * z.length() else 0L
                        }
                    }
                }
            }.awaitAll().sum().run { println(this) }
        }
    }
}