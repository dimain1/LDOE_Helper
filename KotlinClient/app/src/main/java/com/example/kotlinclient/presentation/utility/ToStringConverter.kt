package com.example.kotlinclient.presentation.utility

fun getStringByMinutes(minutes: Long): String{
    if(minutes % 10L == 1L && minutes != 11L){
        return "Минута"
    }
    else if(minutes % 10L in listOf(2L,3L,4L) && minutes !in listOf(12L,13L,14L)){
        return "Минуты"
    }
    else return "Минут"
}

fun getStringByHours(hours: Long): String{
    if(hours % 10L == 1L && hours != 11L){
        return "Час"
    }
    else if(hours % 10L in listOf(2L,3L,4L) && hours !in listOf(12L,13L,14L)){
        return "Часа"
    }
    else return "Часов"
}

fun getStringByDays(days: Long): String{
    if(days % 10L == 1L && days != 11L){
        return "День"
    }
    else if(days % 10L in listOf(2L,3L,4L) && days !in listOf(12L,13L,14L)){
        return "Дня"
    }
    else return "Дней"
}

fun getStringTimeByDuration(duration: Long): String{
    val days = duration / 86_400_000
    val hours = (duration % 86_400_000) / 3_600_000
    val minutes = (duration % 3_600_000) / 60_000
    val seconds = (duration % 60_000) / 1_000
    return String.format(
        "%s %s %s",
        if (days != 0L) {
            "$days ${getStringByDays(days) }"
        } else {
            ""
        },
        if (hours != 0L) {
            "$hours ${getStringByHours(hours) }"
        } else {
            ""
        },
        if (minutes != 0L) {
            "$minutes ${getStringByMinutes(minutes) }"
        } else {
            ""
        }
    ).trim()
}
