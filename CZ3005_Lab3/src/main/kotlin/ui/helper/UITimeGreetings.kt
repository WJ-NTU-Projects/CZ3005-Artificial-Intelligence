package ui.helper

import java.time.LocalDateTime

class UITimeGreetings {
    companion object {
        fun getGreetings(): String {
            val timeNow: LocalDateTime = LocalDateTime.now()
            val greetings: String = when {
                timeNow.hour < 12 -> "Good morning!"
                timeNow.hour < 19 -> "Good afternoon!"
                else              -> "Good evening!"
            }

            return greetings
        }
    }
}