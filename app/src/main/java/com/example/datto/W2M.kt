package com.example.datto

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
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
import java.net.URL
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

    var voting = false

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
            requireContext(),
            android.R.layout.simple_spinner_item,
            schedules.map { it.id })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val spinner2: Spinner = view.findViewById(R.id.spinner2)

        val calendarView = view.findViewById<CalendarView>(R.id.overallCalendarView)

        val voteCalendarView = view.findViewById<CalendarView>(R.id.voteCalendarView)

        val ctx = requireContext()
        val rangeStartBackground =
            AppCompatResources.getDrawable(ctx, R.drawable.w2m_continuous_selected_bg_start).also {
                    if (it != null) {
                        it.level = 5000
                    }
                }

        val rangeEndBackground =
            AppCompatResources.getDrawable(ctx, R.drawable.w2m_continuous_selected_bg_end).also {
                    if (it != null) {
                        it.level = 5000
                    }
                }
        val rangeMiddleBackground =
            AppCompatResources.getDrawable(ctx, R.drawable.w2m_continuous_selected_bg_middle)
        val singleBackground =
            AppCompatResources.getDrawable(ctx, R.drawable.w2m_single_selected_bg)
        val todayBackground = AppCompatResources.getDrawable(ctx, R.drawable.w2m_today_bg)

        val today = LocalDate.now()

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {

                val adapter = ArrayAdapter(requireContext(),
                                           android.R.layout.simple_spinner_item,
                                           schedules[position].availability.map { it.person })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner2.adapter = adapter

                spinner2.setSelection(0)

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
            val roundBgView = view.findViewById<View>(R.id.roundBackgroundView)
            val continuousBgView = view.findViewById<View>(R.id.continuousBackgroundView)
            lateinit var day: CalendarDay
            var onClickListener: ((LocalDate) -> Unit)? = null

            init {
                view.setOnClickListener {
                    onClickListener?.invoke(day.date)
                }
            }
        }

        val availableRecyclerView = view.findViewById<RecyclerView>(R.id.availableW2MRecyclerView)
        availableRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(view.context)
        availableRecyclerView.adapter = MemberListAdapter(ArrayList())
        val unavailableRecyclerView = view.findViewById<RecyclerView>(R.id.unavailableW2MRecyclerView)
        unavailableRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(view.context)
        unavailableRecyclerView.adapter = MemberListAdapter(ArrayList())

        val scrollView = requireActivity().findViewById<View>(R.id.scrollView)


        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                return DayViewContainer(view).apply {
                    textView.setOnClickListener { // val date = day.date
                        val availability = event?.availability?.find { it.person == userId }
                        if (availability != null) { // list all the users that are available on that day

                            class MemberItem(val name: String, val image: URL)
                            class MemberListAdapter(private val members: ArrayList<MemberItem>) :
                                RecyclerView.Adapter<MemberListAdapter.MemberViewHolder>() {

                                inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                                    val memberName: TextView = itemView.findViewById(R.id.memberNameTextView)
                                    val memberImage: ImageView = itemView.findViewById(R.id.memberCoverImageView)
                                    val removeButton: ImageButton = itemView.findViewById(R.id.memberRemoveButton)
                                    init {
                                        itemView.setOnClickListener {
                                            Toast.makeText(itemView.context, itemView.toString(), Toast.LENGTH_SHORT).show()
                                        }

                                        removeButton.visibility = View.GONE
                                    }
                                }

                                override fun onCreateViewHolder(
                                    parent: ViewGroup, viewType: Int
                                ): MemberListAdapter.MemberViewHolder {
                                    val itemView =
                                        LayoutInflater.from(parent.context).inflate(R.layout.group_details_members_list_items, parent, false)
                                    return MemberViewHolder(itemView)
                                }

                                override fun onBindViewHolder(holder: MemberListAdapter.MemberViewHolder, position: Int) {
                                    val currentItem = members[position]
                                    holder.memberName.text = currentItem.name

                                    val thread = Thread {
                                        try {
                                            val bitmap =
                                                BitmapFactory.decodeStream(currentItem.image.openConnection().getInputStream())
                                            holder.memberImage.post {
                                                holder.memberImage.setImageBitmap(bitmap)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    thread.start()
                                }

                                override fun getItemCount() = members.size
                            }

                            val availableUsers = ArrayList<MemberItem>()
                            val unavailableUsers = ArrayList<MemberItem>()

                            event?.availability?.forEach {
                                if (it.availability.contains(day.date)) {
                                    availableUsers.add(MemberItem("User ${it.person}", URL("https://via.assets.so/img.jpg?w=500&h=500&tc=&bg=#7f7f7f&t=Hello%20World")))
                                } else {
                                    unavailableUsers.add(MemberItem("User ${it.person}", URL("https://via.assets.so/img.jpg?w=500&h=500&tc=&bg=#7f7f7f&t=Hello%20World")))
                                }
                            }

                            val availableAdapter = MemberListAdapter(availableUsers)
                            val unavailableAdapter = MemberListAdapter(unavailableUsers)

                            availableRecyclerView.adapter = availableAdapter
                            unavailableRecyclerView.adapter = unavailableAdapter

                            Toast.makeText(requireContext(), "Available: ${availableUsers.size}, Unavailable: ${unavailableUsers.size}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "No availability", Toast.LENGTH_SHORT)
                                .show()
                        }
                        scrollView.visibility = View.VISIBLE
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

                    val maxAvailable = event?.availability?.size ?: 0
                    val minAvailable = 0
                    val current =
                        event?.availability?.count { it.availability.contains(data.date) } ?: 0

                    var red = Math.round(255 / 2.0)
                    var green = Math.round(255 / 2.0)
                    var blue = Math.round(255 / 2.0)

                    if (maxAvailable != minAvailable) {
                        red =
                            Math.round(((204.0) * (maxAvailable - current) / (maxAvailable - minAvailable)) + 51.0)
                        green =
                            Math.round(((102.0) * (maxAvailable - current) / (maxAvailable - minAvailable)) + 153.0)
                        blue =
                            Math.round(((255.0) * (maxAvailable - current) / (maxAvailable - minAvailable)) + 0.0)
                    }

                    val singleColour = android.graphics.Color.argb(
                        255, red.toInt(), green.toInt(), blue.toInt()
                    )

                    val middleBackgroundColour = android.graphics.Color.argb(
                        255,
                        Math.round(((204.0 * (maxAvailable - 1) / (maxAvailable - minAvailable)) + 51.0))
                            .toInt(),
                        Math.round(((102.0 * (maxAvailable - 1) / (maxAvailable - minAvailable)) + 153.0))
                            .toInt(),
                        Math.round(((255.0 * (maxAvailable - 1) / (maxAvailable - minAvailable)) + 0.0))
                            .toInt()
                    )

                    // fuck me, eh?
                    if (event?.availability?.find { it.availability.contains(data.date) } != null && event?.availability?.find {
                            it.availability.contains(
                                data.date.plusDays(1)
                            )
                        } != null && event?.availability?.find {
                            it.availability.contains(
                                data.date.minusDays(
                                    1
                                )
                            )
                        } != null) {
                        container.continuousBgView.visibility = View.VISIBLE
                        container.continuousBgView.setBackgroundResource(R.drawable.w2m_continuous_selected_bg_middle)
                        container.continuousBgView.background.setTint(middleBackgroundColour)
                    } else if (event?.availability?.find { it.availability.contains(data.date) } != null && event?.availability?.find {
                            it.availability.contains(
                                data.date.plusDays(1)
                            )
                        } != null) {
                        container.continuousBgView.visibility = View.VISIBLE
                        container.continuousBgView.background = (AppCompatResources.getDrawable(
                            ctx, R.drawable.w2m_continuous_selected_bg_start
                        )?.apply { level = 5000 })
                        container.continuousBgView.background.setTint(middleBackgroundColour)
                    } else if (event?.availability?.find { it.availability.contains(data.date) } != null && event?.availability?.find {
                            it.availability.contains(
                                data.date.minusDays(1)
                            )
                        } != null) {
                        container.continuousBgView.visibility = View.VISIBLE
                        container.continuousBgView.background = (AppCompatResources.getDrawable(
                            ctx, R.drawable.w2m_continuous_selected_bg_end
                        )?.apply { level = 5000 })
                        container.continuousBgView.background.setTint(middleBackgroundColour)
                    } else if (event?.availability?.find { it.availability.contains(data.date) } != null) {
                        container.continuousBgView.visibility = View.INVISIBLE
                    } else {
                        container.roundBgView.visibility = View.INVISIBLE
                        container.continuousBgView.visibility = View.INVISIBLE
                    }

                    container.roundBgView.visibility = View.VISIBLE
                    container.roundBgView.setBackgroundResource(R.drawable.w2m_single_selected_bg)
                    container.roundBgView.background.setTint(singleColour)

                } else {
                    container.continuousBgView.visibility = View.INVISIBLE
                    container.roundBgView.visibility = View.INVISIBLE
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
                d.onClickListener = { date ->

                    // Check the day position as we do not want to select in or out dates.
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
                    voteCalendarView.notifyCalendarChanged()
                }
                return d
            }

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: CalendarDay) {

                container.continuousBgView.background?.setTintList(null)
                container.roundBgView.background?.setTintList(null)

                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()
                if (data.position == DayPosition.MonthDate) { // Show the month dates. Remember that views are reused!
                    container.textView.visibility = View.VISIBLE
                } else { // Hide in and out dates
                    container.textView.visibility = View.INVISIBLE
                }

                if (selectedDate.containsKey(data.date) && selectedDate.containsKey(
                        data.date.plusDays(
                            1
                        )
                    ) && selectedDate.containsKey(data.date.minusDays(1))
                ) {
                    container.continuousBgView.visibility = View.VISIBLE
                    if (rangeMiddleBackground != null) {
                        container.continuousBgView.applyBackground(rangeMiddleBackground)
                    }
                    container.roundBgView.visibility = View.INVISIBLE

                } else if (selectedDate.containsKey(data.date) && selectedDate.containsKey(
                        data.date.plusDays(
                            1
                        )
                    )
                ) {
                    container.continuousBgView.visibility = View.VISIBLE
                    if (rangeStartBackground != null) {
                        container.continuousBgView.applyBackground(rangeStartBackground)
                    }
                    container.roundBgView.visibility = View.VISIBLE
                    if (singleBackground != null) {
                        container.roundBgView.applyBackground(singleBackground)
                    }

                } else if (selectedDate.containsKey(data.date) && selectedDate.containsKey(
                        data.date.minusDays(
                            1
                        )
                    )
                ) {
                    container.continuousBgView.visibility = View.VISIBLE
                    if (rangeEndBackground != null) {
                        container.continuousBgView.applyBackground(rangeEndBackground)
                    }
                    container.roundBgView.visibility = View.VISIBLE
                    if (singleBackground != null) {
                        container.roundBgView.applyBackground(singleBackground)
                    }

                } else if (selectedDate.containsKey(data.date)) {
                    container.roundBgView.visibility = View.VISIBLE
                    if (singleBackground != null) {
                        container.roundBgView.applyBackground(singleBackground)
                    }
                    container.continuousBgView.visibility = View.INVISIBLE
                } else {
                    container.roundBgView.visibility = View.INVISIBLE
                    container.continuousBgView.visibility = View.INVISIBLE
                }
            }

            private fun View.applyBackground(drawable: Drawable) {
                visibility = View.VISIBLE
                background = drawable
            }

        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
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

        configTopAppBar()

    }

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        val scrollView = requireActivity().findViewById<View>(R.id.scrollView)
        val voteCalendarView = requireActivity().findViewById<CalendarView>(R.id.voteCalendarView)
        menuItem.isEnabled = true
        if (voting) {
            menuItem.setIcon(null)
            menuItem.title = "Save"
            menuItem.setOnMenuItemClickListener { // save the availability
                scrollView.visibility = View.GONE
                voteCalendarView.visibility = View.GONE
                voting = false
                appBar.title = "VIEW"
                configTopAppBar()
                true
            }
        } else {
            menuItem.setIcon(R.drawable.ic_edit)
            menuItem.setOnMenuItemClickListener {
                scrollView.visibility = View.GONE
                voteCalendarView.visibility = View.VISIBLE
                voting = true
                appBar.title = "VOTE"
                configTopAppBar()
                true
            }
        }
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