import java.io.File
import kotlin.system.measureTimeMillis

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = File("src", "$name.txt").readLines()

fun debug(message: String) = Unit // println(message)
fun <T> checkEquals(actual: T, expected: T) = check(actual == expected) {
    "Different values\n  actual: $actual\nexpected: $expected"
}

fun <T> runTimes(times: Int, foo: () -> T) = (0 until times).map { foo() }
fun <T> measure(foo: () -> T): T {
    var result: T? = null
    val timeSpent = measureTimeMillis { result = foo() }
    println("Took $timeSpent ms")
    return result!!
}
fun <T> List<T>.replaceIndex(index: Int, element: T) = take(index) + listOf(element) + takeLast(size - index - 1)