package com.gabriel.cal.ui.calendar

import android.graphics.Color
import android.text.style.BackgroundColorSpan
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay

class MacroColorDecorator(
    private val days: Set<CalendarDay>,
    private val color: Int
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return days.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        // Añade un span que pinte el fondo con el color deseado.
        view.addSpan(BackgroundColorSpan(color))
    }
}

