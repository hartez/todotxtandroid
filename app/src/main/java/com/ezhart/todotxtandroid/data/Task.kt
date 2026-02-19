package com.ezhart.todotxtandroid.data

import java.time.LocalDate
import java.time.LocalDate.parse
import java.time.format.DateTimeParseException

data class Task(val task: String) {
    val body: String
    val priority: Char?
    val completedDate: LocalDate?
    val createdDate: LocalDate?
    val completed: Boolean

    val metadata: Map<String, String>
    val projects: Set<String>
    val contexts: Set<String>

    init {
        val matchResult = taskRegex.find(task)
        val groups = matchResult?.groups as MatchNamedGroupCollection

        body = groups[BODY]?.value ?: ""
        completedDate = tryParseDate(groups[COMPLETED_DATE]?.value)
        createdDate = tryParseDate(groups[CREATED_DATE]?.value)
        completed = groups[DONE] != null
        priority = groups[PRIORITY]?.value[0]

        metadata = mapOf()
        projects = setOf()
        contexts = setOf()
    }

    companion object {

        const val COMPLETED_DATE = "completedDate"
        const val CREATED_DATE = "createdDate"
        const val PRIORITY = "priority"
        const val BODY = "body"
        const val DONE = "done"

        private val taskRegex: Regex =
            """(?<$DONE>x (?<$COMPLETED_DATE>[0-9]{4}-[0-9]{2}-[0-9]{2}) )?(?:\((?<$PRIORITY>[A-Z])\) )?(?:(?<$CREATED_DATE>[0-9]{4}-[0-9]{2}-[0-9]{2}) )?(?<$BODY>.+)$""".toRegex()
        private val priorityRegex: Regex = """^\([A-Z]\) """.toRegex()

        fun parsePriority(task: String): Char? {
            val result = priorityRegex.find(task) ?: return null
            return result.value[1]
        }

        private fun tryParseDate(date: String?): LocalDate? {
            return when (date) {
                null -> null
                else -> try {
                    parse(date)
                } catch (e: DateTimeParseException) {
                    null
                }
            }
        }
    }
}
