import Scanner.CommonConfiguration
import kotlin.math.abs

data class Point(val x: Int, val y: Int, val z: Int) {
    override fun toString() = "$x,$y,$z"
    operator fun plus(other: Point) = Point(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y, z - other.z)
    fun distance(other: Point): Int = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)
}
data class Scanner(val points: Set<Point>) {
    data class CommonConfiguration(val pointsCount: Int, val configuration: Configuration, val vector: Point)
    fun bestCommonConfiguration(scanner: Scanner): CommonConfiguration {
        var result = CommonConfiguration(pointsCount = 0, configuration = configurations.first(), Point(0, 0, 0))
        configurations.forEach { configuration ->
            points.forEach { point ->
                scanner.points.forEach { other ->
                    val vector = point - configuration(other)
                    val pointConfiguration: Configuration = { p -> configuration(p) + vector }
                    val mappedPoints = scanner.points.map(pointConfiguration).toSet()
                    val common = mappedPoints.intersect(points)
                    if (common.size > result.pointsCount) {
                        result = CommonConfiguration(common.size, pointConfiguration, vector)
                    }
                }
            }
        }
        return result
    }
}
typealias Configuration = (Point) -> Point

fun findPointsAndVectors(
    firstScanner: Scanner,
    secondScanner: Scanner,
    allScanners: List<Scanner>,
    firstMatch: CommonConfiguration
): Pair<Set<Point>, List<Point>> {
    val points = mutableSetOf<Point>()
    points.addAll(firstScanner.points)
    points.addAll(secondScanner.points.map(firstMatch.configuration))
    val scannerPoints = mutableListOf(Point(0, 0, 0), firstMatch.vector)
    val leftScanners = (allScanners - firstScanner - secondScanner).toMutableSet()
    while (leftScanners.isNotEmpty()) {
        val currentScanner = Scanner(points)
        val scanner = leftScanners.first {
            currentScanner.bestCommonConfiguration(it).pointsCount >= COMMON_POINTS_THRESHOLD
        }
        val match = currentScanner.bestCommonConfiguration(scanner)
        points.addAll(scanner.points.map(match.configuration))
        leftScanners.remove(scanner)
        scannerPoints.add(match.vector)
    }
    return points to scannerPoints
}

fun solve(scanners: List<Scanner>): Pair<Set<Point>, List<Point>> =
    scanners.firstNotNullOf { scanner ->
        scanners.filterNot { it == scanner }.firstNotNullOf { other ->
            val match = scanner.bestCommonConfiguration(other)
            when {
                match.pointsCount >= COMMON_POINTS_THRESHOLD -> {
                    findPointsAndVectors(scanner, other, scanners, match)
                }
                else -> null
            }
        }
    }

fun main() {
    val input = readInput("Day19")
    val currentPoints = mutableSetOf<Point>()
    val scanners: List<Scanner> = buildList {
        input.filterNot(String::isBlank).forEach { line ->
            if (line.startsWith("---")) {
                if (currentPoints.isNotEmpty()) {
                    add(Scanner(currentPoints.toSet()))
                    currentPoints.clear()
                }
            } else {
                val (x, y, z) = line.split(",").map(String::toInt)
                currentPoints.add(Point(x, y, z))
            }
        }
        add(Scanner(currentPoints.toSet()))
    }
    val (beacons, scannerPoints) = solve(scanners)
    println(beacons.size)
    val maxDistance = scannerPoints.maxOf { scannerPoints.maxOf(it::distance) }
    println(maxDistance)
}

const val COMMON_POINTS_THRESHOLD = 12

val configurations = listOf<Configuration>(
    { (x, y, z) -> Point(x, y, z) },
    { (x, y, z) -> Point(x, -y, -z) },
    { (x, y, z) -> Point(-x, y, -z) },
    { (x, y, z) -> Point(-x, -y, z) },
    { (x, y, z) -> Point(x, z, -y) },
    { (x, y, z) -> Point(x, -z, y) },
    { (x, y, z) -> Point(-x, z, y) },
    { (x, y, z) -> Point(-x, -z, -y) },
    { (x, y, z) -> Point(y, x, -z) },
    { (x, y, z) -> Point(y, -x, z) },
    { (x, y, z) -> Point(-y, x, z) },
    { (x, y, z) -> Point(-y, -x, -z) },
    { (x, y, z) -> Point(y, z, x)  },
    { (x, y, z) -> Point(y, -z, -x) },
    { (x, y, z) -> Point(-y, z, -x) },
    { (x, y, z) -> Point(-y, -z, x) },
    { (x, y, z) -> Point(z, x, y) },
    { (x, y, z) -> Point(z, -x, -y) },
    { (x, y, z) -> Point(-z, x, -y) },
    { (x, y, z) -> Point(-z, -x, y) },
    { (x, y, z) -> Point(z, y, -x) },
    { (x, y, z) -> Point(z, -y, x) },
    { (x, y, z) -> Point(-z, y, x) },
    { (x, y, z) -> Point(-z, -y, -x) },
)