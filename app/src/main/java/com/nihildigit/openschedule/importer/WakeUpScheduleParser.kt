package com.nihildigit.openschedule.importer

import com.nihildigit.openschedule.model.Course
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.absoluteValue

/**
 * 将 WakeUpSchedule (iCalendar) 文件转换为应用内的 [Course] 数据。
 */
class WakeUpScheduleParser {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
    private val nodePattern = Regex("第\\s*(\\d+)\\s*[-~—–]\\s*(\\d+)节")
    private val colorPalette = listOf(
        0xFFE57373, 0xFF64B5F6, 0xFF81C784, 0xFFFFB74D,
        0xFF4DB6AC, 0xFFF06292, 0xFF9575CD, 0xFFAED581,
        0xFFFFD54F, 0xFF4FC3F7, 0xFFBA68C8, 0xFFFF8A65
    )

    fun parse(content: String): List<Course> {
        val rawEvents = buildEvents(content)
        if (rawEvents.isEmpty()) return emptyList()

        val firstWeekMonday = rawEvents.minOf { it.start.toLocalDate() }
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        return rawEvents.mapNotNull { event ->
            val description = event.description
            val (startNode, endNode) = parseNodes(description)
                ?: deriveNodesFromTime(event.start.toLocalTime(), event.end.toLocalTime())
                ?: return@mapNotNull null

            val startWeek = weeksBetween(firstWeekMonday, event.start.toLocalDate()) + 1
            val endWeek = weeksBetween(firstWeekMonday, event.untilDate ?: event.end.toLocalDate()) + 1

            val (room, teacher) = parseRoomAndTeacher(event.location, description)

            Course(
                name = event.summary,
                day = event.start.dayOfWeek.value,
                room = room,
                teacher = teacher,
                startNode = startNode,
                endNode = endNode,
                startWeek = startWeek,
                endWeek = maxOf(endWeek, startWeek),
                type = 0,
                note = description?.trim().orEmpty(),
                color = pickColor(event.summary)
            )
        }
    }

    private fun buildEvents(content: String): List<WakeUpEvent> {
        val events = mutableListOf<WakeUpEvent>()
        var collecting = false
        val buffer = mutableListOf<String>()

        content.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line == "BEGIN:VEVENT" -> {
                    collecting = true
                    buffer.clear()
                }
                line == "END:VEVENT" -> {
                    if (collecting) {
                        parseEvent(buffer)?.let { events += it }
                    }
                    collecting = false
                    buffer.clear()
                }
                collecting -> buffer += line
            }
        }
        return events
    }

    private fun parseEvent(lines: List<String>): WakeUpEvent? {
        val map = mutableMapOf<String, String>()
        lines.forEach { line ->
            val (rawKey, rawValue) = line.split(':', limit = 2).let {
                if (it.size < 2) return@forEach
                it[0] to it[1]
            }
            val key = rawKey.substringBefore(';')
            map[key] = rawValue
        }

        val summary = map["SUMMARY"] ?: return null
        val start = parseDateTime(map["DTSTART"]) ?: return null
        val end = parseDateTime(map["DTEND"]) ?: return null
        val rrule = map["RRULE"]

        return WakeUpEvent(
            summary = summary.trim(),
            location = map["LOCATION"]?.trim(),
            description = map["DESCRIPTION"]?.replace("\\n", "\n")?.trim(),
            start = start,
            end = end,
            untilDate = parseUntil(rrule),
        )
    }

    private fun parseDateTime(raw: String?): LocalDateTime? {
        raw ?: return null
        val normalized = raw.removeSuffix("Z")
        return LocalDateTime.parse(normalized, dateTimeFormatter)
    }

    private fun parseUntil(rrule: String?): LocalDate? {
        if (rrule.isNullOrBlank()) return null
        val until = rrule.split(';').firstOrNull { it.startsWith("UNTIL=") }?.substringAfter('=')
            ?: return null
        val normalized = until.removeSuffix("Z")
        return runCatching {
            LocalDateTime.parse(normalized, dateTimeFormatter).toLocalDate()
        }.getOrNull()
    }

    private fun parseNodes(description: String?): Pair<Int, Int>? {
        description ?: return null
        description.lineSequence().forEach { line ->
            val match = nodePattern.find(line)
            if (match != null) {
                val start = match.groupValues[1].toIntOrNull()
                val end = match.groupValues[2].toIntOrNull()
                if (start != null && end != null) {
                    return start to end
                }
            }
        }
        return null
    }

    private fun deriveNodesFromTime(start: LocalTime, end: LocalTime): Pair<Int, Int>? {
        val startNode = timeToNode(start) ?: return null
        val slots = Duration.between(start, end).toMinutes().div(45).coerceAtLeast(1)
        val endNode = startNode + slots.toInt() - 1
        return startNode to endNode
    }

    private fun timeToNode(time: LocalTime): Int? = when (time) {
        LocalTime.of(8, 0) -> 1
        LocalTime.of(8, 55) -> 2
        LocalTime.of(10, 10) -> 3
        LocalTime.of(11, 5) -> 4
        LocalTime.of(14, 0) -> 5
        LocalTime.of(14, 55) -> 6
        LocalTime.of(16, 10) -> 7
        LocalTime.of(17, 5) -> 8
        LocalTime.of(18, 30) -> 9
        LocalTime.of(19, 25) -> 10
        LocalTime.of(20, 30) -> 11
        LocalTime.of(21, 25) -> 12
        else -> null
    }

    private fun parseRoomAndTeacher(
        location: String?,
        description: String?
    ): Pair<String, String> {
        val descriptionLines = description?.lines().orEmpty()
        val descriptionRoom = descriptionLines.getOrNull(1)?.trim().orEmpty()
        val descriptionTeacher = descriptionLines.getOrNull(2)?.trim().orEmpty()

        val (roomFromLocation, teacherFromLocation) = location?.let { loc ->
            if (!loc.contains(' ')) {
                loc.trim() to ""
            } else {
                val teacher = loc.substringAfterLast(' ').trim().trim('*')
                val room = loc.substringBeforeLast(' ').trim()
                room to teacher
            }
        } ?: ("" to "")

        val room = descriptionRoom.ifBlank { roomFromLocation }
        val teacher = descriptionTeacher.trim('*').ifBlank { teacherFromLocation }
        return room to teacher
    }

    private fun weeksBetween(startMonday: LocalDate, date: LocalDate): Int {
        val targetMonday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return ChronoUnit.WEEKS.between(startMonday, targetMonday).toInt()
    }

    private fun pickColor(name: String): Long {
        val index = name.hashCode().absoluteValue % colorPalette.size
        return colorPalette[index]
    }

    private data class WakeUpEvent(
        val summary: String,
        val location: String?,
        val description: String?,
        val start: LocalDateTime,
        val end: LocalDateTime,
        val untilDate: LocalDate?
    )
}
