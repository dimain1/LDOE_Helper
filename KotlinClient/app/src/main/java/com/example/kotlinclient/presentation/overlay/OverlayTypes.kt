package com.example.kotlinclient.presentation.overlay

/** Боковой край экрана, к которому может примагничиваться кнопка.
 *  Верхний и нижний края намеренно исключены — там кнопка застревала
 *  за системными панелями и становилась недосягаемой. */
internal enum class HiddenEdge { Left, Right }

/** Активный пикер / модальное окно в форме создания события. */
internal enum class OverlayCreateModal {
    Template,
    StartDateTime, StartDate, StartTime,
    EndDateTime,   EndDate,   EndTime
}

internal data class ButtonSize(val width: Int, val height: Int)

internal data class OverlayBounds(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
)
