package com.gabriel.cal.ui.calendar

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.gabriel.cal.R

class SelectedDatesDecorator(private val context: Context, private val dates: Set<CalendarDay>) : DayViewDecorator {

    // Drawable para el fondo resaltado
    private val drawable = ContextCompat.getDrawable(context, R.drawable.calendar_selected_background)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        // Aplica el drawable para cambiar el fondo
        drawable?.let { view.setBackgroundDrawable(it) }
        // Agrega un span para cambiar el color del texto a blanco
        view.addSpan(ForegroundColorSpan(Color.WHITE))
    }
}
