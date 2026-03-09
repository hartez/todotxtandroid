package com.ezhart.todotxtandroid.data

import java.time.LocalDate
import java.time.LocalDate.parse
import java.time.format.DateTimeParseException

data class Task(val task: String) {
    val body: String
    val taskPriority: TaskPriority
    val completedDate: LocalDate?
    val createdDate: LocalDate?
    val dueDate: LocalDate?
    val completed: Boolean

    val metadata: Map<String, String>
    val projects: Set<String>
    val contexts: Set<String>

    init {
        val matchResult = taskRegex.find(task)
        val groups = matchResult?.groups as MatchNamedGroupCollection

        var taskBody = groups[BODY]?.value ?: ""
        completedDate = tryParseDate(groups[COMPLETED_DATE]?.value)
        createdDate = tryParseDate(groups[CREATED_DATE]?.value)
        completed = groups[DONE] != null

        taskPriority = when(val pri = groups[PRIORITY]?.value[0]) {
            is Char -> Priority(pri)
            else -> None
        }

        metadata = parseMetadata(taskBody)
        dueDate = tryParseDate(metadata["due"])
        projects = parseProjects(taskBody)
        contexts = parseContexts(taskBody)

        // Make sure to strip due:date metadata out of the displayed task body
        body = when (metadata["due"]) {
            null -> taskBody
            else -> taskBody.replace("due:" + metadata["due"], "").trim()
        }
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

        private val metadataRegex: Regex = """(?:^|\s)\w+:\S+\S*""".toRegex()
        private val projectsRegex: Regex = """(?:^|\s)\+\S*\w""".toRegex()
        private val contextsRegex: Regex = """(?:^|\s)@\S*\w""".toRegex()

        private fun parseMetadata(body: String): Map<String, String> {
            val matches = metadataRegex.findAll(body) ?: return mapOf()
            return matches.associate { match -> splitMetadata(match.value.trim()) }
        }

        private fun splitMetadata(pair: String): Pair<String, String> {
            val colonIndex = pair.indexOf(':')
            val key = pair.substring(0, colonIndex)
            val value = pair.substring(colonIndex + 1, pair.length)
            return Pair(key, value)
        }

        fun parseProjects(body: String): Set<String> {
            val matches = projectsRegex.findAll(body) ?: return setOf()
            return matches.map { t -> t.value.trim() }.toSet()
        }

        fun parseContexts(body: String): Set<String> {
            val matches = contextsRegex.findAll(body) ?: return setOf()
            return matches.map { t -> t.value.trim() }.toSet()
        }

        fun editTags(body:String, vararg tags:String) : String{
            var result = body

            val projects = parseProjects(body)
            val contexts = parseContexts(body)

            for(project in projects){
                result = result.replace(project, "")
            }

            for(context in contexts){
                result = result.replace(context, "")
            }

            return "$result ${tags.joinToString(" ")}"
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

        fun editDueDate(body: String, dueDate: LocalDate ) : String{
            val currentDueDate = tryParseDate(parseMetadata(body)["due"])

            if(currentDueDate != null){
                return body.replace("due:${currentDueDate}", "due:${dueDate}")
            }

            return "$body due:${dueDate}"
        }

        fun parsePriority(task: String): TaskPriority {
            val result = priorityRegex.find(task) ?: return None
            return Priority(result.value[1])
        }

        fun editPriority(body: String, taskPriority: TaskPriority): String {

            val currentPriority = parsePriority(body)

            if(taskPriority == currentPriority){
                return body
            }

            if(currentPriority is Priority && taskPriority is None){
                return body.substring(4)
            }

            if(currentPriority is None && taskPriority is Priority){
                return "(${taskPriority.letter}) $body"
            }

            if(taskPriority is Priority){
                return "(${taskPriority.letter}) ${body.substring(4)}"
            }

            return body
        }
    }
}
