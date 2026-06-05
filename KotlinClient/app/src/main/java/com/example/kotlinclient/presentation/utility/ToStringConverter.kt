package com.example.kotlinclient.presentation.utility

fun getStringByMinutes(minutes: Long): String{
    return if(minutes % 10L == 1L && minutes != 11L){
        "Минута"
    }
    else if(minutes % 10L in listOf(2L,3L,4L) && minutes !in listOf(12L,13L,14L)){
        "Минуты"
    }
    else "Минут"
}

fun getStringByHours(hours: Long): String{
    return if(hours % 10L == 1L && hours != 11L){
        "Час"
    }
    else if(hours % 10L in listOf(2L,3L,4L) && hours !in listOf(12L,13L,14L)){
        "Часа"
    }
    else "Часов"
}

fun getStringByDays(days: Long): String{
    return if(days % 10L == 1L && days != 11L){
        "День"
    }
    else if(days % 10L in listOf(2L,3L,4L) && days !in listOf(12L,13L,14L)){
        "Дня"
    }
    else "Дней"
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
