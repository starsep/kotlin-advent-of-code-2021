import Packet.*
import TypeId.*

enum class TypeId(val id: Int) {
    SUM(0),
    PRODUCT(1),
    MINIMUM(2),
    MAXIMUM(3),
    LITERAL(4),
    GREATER_THAN(5),
    LESS_THEN(6),
    EQUAL_TO(7),
}

sealed class Packet(open val version: Int, open val typeId: TypeId) {
    abstract fun versionSum(): Int
    abstract fun calculate(): Long
    data class LiteralValue(override val version: Int, override val typeId: TypeId, val value: Long): Packet(version, typeId) {
        override fun versionSum() = version
        override fun calculate() = value
    }
    data class OperatorPacket(override val version: Int, override val typeId: TypeId, val packets: List<Packet>): Packet(version, typeId) {
        override fun versionSum() = version + packets.sumOf(Packet::versionSum)
        private fun calculateCompareTo() = packets.first().calculate().compareTo(packets.last().calculate())
        private fun Boolean.toLong() = if(this) 1L else 0L
        override fun calculate() = when(typeId) {
            SUM -> packets.sumOf(Packet::calculate)
            PRODUCT -> packets.fold(1L) { acc, packet -> acc * packet.calculate() }
            MINIMUM -> packets.minOf(Packet::calculate)
            MAXIMUM -> packets.maxOf(Packet::calculate)
            LITERAL -> error("operator")
            GREATER_THAN -> (calculateCompareTo() > 0).toLong()
            LESS_THEN -> (calculateCompareTo() < 0).toLong()
            EQUAL_TO -> (calculateCompareTo() == 0).toLong()
        }
    }
}

class Parser(val input: String) {
    private val bits = input.split("").filterNot(String::isBlank).joinToString("") {
        it.toInt(16).toString(2).padStart(BIT_ALIGNMENT, '0')
    }
    private var pointer = 0
    private var version = 0
    private var typeId = SUM

    private fun parseVersionAndTypeId() {
        version = bits.substring(pointer until pointer + VERSION_SIZE).toInt(2)
        movePointer(VERSION_SIZE, 'V')
        typeId = values().first { it.id == bits.substring(pointer until pointer + TYPE_ID_SIZE).toInt(2) }
        movePointer(TYPE_ID_SIZE, 'T')
    }

    private fun parseLiteralValue(): LiteralValue {
        debug("parsing literal value bits=${bits.substring(pointer)}")
        val number = buildString {
            do {
                val group = bits.substring(pointer until pointer + LITERAL_GROUP_SIZE)
                movePointer(LITERAL_GROUP_SIZE, 'L')
                append(group.drop(1))
            } while (group.startsWith("1"))
        }.toLong(2)
        return LiteralValue(version, typeId, number).also { debug("Parsed $it") }
    }

    private fun parseOperatorPacket(): Packet {
        val currentVersion = version
        val currentTypeId = typeId
        debug("Starting to parse operator packet version=$currentVersion type=$currentTypeId")
        val lengthTypeId = bits.substring(pointer..pointer).toInt()
        movePointer(1, 'I')
        val packets = when (lengthTypeId) {
            LENGTH_TYPE_BITS -> {
                val bitsLength = bits.substring(pointer until pointer + LENGTH_BITS_SIZE).toInt(2)
                movePointer(LENGTH_BITS_SIZE, 'B')
                debug("Parsed bits length $bitsLength")
                subparse(parsedBitsLimit = bitsLength, parsedPacketsLimit = Int.MAX_VALUE)
            }
            LENGTH_TYPE_PACKETS -> {
                val packetsLength = bits.substring(pointer until pointer + LENGTH_PACKETS_SIZE).toInt(2)
                movePointer(LENGTH_PACKETS_SIZE, 'P')
                debug("Parsed packets length $packetsLength")
                subparse(parsedPacketsLimit = packetsLength)
            }
            else -> error("Unknown length type: $lengthTypeId")
        }
        return OperatorPacket(currentVersion, currentTypeId, packets).also { debug("Parsed $it") }
    }

    private fun subparse(
        parsedBitsLimit: Int = Int.MAX_VALUE,
        parsedPacketsLimit: Int = 1,
    ): List<Packet> = buildList {
        val initPointer = pointer
        debug("subparse(bits=${bits.substring(pointer)})")
        while (pointer < bits.length && pointer - initPointer < parsedBitsLimit && size < parsedPacketsLimit) {
            parseVersionAndTypeId()
            when (typeId) {
                LITERAL -> add(parseLiteralValue())
                else -> add(parseOperatorPacket())
            }
        }
    }

    private fun movePointer(times: Int, debugChar: Char) {
        pointer += times
        repeat(times) {
            debugChars.add(debugChar)
        }
    }

    fun parse(): Packet {
        debug("parse(bits=$bits)")
        return subparse(parsedPacketsLimit = 1).first().also { printDebugInfo() }
    }

    companion object {
        private const val BIT_ALIGNMENT = 4
        private const val VERSION_SIZE = 3
        private const val TYPE_ID_SIZE = 3
        private const val LITERAL_GROUP_SIZE = 5
        private const val LENGTH_TYPE_BITS = 0
        private const val LENGTH_TYPE_PACKETS = 1
        private const val LENGTH_BITS_SIZE = 15
        private const val LENGTH_PACKETS_SIZE = 11
    }

    private var debugChars = mutableListOf<Char>()
    private fun printDebugInfo() {
        debug(bits)
        debug(debugChars.joinToString(""))
        debug("-----------------")
    }
}

fun main() {
    with(Parser("D2FE28").parse()) {
        checkEquals(this, LiteralValue(6, LITERAL, 2021))
        checkEquals(versionSum(), 6)
        checkEquals(calculate(), 2021)
    }

    with(Parser("38006F45291200").parse()) {
        checkEquals(
            this,
            OperatorPacket(
                1, LESS_THEN, listOf(
                    LiteralValue(6, LITERAL, 10),
                    LiteralValue(2, LITERAL, 20),
                )
            )
        )
        checkEquals(versionSum(), 9)
        checkEquals(calculate(), 1)
    }

    with(Parser("EE00D40C823060").parse()) {
        checkEquals(
            this,
            OperatorPacket(
                7, MAXIMUM, listOf(
                    LiteralValue(2, LITERAL, 1),
                    LiteralValue(4, LITERAL, 2),
                    LiteralValue(1, LITERAL, 3),
                )
            )
        )
        checkEquals(versionSum(), 14)
        checkEquals(calculate(), 3)
    }

    with(Parser("8A004A801A8002F478").parse()) {
        checkEquals(this,
            OperatorPacket(4, MINIMUM, listOf(
                OperatorPacket(1, MINIMUM, listOf(
                    OperatorPacket(5, MINIMUM, listOf(
                        LiteralValue(6, LITERAL, 15)
                    )))))))
        checkEquals(versionSum(), 16)
        checkEquals(calculate(), 15)
    }

    with(Parser("620080001611562C8802118E34").parse()) {
        checkEquals(this,
            OperatorPacket(3, SUM, listOf(
                OperatorPacket(0, SUM, listOf(
                    LiteralValue(0, LITERAL, 10),
                    LiteralValue(5, LITERAL, 11),
                )),
                OperatorPacket(1, SUM, listOf(
                    LiteralValue(0, LITERAL, 12),
                    LiteralValue(3, LITERAL, 13),
                ))
            )))
        checkEquals(versionSum(), 12)
        checkEquals(calculate(), 46)
    }

    with(Parser("C0015000016115A2E0802F182340").parse()) {
        checkEquals(this,
            OperatorPacket(6, SUM, listOf(
                OperatorPacket(0, SUM, listOf(
                    LiteralValue(0, LITERAL, 10),
                    LiteralValue(6, LITERAL, 11),
                )),
                OperatorPacket(4, SUM, listOf(
                    LiteralValue(7, LITERAL, 12),
                    LiteralValue(0, LITERAL, 13),
                ))
            )))
        checkEquals(versionSum(), 23)
        checkEquals(calculate(), 46)
    }

    with(Parser("A0016C880162017C3686B18A3D4780").parse()) {
        checkEquals(this,
            OperatorPacket(5, SUM, listOf(
                OperatorPacket(1, SUM, listOf(
                    OperatorPacket(3, SUM, listOf(
                        LiteralValue(7, LITERAL, 6),
                        LiteralValue(6, LITERAL, 6),
                        LiteralValue(5, LITERAL, 12),
                        LiteralValue(2, LITERAL, 15),
                        LiteralValue(2, LITERAL, 15),
                    )))))))
        checkEquals(versionSum(), 31)
        checkEquals(calculate(), 54)
    }

    val input = readInput("Day16").first()
    val packet = Parser(input).parse()
    println(packet.versionSum())
    println(packet.calculate())
}
