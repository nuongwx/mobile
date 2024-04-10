package com.example.datto

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.YearMonth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [W2M.newInstance] factory method to
 * create an instance of this fragment.
 */
class W2M : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_w2m, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        data class Availability(
            val person: Int,
            var availability: List<LocalDate> // my condolences to the poor naming scheme
        )

        data class Event(
            val id: Int, val name: String, val availability: List<Availability>
        )

        var selectedDate = HashMap<LocalDate, Boolean>()

        class LocalDateDeserializer : JsonDeserializer<LocalDate> {
            override fun deserialize(
                json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext
            ): LocalDate {
                return LocalDate.parse(json.asString)
            }
        }

        class LocalDateSerializer : JsonSerializer<LocalDate> {
            override fun serialize(
                src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?
            ): JsonElement {
                return JsonPrimitive(src.toString())
            }
        }

        val json =
            "[{\"availability\":[{\"availability\":[\"2024-04-10\",\"2024-04-11\"],\"person\":1},{\"availability\":[\"2024-04-12\"],\"person\":2}],\"id\":1,\"name\":\"Event A\"},{\"availability\":[{\"availability\":[\"2024-04-12\",\"2024-04-13\"],\"person\":2},{\"availability\":[\"2024-04-14\",\"2024-04-19\"],\"person\":3}],\"id\":2,\"name\":\"Event B\"}]\n"

        val schedules = json.let {
            com.google.gson.GsonBuilder()
                .registerTypeAdapter(LocalDate::class.java, LocalDateDeserializer()).create()
                .fromJson(it, Array<Event>::class.java)
        }.toMutableList()

        var userId = 1

        var event: Event? = schedules.first { true }

        val spinner: Spinner = view.findViewById(R.id.spinner)
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, schedules.map { it.id }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val spinner2: Spinner = view.findViewById(R.id.spinner2)

        val calendarView = view.findViewById<CalendarView>(R.id.overallCalendarView)

        val voteCalendarView = view.findViewById<CalendarView>(R.id.voteCalendarView)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                Toast.makeText(
                    requireContext(), "Selected: ${schedules[position].name}", Toast.LENGTH_SHORT
                ).show()
                val adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_item,
                    schedules[position].availability.map { it.person })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner2.adapter = adapter

                spinner2.setSelection(0)

                // eventId = schedules[position].id

                event = schedules.find { it.id == spinner.selectedItem.toString().toInt() }!!

                calendarView.notifyCalendarChanged()
                voteCalendarView.notifyCalendarChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) { // get current person availability
                // val event = schedules.find { it.id == spinner.selectedItem.toString().toInt() }
                selectedDate = HashMap()
                val availability = event?.availability?.find {
                    it.person == spinner2.selectedItem.toString().toInt()
                }
                availability?.availability?.forEach {
                    selectedDate[it] = true
                }

                userId = spinner2.selectedItem.toString().toInt()

                calendarView.notifyCalendarChanged()
                voteCalendarView.notifyCalendarChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.dayText)
            var selectedDate: LocalDate? = null

            lateinit var day: CalendarDay
            var onClickListener: ((LocalDate) -> Unit)? = null

            init {
                view.setOnClickListener {
                    onClickListener?.invoke(day.date)
                }
            }
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                return DayViewContainer(view).apply {
                    textView.setOnClickListener {
                        // val date = day.date
                        val availability = event?.availability?.find { it.person == userId }
                        if (availability != null) {
                            Log.d("W2M", "${event?.availability?.map { it.person }}")
                            Toast.makeText(
                                requireContext(), event?.availability?.map { it.person }.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.d("W2m", "No availability")
                            Toast.makeText(requireContext(), "No availability", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()
                if (data.position != DayPosition.MonthDate) {
                    container.textView.visibility = View.INVISIBLE
                    return
                } else {
                    container.textView.visibility = View.VISIBLE
                }
                val availability = event?.availability?.find { it.availability.contains(data.date) }
                if (availability != null) {
                    container.textView.setBackgroundResource(R.drawable.ic_launcher_background) // alpha is used to show the availability of the group, the more people are available the more opaque the background
                    container.textView.alpha =
                        event?.availability?.count { it.availability.contains(data.date) }
                            ?.toFloat()!! / event?.availability?.size!!
                        // availability.availability.count { it.availability.contains(day.date) }
                        //     .toFloat() / availability.availability.size
                } else {
                    container.textView.background = null
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val monthHeader = view.findViewById<TextView>(R.id.headerText)
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View): MonthViewContainer = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                container.monthHeader.text = data.yearMonth.month.name
            }
        }


        voteCalendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View): DayViewContainer {
                val d = DayViewContainer(view)
                d.onClickListener =
                    { date -> // Check the day position as we do not want to select in or out dates.
                        if (d.day.position == DayPosition.MonthDate) { // Keep a reference to any previous selection
                            // in case we overwrite it and need to reload it.
                            if (selectedDate.containsKey(date)) { // If the user clicks the same date, clear selection.
                                selectedDate.remove(date) // Reload this date so the dayBinder is called
                                // and we can REMOVE the selection background.
                                voteCalendarView.notifyDateChanged(date)
                            } else {
                                selectedDate[date] =
                                    true // Reload the newly selected date so the dayBinder is
                                // called and we can ADD the selection background.
                                voteCalendarView.notifyDateChanged(d.day.date)
                                if (selectedDate.size > 1) { // We need to also reload the previously selected
                                    // date so we can REMOVE the selection background.
                                    voteCalendarView.notifyDateChanged(selectedDate.keys.first { it != date })
                                }
                            }
                        }

                        // update the schedule availability
                        val schedule = event
                        val person = schedule?.availability?.find { it.person == userId }
                        if (person != null) {
                            person.availability = selectedDate.keys.toList()
                        }
                        calendarView.notifyCalendarChanged()
                    }
                return d
            }

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()
                if (data.position == DayPosition.MonthDate) { // Show the month dates. Remember that views are reused!
                    textView.visibility = View.VISIBLE
                    if (selectedDate.containsKey(data.date)) { // If this is the selected date, show a round background and change the text color.
                        // textView.setTextColor(Color.WHITE)
                        textView.setBackgroundResource(R.drawable.ic_launcher_background)
                    } else { // If this is NOT the selected date, remove the background and reset the text color.
                        // textView.setTextColor(Color.BLACK)
                        textView.background = null
                    }
                } else { // Hide in and out dates
                    textView.visibility = View.INVISIBLE
                }
            }

        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)  // Adjust as needed
        val endMonth = currentMonth.plusMonths(12)  // Adjust as needed
        val daysOfWeek = daysOfWeek()

        val legendLayout = view.findViewById<ViewGroup>(R.id.legendLayout)
        legendLayout.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].name.first().toString()
            }
        }

        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        voteCalendarView.setup(startMonth, endMonth, daysOfWeek.first())
        voteCalendarView.scrollToMonth(currentMonth)


        calendarView.notifyCalendarChanged()
        voteCalendarView.notifyCalendarChanged()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment w2m.
         */ // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = W2M().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}