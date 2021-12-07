fun main() {
    val input = readInput("Day06").first().split(",").map(String::toInt)
    fun solve(days: Int) {
        val lanternFish = LongArray(9) { 0 }
        for (x in input) lanternFish[x]++
        repeat(days) {
            val zeroes = lanternFish[0]
            for (i in 0..7) lanternFish[i] = lanternFish[i + 1]
            lanternFish[8] = zeroes
            lanternFish[6] += zeroes
        }
        println(lanternFish.sum())
    }
    solve(80)
    solve(256)
}
