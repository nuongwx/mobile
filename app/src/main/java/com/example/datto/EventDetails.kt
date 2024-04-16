package com.example.datto

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.FundResponse
import com.example.datto.DataClassRecyclerView.FundItem
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Currency
import java.util.Date
import java.util.Locale

private const val ARG_PARAM1 = "eventId"

/**
 * A simple [Fragment] subclass.
 * Use the [EventDetails.newInstance] factory method to
 * create an instance of this fragment.
 */

data class Planning(val name: String, val description: String, val start: Date, val duration: Duration)

class PlanningListAdapter(private val plannings: List<Planning>) : RecyclerView.Adapter<PlanningListAdapter.PlanningViewHolder>() {

    inner class PlanningViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val planningName: TextView = itemView.findViewById(R.id.planningNameTextView)
        val planningDesc: TextView = itemView.findViewById(R.id.planningDescriptionTextView)
        val startingTime: TextView = itemView.findViewById(R.id.planningTimeTextView)
        val duration: TextView = itemView.findViewById(R.id.planningDurationTextView)

        var setItemClickListener: ((View) -> Unit)? = null

        init {
            itemView.setOnClickListener {
                setItemClickListener?.invoke(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanningListAdapter.PlanningViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.event_details_planning_list_items, parent, false)
        return PlanningViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlanningListAdapter.PlanningViewHolder, position: Int) {
        val currentItem = plannings[position]

        holder.planningName.text = currentItem.name
        holder.planningDesc.text = currentItem.description
        holder.startingTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentItem.start)
        holder.duration.text = buildString {
            append(currentItem.duration.toMinutes().toString())
            append(" min")
        }

        holder.setItemClickListener = {
            val bundle = Bundle()
            bundle.putString("id", currentItem.name)
            val eventDetails = PlanningEdit()
            eventDetails.arguments = bundle
            val transaction = (it.context as MainActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.app_fragment, eventDetails)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun getItemCount() = plannings.size
}

class FunkyDatedPlanningAdapter(private val plans: List<Planning>) : RecyclerView.Adapter<FunkyDatedPlanningAdapter.PlanningViewHolder1>() {

    inner class PlanningViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val weekDayTextView: TextView = itemView.findViewById(R.id.eventDetailsPlanningDateTitleDayOfWeekTextView)
        val dayTextView: TextView = itemView.findViewById(R.id.eventDetailsPlanningDateTitleCalendarDateTextView)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.eventDetailsDatedPlanningListRecyclerView)

        init {
            // plans.sortBy { it.start }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FunkyDatedPlanningAdapter.PlanningViewHolder1 {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.event_details_planning_daily_items, parent, false)
        return PlanningViewHolder1(itemView)
    }

    override fun onBindViewHolder(holder: FunkyDatedPlanningAdapter.PlanningViewHolder1, position: Int) {
        val currentItem = plans[position]

        if (position == 0 || currentItem.start.day != plans[position - 1].start.day) {
            holder.itemView.findViewById<CardView>(R.id.eventDetailsPlanningDateTitleCardView).visibility = View.VISIBLE
            holder.recyclerView.visibility = View.VISIBLE

            val plansInDate = plans.filter { it.start.day == currentItem.start.day }

            val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(currentItem.start)
            val dayOfMonth = SimpleDateFormat("dd MMM", Locale.getDefault()).format(currentItem.start)

            holder.weekDayTextView.text = dayOfWeek
            holder.dayTextView.text = dayOfMonth
            holder.recyclerView.adapter = PlanningListAdapter(plansInDate)
            holder.recyclerView.setHasFixedSize(true)
            holder.recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        } else {
            holder.itemView.findViewById<CardView>(R.id.eventDetailsPlanningDateTitleCardView).visibility = View.GONE
            holder.recyclerView.visibility = View.GONE
        }
    }

    override fun getItemCount() = plans.size
}

class EventDetails : Fragment() {
    private var eventID: String? = null

    private fun configTopAppBar(name: String) {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.isVisible = true
        menuItem.title = null
        menuItem.setIcon(R.drawable.ic_edit)
        menuItem.setOnMenuItemClickListener(null)

        appBar.title = name
        appBar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventID = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configTopAppBar("Event Name")

        val eventName: TextView = view.findViewById(R.id.eventDetailsEventNameTextView)
        val w2mButton: Button = view.findViewById(R.id.eventDetailsW2MButton)

        val startToPlanButton: Button = view.findViewById(R.id.eventDetailsStartPlaningButton)
        val planningRecyclerView: RecyclerView = view.findViewById(R.id.eventDetailsPlanningRecyclerView)
        val viewPlanButton: Button = view.findViewById(R.id.eventDetailsViewPlanButton)

        val expenseDetailsButton: Button = view.findViewById(R.id.eventDetailsExpensesDetailsButton)
        val newExpenseButton: Button = view.findViewById(R.id.eventDetailsNewExpenseButton)
        val expenseInTextView: TextView = view.findViewById(R.id.eventDetailsExpenseInTextView)
        val expenseOutTextView: TextView = view.findViewById(R.id.eventDetailsExpenseOutTextView)

        val newMemoryButton: Button = view.findViewById(R.id.eventDetailsNewMemoryButton)

        w2mButton.setOnClickListener {
            Toast.makeText(view.context, "W2M", Toast.LENGTH_SHORT).show()
        }

        val plannings = ArrayList<Planning>()

        plannings.addAll(arrayListOf(
                Planning("Planning 1", "Description 1", Date(), Duration.ofMinutes(30)),
                Planning("Planning 2", "Description 2", Date(Date().time + 61 * 60 * 1000), Duration.ofMinutes(40)),
                Planning("Planning 3", "Description 3", Date(Date().time + 60 * 60 * 1000), Duration.ofMinutes(121)),
                Planning("Planning TMR", "Description 3", Date(Date().time + 10 * 24 * 60 * 60 * 1000), Duration.ofMinutes(121))))

        plannings.sortBy { it.start }
        if (plannings.size > 3)
            plannings.subList(0, 3)

        if (plannings.isEmpty()) {
            startToPlanButton.visibility = View.VISIBLE
            viewPlanButton.visibility = View.GONE
            planningRecyclerView.setPadding(0, 0, 0, 0)
        } else {
            startToPlanButton.visibility = View.GONE
            viewPlanButton.visibility = View.VISIBLE
            planningRecyclerView.setPadding(0, 0, 0, 100)
        }

        planningRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(view.context)
        val adapter = PlanningListAdapter(plannings)
        planningRecyclerView.adapter = adapter
        planningRecyclerView.setHasFixedSize(true)

        startToPlanButton.setOnClickListener {
            Toast.makeText(view.context, "Start Planning", Toast.LENGTH_SHORT).show()
        }

        viewPlanButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.app_fragment, PlanningList())
                addToBackStack(null)
                commit()
            }
        }

        expenseDetailsButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.app_fragment, FundList())
                addToBackStack(null)
                commit()
            }
        }

        newExpenseButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                arguments?.getString("eventId")?.let { it1 -> NewFund(it1) }?.let { it2 ->
                    replace(R.id.app_fragment, it2)
                }
                addToBackStack(null)
                commit()
            }
        }

        APIService().doGet<FundResponse>("events/${arguments?.getString("eventId")}/funds", object : APICallback<Any> {
            override fun onSuccess(data: Any) {
                data as FundResponse

                val fundList = ArrayList<FundItem>()
                var inAmount: Double = 0.0
                var outAmount: Double = 0.0

                for (fund in data.funds) {
                    // Format paidAt date
                    val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
                    val date = originalFormat.parse(fund.paidAt)

                    val f = FundItem(fund.id, fund.info, fund.amount, fund.paidBy.profile.fullName, targetFormat.format(date!!))
                    fundList.add(f)

                    if (fund.amount > 0) {
                        inAmount += fund.amount
                    } else {
                        outAmount -= fund.amount
                    }
                }

                if (fundList.isEmpty()) {
                    expenseDetailsButton.visibility = View.GONE
                    newExpenseButton.visibility = View.VISIBLE
                    view.findViewById<CardView>(R.id.eventDetailsExpenseOverViewCardView).visibility = View.INVISIBLE
                } else {
                    expenseDetailsButton.visibility = View.VISIBLE
                    newExpenseButton.visibility = View.GONE
                    view.findViewById<CardView>(R.id.eventDetailsExpenseOverViewCardView).visibility = View.VISIBLE

                }

                val format: NumberFormat = NumberFormat.getCurrencyInstance()
                format.setMaximumFractionDigits(0)
                format.currency = Currency.getInstance("VND")
                expenseInTextView.text = format.format(inAmount)
                expenseOutTextView.text = format.format(outAmount)
            }

            override fun onError(error: Throwable) {
                Log.e("API_SERVICE", "Error: $error")
            }
        })

        newMemoryButton.setOnClickListener {
            Toast.makeText(view.context, "New Memory", Toast.LENGTH_SHORT).show()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventDetails.
         */
        @JvmStatic
        fun newInstance(param1: String) = EventDetails().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
            }
        }
    }
}