package com.ezhart.todotxtandroid.data

sealed class TaskPriority{
    fun display():String{
        return when (this) {
            None -> " "
            is Priority -> "${this.letter}"
        }
    }

    companion object {

        val options : List<TaskPriority> = options()

        private fun options():List<TaskPriority>{
            val list = mutableListOf<TaskPriority>()

            list.add(None)

            for(n in 65..90){
                list.add(Priority(Char(n)))
            }

            return list
        }
    }
}

data object None : TaskPriority()
data class Priority(val letter: Char) : TaskPriority() {
    init{
        require(letter.isLetter() && letter.isUpperCase())
    }
}

