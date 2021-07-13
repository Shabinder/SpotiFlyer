package common

import java.util.*

fun getTodayDate(): String {
    val c: Calendar = Calendar.getInstance()
    val monthName = arrayOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November",
        "December"
    )
    val month = monthName[c.get(Calendar.MONTH)]
    val year: Int = c.get(Calendar.YEAR)
    val date: Int = c.get(Calendar.DATE)
    return " $date $month, $year"
}
