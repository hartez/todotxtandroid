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

        taskPriority = when (val pri = groups[PRIORITY]?.value[0]) {
            is Char -> Priority(pri)
            else -> NoPriority
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
            val matches = projectsRegex.findAll(body)
            return matches.map { t -> t.value.trim() }.toSet()
        }

        fun parseContexts(body: String): Set<String> {
            val matches = contextsRegex.findAll(body)
            return matches.map { t -> t.value.trim() }.toSet()
        }

        fun editTags(task: String, tags: List<String>): String {
            var result = task

            val projects = parseProjects(task)
            val contexts = parseContexts(task)

            for (project in projects) {
                result = result.replace(" ${Regex.escape(project)}( |$)".toRegex(), " ")
            }

            for (context in contexts) {
                result = result.replace(" ${Regex.escape(context)}( |$)".toRegex(), " ")
            }

            return "${result.trim()} ${tags.joinToString(" ")}"
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

        fun editDueDate(task: String, dueDate: LocalDate): String {
            val currentDueDate = tryParseDate(parseMetadata(task)["due"])

            if (currentDueDate != null) {
                return task.replace("due:${currentDueDate}", "due:${dueDate}")
            }

            return "$task due:${dueDate}"
        }

        fun parsePriority(task: String): TaskPriority {
            val result = priorityRegex.find(task) ?: return NoPriority
            return Priority(result.value[1])
        }

        fun editPriority(task: String, newPriority: TaskPriority): String {

            val currentPriority = parsePriority(task)

            return if (currentPriority == NoPriority) {
                when (newPriority) {
                    NoPriority -> task
                    is Priority -> "(${newPriority.letter}) $task"
                }
            } else {
                when (newPriority) {
                    NoPriority -> task.substring(4)
                    is Priority -> "(${newPriority.letter}) ${task.substring(4)}"
                }
            }
        }

        fun insertCreatedDate(task: String, createdDate: LocalDate): String {

            val prospectiveTask = Task(task)
            if(prospectiveTask.createdDate != null){
                return task
            }

            if (prospectiveTask.completed) {
                return "${task.substring(0, 12)} $createdDate ${task.substring(13)}"
            }

            val priority = prospectiveTask.taskPriority

            return if (priority == NoPriority) {
                "$createdDate $task"
            } else {
                "(${priority.display()}) $createdDate ${task.substring(4)}"
            }
        }

        fun removeCreatedDate(task: String): String {
            val parsedTask = Task(task)

            if (parsedTask.createdDate == null) {
                return task
            }

            return if (parsedTask.completed) {
                task.substring(0, 13) + task.substring(24)
            } else if (parsedTask.taskPriority != NoPriority) {
                task.substring(0, 4) + task.substring(15)
            } else {
                task.substring(11)
            }
        }

        fun markCompleted(task: String, completedDate: LocalDate): String {
            if(task.startsWith("x ")){
                return task
            }

            val priority = Task.parsePriority(task)

            if(priority == NoPriority) {
                return "x $completedDate $task"
            }

            return "x $completedDate ${task.substring(4)}"
        }

        fun markPending(task: String): String {
            if(task.startsWith("x ")){
                return task.substring(13)
            }

            return task
        }
    }
}
