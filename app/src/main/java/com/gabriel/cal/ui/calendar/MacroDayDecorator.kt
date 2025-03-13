package com.gabriel.cal.ui.calendar

import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.gabriel.cal.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class MacroDayDecorator(
    private val context: android.content.Context,
    private val days: Set<CalendarDay>,
    private val color: Int
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return days.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        // Usa el context que se pasó en el constructor
        val baseDrawable = ContextCompat.getDrawable(context, R.drawable.calendar_selected_background)
        val mutableDrawable = baseDrawable?.mutate() as? GradientDrawable
        mutableDrawable?.setColor(color)
        mutableDrawable?.let { view.setSelectionDrawable(it) }
    }
}
