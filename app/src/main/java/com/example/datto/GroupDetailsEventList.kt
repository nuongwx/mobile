package com.example.datto

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.EventResponse
import java.text.SimpleDateFormat
import java.util.TimeZone

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupDetailsEventList.newInstance] factory method to
 * create an instance of this fragment.
 */

class EventListAdapter(private val events: ArrayList<EventResponse>) :
    RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventNameTextView)
        val eventDes: TextView = itemView.findViewById(R.id.eventDescriptionTextView)
        val date: TextView = itemView.findViewById(R.id.eventDateTextView)
        val month: TextView = itemView.findViewById(R.id.eventMonthTextView)

        var setOnClickListener: (View) -> Unit = {}

        init {
            itemView.setOnClickListener {
                setOnClickListener(itemView)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): EventListAdapter.EventViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.group_details_events_list_items, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventListAdapter.EventViewHolder, position: Int) {
        val currentItem = events[position]
        holder.eventName.text = currentItem.name
        if (currentItem.description !== null) {
            holder.eventDes.text = currentItem.description
        }

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(currentItem.time.start)

        holder.date.text = SimpleDateFormat("dd", java.util.Locale.getDefault()).format(date)
        holder.month.text = SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(date)

        holder.setOnClickListener = {
            // switch to event details fragment
            val bundle = Bundle()
            bundle.putString("eventId", currentItem.id)
            val eventDetails = EventDetails()
            eventDetails.arguments = bundle
            val transaction = (it.context as MainActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.app_fragment, eventDetails)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun getItemCount() = events.size
}


class GroupDetailsEventList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.isVisible = true
        menuItem.setIcon(R.drawable.ic_add)
//        menuItem.setOnMenuItemClickListener {
//            parentFragmentManager.beginTransaction().replace(R.id.app_fragment, NewEvent())
//                .addToBackStack(null)
//                .commit()
//            true
//        }
        appBar.title = "Events"
    }

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
        return inflater.inflate(R.layout.fragment_group_details_event_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configTopAppBar()

        val eventIds = arguments?.getStringArrayList("eventIds")

        val eventList = ArrayList<EventResponse>()
        if (eventIds !== null && eventIds.isNotEmpty()) {
            var completedRequests = 0

            for (eventId in eventIds) {
                APIService().doGet<EventResponse>("events/${eventId}", object : APICallback<Any> {
                    override fun onSuccess(data: Any) {
                        Log.d("API_SERVICE", "Data: $data")

                        data as EventResponse

                        eventList.add(data)

                        completedRequests++ // Increment the count of completed requests

                        // Check if all requests have completed
                        if (completedRequests == eventIds.size) {
                            val eventRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                            eventRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(view.context)
                            eventRecyclerView.adapter = EventListAdapter(eventList)
                            eventRecyclerView.setHasFixedSize(true)
                        }
                    }

                    override fun onError(error: Throwable) {
                        Log.e("API_SERVICE", "Error: ${error.message}")
                    }
                })
            }
        } else {
            val textView = view.findViewById<TextView>(R.id.noEventsTextView)
            textView.isVisible = true
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupDetailsEventList.
         */ // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = GroupDetailsEventList().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}