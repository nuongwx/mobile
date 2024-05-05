package com.example.datto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.Planning
import com.google.android.material.appbar.MaterialToolbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "eventId"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlanningList.newInstance] factory method to
 * create an instance of this fragment.
 */


class PlanningList : Fragment() {
    // TODO: Rename and change types of parameters
    private var eventId: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_planning_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plannings = ArrayList<Planning>()

        APIService(requireContext()).doGet<List<Planning>>("events/${eventId}/timeline", object : APICallback<Any> {
            override fun onSuccess(data: Any) {
                plannings.clear()
                plannings.addAll(data as List<Planning>)
                plannings.sortBy { it.start }

                val recyclerView =
                    view.findViewById<RecyclerView>(R.id.eventDetailsPlanningListRecyclerView)
                recyclerView.layoutManager = LinearLayoutManager(view.context)
                recyclerView.adapter = FunkyDatedPlanningAdapter(plannings)
                // recyclerView.setHasFixedSize(true)
            }

            override fun onError(error: Throwable) {
                println(error.message)
            }
        })

        configTopAppBar()
    }

    fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.title = "New"
        menuItem.setIcon(null)
        menuItem.setOnMenuItemClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.app_fragment, PlanningEdit().apply {
                    arguments = Bundle().apply {
                        putString("eventId", eventId)
                    }
                }).addToBackStack(null).commit()
            true
        }

        appBar.title = "Plannings"
        appBar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlanningList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = PlanningList().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}