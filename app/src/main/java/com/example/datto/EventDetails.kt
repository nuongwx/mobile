package com.example.datto

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.EventResponse
import com.example.datto.DataClass.FundResponse
import com.example.datto.DataClass.Planning
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Currency
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "eventId"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventDetails.newInstance] factory method to
 * create an instance of this fragment.
 */

// data class Planning(val name: String, val description: String, val start: Date, val end: Date)

class PlanningListAdapter(private val plannings: List<Planning>) :
    RecyclerView.Adapter<PlanningListAdapter.PlanningViewHolder>() {

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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlanningListAdapter.PlanningViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_details_planning_list_items, parent, false)
        return PlanningViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlanningListAdapter.PlanningViewHolder, position: Int) {
        val currentItem = plannings[position]

        holder.planningName.text = currentItem.name
        holder.planningDesc.text = currentItem.description
        holder.startingTime.text =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentItem.start)
        holder.duration.text = buildString {
            append(
                Duration.between(currentItem.start.toInstant(), currentItem.end.toInstant())
                    .toMinutes()
            )
            append(" min")
        }

        holder.setItemClickListener = {
            val bundle = Bundle()
            bundle.putString("id", currentItem.id)
            // dark arts
            bundle.putString(
                "eventId",
                (it.context as MainActivity).supportFragmentManager.fragments.last().arguments?.getString(
                    "eventId"
                )
            )
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

class FunkyDatedPlanningAdapter(private val plans: List<Planning>) :
    RecyclerView.Adapter<FunkyDatedPlanningAdapter.PlanningViewHolder>() {

    inner class PlanningViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val weekDayTextView: TextView =
            itemView.findViewById(R.id.eventDetailsPlanningDateTitleDayOfWeekTextView)
        val dayTextView: TextView =
            itemView.findViewById(R.id.eventDetailsPlanningDateTitleCalendarDateTextView)
        val recyclerView: RecyclerView =
            itemView.findViewById(R.id.eventDetailsDatedPlanningListRecyclerView)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FunkyDatedPlanningAdapter.PlanningViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_details_planning_daily_items, parent, false)
        return PlanningViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: FunkyDatedPlanningAdapter.PlanningViewHolder,
        position: Int
    ) {
        val currentItem = plans[position]

        if (position == 0 || currentItem.start.day != plans[position - 1].start.day) {
            holder.itemView.findViewById<CardView>(R.id.eventDetailsPlanningDateTitleCardView).visibility =
                View.VISIBLE
            holder.recyclerView.visibility = View.VISIBLE

            val plansInDate = plans.filter { it.start.day == currentItem.start.day }

            val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(currentItem.start)
            val dayOfMonth =
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(currentItem.start)

            holder.weekDayTextView.text = dayOfWeek
            holder.dayTextView.text = dayOfMonth
            holder.recyclerView.adapter = PlanningListAdapter(plansInDate)
            holder.recyclerView.setHasFixedSize(true)
            holder.recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        } else {
            holder.itemView.findViewById<CardView>(R.id.eventDetailsPlanningDateTitleCardView).visibility =
                View.GONE
            holder.recyclerView.visibility = View.GONE
        }
    }

    override fun getItemCount() = plans.size
}

class EventDetails : Fragment() {

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // configTopAppBar("Event Name")

        val eventName: TextView = view.findViewById(R.id.eventDetailsEventNameTextView)
        val w2mButton: Button = view.findViewById(R.id.eventDetailsW2MButton)

        val startToPlanButton: Button = view.findViewById(R.id.eventDetailsStartPlaningButton)
        val planningRecyclerView: RecyclerView =
            view.findViewById(R.id.eventDetailsPlanningRecyclerView)
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

        APIService(requireContext()).doGet<EventResponse>(
            "events/${eventId}",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    data as EventResponse
                    val appBar =
                        requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(
                            R.id.app_top_app_bar
                        )
                    eventName.text = data.name
                    appBar.title = data.name
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: $error")
                }
            })

        APIService(requireContext()).doGet<ArrayList<Planning>>("events/${eventId}/timeline",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    plannings.clear()
                    plannings.addAll(data as ArrayList<Planning>)

                    plannings.sortBy { it.start }

                    if (plannings.isEmpty()) {
                        startToPlanButton.visibility = View.VISIBLE
                        viewPlanButton.visibility = View.GONE
                        planningRecyclerView.visibility = View.GONE
                    } else {
                        startToPlanButton.visibility = View.GONE
                        viewPlanButton.visibility = View.VISIBLE
                        planningRecyclerView.visibility = View.VISIBLE
                    }

                    planningRecyclerView.layoutManager =
                        LinearLayoutManager(view.context)
                    val adapter =
                        FunkyDatedPlanningAdapter(plannings.filter { it.start.after(Date()) })
                    planningRecyclerView.adapter = adapter
                    planningRecyclerView.setHasFixedSize(true)

                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: $error")
                }
            })


        startToPlanButton.setOnClickListener {
            viewPlanButton.callOnClick()
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.app_fragment, PlanningEdit().apply {
                    arguments = bundleOf("eventId" to eventId)
                })
                addToBackStack(null)
                commit()
            }
        }

        viewPlanButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.app_fragment, PlanningList().apply {
                    arguments = bundleOf("eventId" to eventId)
                })
                addToBackStack(null)
                commit()
            }
        }

        expenseDetailsButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.app_fragment, FundList(arguments?.getString("eventId")!!))
                addToBackStack(null)
                commit()
            }
        }

        newExpenseButton.setOnClickListener {
            expenseDetailsButton.callOnClick()
            requireActivity().supportFragmentManager.beginTransaction().apply {
                arguments?.getString("eventId")?.let { it1 -> NewFund(it1) }?.let { it2 ->
                    replace(R.id.app_fragment, it2)
                }
                addToBackStack(null)
                commit()
            }
        }

        APIService(requireContext()).doGet<FundResponse>(
            "events/${eventId}/funds",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    data as FundResponse

                    val fundList = ArrayList<FundItem>()
                    var inAmount = 0.0
                    var outAmount = 0.0

                    for (fund in data.funds) {
                        // Format paidAt date
                        val originalFormat =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                        val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
                        val date = originalFormat.parse(fund.paidAt)

                        val f = FundItem(
                            fund.id,
                            fund.info,
                            fund.amount,
                            fund.paidBy.profile.fullName,
                            targetFormat.format(date!!)
                        )
                        fundList.add(f)

                        if (fund.amount > 0) {
                            inAmount += fund.amount
                        } else {
                            outAmount -= fund.amount
                        }
                    }

                    if (fundList.isEmpty()) {
                        expenseDetailsButton.visibility = View.INVISIBLE
                        newExpenseButton.visibility = View.VISIBLE
                        view.findViewById<CardView>(R.id.eventDetailsExpenseOverViewCardView).visibility =
                            View.INVISIBLE
                    } else {
                        expenseDetailsButton.visibility = View.VISIBLE
                        newExpenseButton.visibility = View.GONE
                        view.findViewById<CardView>(R.id.eventDetailsExpenseOverViewCardView).visibility =
                            View.VISIBLE

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

        configTopAppBar()
    }

    private fun configTopAppBar() {
        val appBar =
            requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.isVisible = true
        menuItem.title = null
        menuItem.setIcon(R.drawable.ic_edit)
        menuItem.setOnMenuItemClickListener {
            Toast.makeText(requireContext(), "Edit event: $eventId", Toast.LENGTH_SHORT).show()
            true
        }
        appBar.title = "Events"

        appBar.navigationIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_back)
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