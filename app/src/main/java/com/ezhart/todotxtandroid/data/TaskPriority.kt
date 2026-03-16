package com.ezhart.todotxtandroid.data

sealed class TaskPriority : Comparable<TaskPriority> {
    fun display(noneLabel: String = " "): String {
        return when (this) {
            is NoPriority -> noneLabel
            is Priority -> "${this.letter}"
        }
    }

    companion object {

        val options: List<TaskPriority> by lazy { options() }

        private fun options(): List<TaskPriority> {
            val list = mutableListOf<TaskPriority>()

            list.add(NoPriority)

            for (n in 65..90) {
                list.add(Priority(Char(n)))
            }

            return list
        }
    }
}

data object NoPriority : TaskPriority() {
    override fun compareTo(other: TaskPriority): Int {
        if(other is Priority){
            return 1
        }

        return 0
    }
}

data class Priority(val letter: Char) : TaskPriority() {
    override fun compareTo(other: TaskPriority): Int {
        return when(other){
            is NoPriority -> -1
            is Priority -> this.letter.compareTo(other.letter)
        }
    }

    init {
        require(letter.isLetter() && letter.isUpperCase())
    }
}

