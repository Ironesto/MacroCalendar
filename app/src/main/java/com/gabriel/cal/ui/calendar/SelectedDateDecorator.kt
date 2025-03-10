package com.gabriel.cal.ui.calendar

import android.content.Context
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.gabriel.cal.R

class SelectedDateDecorator(private val context: Context, private val selectedDay: CalendarDay) : DayViewDecorator {
    // Usaremos un drawable de fondo para resaltar la fecha
    private val drawable = ContextCompat.getDrawable(context, R.drawable.calendar_selected_background)

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day == selectedDay
    }

    override fun decorate(view: DayViewFacade) {
        // Aplica el drawable de fondo a la celda del día seleccionado
        view.setBackgroundDrawable(drawable!!)
    }
}
