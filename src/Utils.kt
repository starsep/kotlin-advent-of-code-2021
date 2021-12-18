import java.io.File

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = File("src", "$name.txt").readLines()

fun debug(message: String) = Unit // println(message)
fun <T> checkEquals(actual: T, expected: T) = check(actual == expected) {
    "Different values\n  actual: $actual\nexpected: $expected"
}