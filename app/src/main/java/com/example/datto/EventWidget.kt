package com.example.datto

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.icu.text.AlphabeticIndex.ImmutableIndex
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.Toast
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.AccountResponse
import com.example.datto.DataClass.EventResponse
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Implementation of App Widget functionality.
 */
class EventWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        Toast.makeText(context, "Widget Updated", Toast.LENGTH_SHORT).show()
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.event_widget)

    APIService().doGet<List<CustomGroupResponse>>(
        "accounts/${CredentialService().get()}/groups",
        object : APICallback<Any> {
            override fun onSuccess(data: Any) {
                Log.d("API_SERVICE", "Data: $data")
                Log.d("WIDGET", "AppWidgetId: $data")

                data as List<CustomGroupResponse>

                val events = mutableListOf<EventResponse>()

                for (group in data) {
                    events.addAll(group.events)
                }

                // Get upcoming event
                val upComingEvent = events.filter {
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    parser.timeZone = TimeZone.getTimeZone("UTC")

                    val formattedDateEnd = parser.parse(it.time.end)

                    // Create a Calendar instance and set it to the parsed date
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.time = formattedDateEnd!!

                    // Set the time to the end of the day
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)

                    val endOfDayTimestamp = calendar.time.time

                    endOfDayTimestamp > System.currentTimeMillis()
                }

                // Get lastest upcoming event
                val nearestUpcomingEvent = upComingEvent.minByOrNull { it.time.start }

                Log.d("WIDGET", "Nearest Upcoming Event: $nearestUpcomingEvent")

                // Check if there is no upcoming event
                if (nearestUpcomingEvent == null) {
                    // Update visibility
                    views.setViewVisibility(R.id.widget_group_name, View.INVISIBLE)
                    views.setViewVisibility(R.id.widget_event_name, View.INVISIBLE)
                    views.setViewVisibility(R.id.widget_date, View.INVISIBLE)
                    views.setViewVisibility(R.id.widget_warning, View.VISIBLE)

                    // Instruct the widget manager to update the widget
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    return
                }

                // Otherwise, continue
                // Get group name
                var groupName = ""

                for (group in data) {
                    if (group.events.contains(nearestUpcomingEvent)) {
                        groupName = group.name
                        break
                    }
                }

                // Format the date
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)

                // Format the date
                val formattedStartDate = formatter.format(parser.parse(nearestUpcomingEvent?.time?.start!!)!!)
                val formattedEndDate = formatter.format(parser.parse(nearestUpcomingEvent?.time?.end!!)!!)

                // Set the text
                views.setTextViewText(R.id.widget_group_name, groupName)
                views.setTextViewText(R.id.widget_event_name, nearestUpcomingEvent?.name)
                views.setTextViewText(R.id.widget_date, "$formattedStartDate - $formattedEndDate")

                // Update visibility
                views.setViewVisibility(R.id.widget_group_name, View.VISIBLE)
                views.setViewVisibility(R.id.widget_event_name, View.VISIBLE)
                views.setViewVisibility(R.id.widget_date, View.VISIBLE)
                views.setViewVisibility(R.id.widget_warning, View.INVISIBLE)

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }

            override fun onError(error: Throwable) {
                Log.e("WIDGET", "Error: ${error.message}")

                // Update visibility
                views.setViewVisibility(R.id.widget_group_name, View.INVISIBLE)
                views.setViewVisibility(R.id.widget_event_name, View.INVISIBLE)
                views.setViewVisibility(R.id.widget_date, View.INVISIBLE)
                views.setViewVisibility(R.id.widget_warning, View.VISIBLE)

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        })
}