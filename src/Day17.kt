import kotlin.math.abs
import kotlin.math.max

data class Target(val x: IntRange, val y: IntRange)

fun simulate(initialXSpeed: Int, initialYSpeed: Int, target: Target): Int? {
    var topY = 0
    var x = 0
    var y = 0
    var xSpeed = initialXSpeed
    var ySpeed = initialYSpeed
    while (y >= target.y.first || ySpeed >= 0) {
        x += xSpeed
        y += ySpeed
        topY = max(topY, y)
        if (x in target.x && y in target.y) return topY
        if (xSpeed > 0) xSpeed--
        ySpeed--
    }
    return null
}

/*
Non-positive x velocity makes no sense
it would cause probe to go left.
Let's assume x>0.

For y velocity <= 0, maximal y position is the starting one (0).
Let's assume y>0.
*/
fun topY(target: Target) =
    (1..abs(target.y.first)).maxOf { y ->
        (1..target.x.last).maxOf { x ->
            simulate(x, y, target) ?: 0
        }
    }

fun countDistinctVelocities(target: Target) =
    (target.y.first..abs(target.y.first)).sumOf { y ->
        (1..target.x.last).count { x ->
            simulate(x, y, target) != null
        }
    }

fun main() {
    val example = Target(20..30, -10..-5)
    val input = Target(281..311, -74..-54)
    println(topY(example))
    println(topY(input))
    println(countDistinctVelocities(example))
    println(countDistinctVelocities(input))
}
