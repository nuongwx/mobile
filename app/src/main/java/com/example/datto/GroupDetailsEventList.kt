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

private const val ARG_PARAM1 = "groupId"
private const val ARG_PARAM2 = "groupName"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupDetailsEventList.newInstance] factory method to
 * create an instance of this fragment.
 */

class GroupDetailsEventList : Fragment() {
    private var groupId: String? = null
    private var groupName: String? = null

    private fun configTopAppBar() {
        val appBar =
            requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.isVisible = true
        menuItem.setIcon(null)
        menuItem.title = "New event"
        menuItem.setOnMenuItemClickListener {
            val createEventFragment = Create()
            val bundle = Bundle()
            bundle.putString("groupId", groupId)
            createEventFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.app_fragment, createEventFragment)
                .addToBackStack("createEvent").commit()
            true
        }
        appBar.title = "Events"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString(ARG_PARAM1)
            groupName = it.getString(ARG_PARAM2)
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

        val eventList = ArrayList<Event>()
        APIService().doGet<List<EventResponse>>(
            "groups/${groupId}/events",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")


                    data as List<EventResponse>

                    if (data.isEmpty()) {
                        val textView = view.findViewById<TextView>(R.id.noEventsTextView)
                        textView.isVisible = true
                    } else {
                      
                        data.forEach {
                            eventList.add(
                                Event(
                                    groupName ?: "",
                                    it
                                )
                            )
                        }

                        val eventRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                        eventRecyclerView.layoutManager =
                            androidx.recyclerview.widget.LinearLayoutManager(view.context)
                        eventRecyclerView.adapter = EventAdapter(eventList)
                        eventRecyclerView.setHasFixedSize(true)
                    }
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupDetailsEventList.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) = GroupDetailsEventList().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}
