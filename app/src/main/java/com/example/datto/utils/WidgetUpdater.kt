package com.example.datto.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.datto.EventWidget

class WidgetUpdater {
    fun update(context: Context) {
        // Update widget
        val widgetUpdateIntent = Intent(context, EventWidget::class.java)
        widgetUpdateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, EventWidget::class.java)
        )
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        context.sendBroadcast(widgetUpdateIntent)
    }
}