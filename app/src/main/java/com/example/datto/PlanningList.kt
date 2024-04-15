package com.example.datto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.Duration
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlanningList.newInstance] factory method to
 * create an instance of this fragment.
 */


class PlanningList : Fragment() {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_planning_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plannings = ArrayList<Planning>()

        plannings.addAll(arrayListOf(
                Planning("Planning 1", "Description 1", Date(), Duration.ofMinutes(30)),
                Planning("Planning 2", "Description 2", Date(Date().time + 61 * 60 * 1000), Duration.ofMinutes(40)),
                Planning("Planning 3", "Description 3", Date(Date().time + 60 * 60 * 1000), Duration.ofMinutes(121)),
                Planning("Planning TMR", "Description 3", Date(Date().time + 10 * 24 * 60 * 60 * 1000), Duration.ofMinutes(121))))

        val recyclerView = view.findViewById<RecyclerView>(R.id.eventDetailsPlanningListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = FunkyDatedPlanningAdapter(plannings)
        recyclerView.setHasFixedSize(true)

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