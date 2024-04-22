package com.example.datto

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.GroupResponse
import com.example.datto.DataClass.MemoryResponse
import com.example.datto.GlobalVariable.GlobalVariable
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


private const val ARG_PARAM1 = "groupId"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupDetails.newInstance] factory method to
 * create an instance of this fragment.
 */

class MemoriesListAdapter(private val memories: ArrayList<MemoryResponse>) :
    RecyclerView.Adapter<MemoriesListAdapter.MemoriesViewHolder>() {

    inner class MemoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memoryThumbnail: ImageView = itemView.findViewById(R.id.memoryThumbnail)
        val imageButton: ImageButton = itemView.findViewById(R.id.floatingActionButton)

        var setOnClickListener: (() -> Unit)? = null

        init {
            itemView.setOnClickListener {
                setOnClickListener?.invoke()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MemoriesListAdapter.MemoriesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_details_memories_cover_items, parent, false)
        return MemoriesViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: MemoriesListAdapter.MemoriesViewHolder, position: Int) {
        val currentItem = memories[position]

        if (position == 0) {
            // set unique button for the first item
            holder.imageButton.visibility = View.VISIBLE
            holder.setOnClickListener = {
                val newMemoryFragment = NewMemory().apply {
                    arguments = Bundle().apply {
                        putString(
                            "groupId",
                            (holder.itemView.context as MainActivity).supportFragmentManager.fragments.last().arguments?.getString(
                                "groupId"
                            )
                        )
                    }
                }
                val transaction =
                    (holder.itemView.context as MainActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.app_fragment, newMemoryFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        } else { // hide button
            holder.imageButton.visibility = View.GONE
            val thread = Thread {
                try {
                    val bitmap = BitmapFactory.decodeStream(
                        URL(GlobalVariable.BASE_URL + "files/" + currentItem.thumbnail).openStream()
                    )
                    holder.memoryThumbnail.post {
                        holder.memoryThumbnail.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()

            holder.setOnClickListener = {
                val MemoryViewFragment = MemoryView()
                val transaction =
                    (holder.itemView.context as MainActivity).supportFragmentManager.beginTransaction()
                val bundle = Bundle()
                bundle.putString(
                    "imgUrl",
                    GlobalVariable.BASE_URL + "files/" + currentItem.thumbnail
                )
                bundle.putString(
                    "groupId",
                    (holder.itemView.context as MainActivity).supportFragmentManager.fragments.last().arguments?.getString(
                        "groupId"
                    )
                )
                bundle.putString("id", currentItem.id)
                MemoryViewFragment.arguments = bundle
                transaction.replace(R.id.app_fragment, MemoryViewFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }

    override fun getItemCount(): Int {
        return memories.size
    }
}

data class FundData(
    @SerializedName("_id")
    val id: String,
    val amount: Double,
    val paidAt: String
)

data class GroupFundsResponse(
    @SerializedName("_id")
    val id: String,
    val funds: List<FundData>
)

class GroupDetails : Fragment() {
    private var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var group: GroupResponse? = null

        APIService().doGet<GroupResponse>("groups/${groupId}",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")

                    data as GroupResponse

                    group = data

                    configTopAppBar(data.name, if (data.thumbnail != null) data.thumbnail else "")

                    val coverImage: ImageView = view.findViewById(R.id.imageView)
                    val thread = Thread {
                        try {
                            activity?.runOnUiThread {
                                val imageUrl =
                                    if (data.thumbnail != null) GlobalVariable.BASE_URL + "files/" + data.thumbnail else null
                                if (imageUrl != null) {
                                    Picasso.get().load(imageUrl).into(coverImage)
                                } else {
                                    Picasso.get().load(R.drawable.cover).into(coverImage)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    thread.start()

                    val toEventList: ConstraintLayout =
                        view.findViewById(R.id.eventsListGroupDetailsLL)
                    toEventList.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("groupId", data.id)
                        bundle.putString("groupName", data.name)
                        val GroupDetailsEventListFragment = GroupDetailsEventList()
                        GroupDetailsEventListFragment.arguments = bundle

                        val transaction = parentFragmentManager.beginTransaction()
                        transaction.replace(R.id.app_fragment, GroupDetailsEventListFragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }

                    val toMemberList: ConstraintLayout =
                        view.findViewById(R.id.membersListGroupDetailsLL)
                    toMemberList.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putStringArrayList("memberIds", ArrayList(data.members))
                        bundle.putString("inviteCode", data.inviteCode)
                        bundle.putString("groupId", data.id)
                        val GroupDetailsMemberList = GroupDetailsMemberList()
                        GroupDetailsMemberList.arguments = bundle

                        val transaction = parentFragmentManager.beginTransaction()
                        transaction.replace(R.id.app_fragment, GroupDetailsMemberList)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })


        // create a thread to fetch the images
        val memories = ArrayList<MemoryResponse>()

        val memoriesRecyclerView: RecyclerView =
            view.findViewById(R.id.memoriesCoverGroupDetailsRecyclerView) // scroll horizontally
        memoriesRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            view.context, RecyclerView.HORIZONTAL, false
        )
        val adapter = MemoriesListAdapter(memories)
        memoriesRecyclerView.adapter = adapter
        memoriesRecyclerView.setHasFixedSize(true)

        APIService().doGet<List<MemoryResponse>>("groups/${groupId}/memories",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")

                    data as List<MemoryResponse>

                    memories.clear()
                    memories.add(MemoryResponse("", "😎", ""))

                    for (memory in data) {
                        memories.add(MemoryResponse(memory.thumbnail, memory.info, memory.id))
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })

        val lineChart: LineChart = view.findViewById(R.id.lineChart)
        APIService().doGet<List<GroupFundsResponse>>("groups/${groupId}/funds",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")

                    data as List<GroupFundsResponse>

                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    targetFormat.timeZone = TimeZone.getTimeZone("UTC")

                    val positiveAmountsByDay = mutableMapOf<String, Double>()
                    val negativeAmountsByDay = mutableMapOf<String, Double>()

                    data.flatMap { groupFundsResponse ->
                        groupFundsResponse.funds.map { fundData ->
                            val fullDate = sdf.parse(fundData.paidAt)!!.let {
                                targetFormat.format(it)
                            }

                            if (fundData.amount >= 0) {
                                positiveAmountsByDay[fullDate] = positiveAmountsByDay.getOrDefault(
                                    fullDate,
                                    0.0
                                ) + fundData.amount
                            } else {
                                negativeAmountsByDay[fullDate] = negativeAmountsByDay.getOrDefault(
                                    fullDate,
                                    0.0
                                ) + fundData.amount
                            }
                        }
                    }

                    var positiveEntries = positiveAmountsByDay.map { (fullDate, amount) ->
                        val date = targetFormat.parse(fullDate)
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                        Entry(dayOfYear.toFloat(), amount.toFloat())
                    }

                    var negativeEntries = negativeAmountsByDay.map { (fullDate, amount) ->
                        val date = targetFormat.parse(fullDate)
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                        Entry(dayOfYear.toFloat(), -1 * amount.toFloat())
                    }

                    positiveEntries = positiveEntries.sortedBy { it.x }
                    negativeEntries = negativeEntries.sortedBy { it.x }

                    Log.d("API_SERVICE", "Positive entries: $positiveEntries")
                    Log.d("API_SERVICE", "Negative entries: $negativeEntries")

                    val positiveLineDataSet =
                        LineDataSet(positiveEntries, "Funds").apply {
                            setColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.md_theme_primary
                                )
                            )
                            setCircleColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.md_theme_primary
                                )
                            )
                        }
                    val negativeLineDataSet =
                        LineDataSet(negativeEntries, "Expenses").apply {
                            setColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.md_theme_tertiary
                                )
                            )
                            setCircleColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.md_theme_tertiary
                                )
                            )
                        }
                    val lineData = LineData(positiveLineDataSet, negativeLineDataSet)

                    lineChart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.DAY_OF_YEAR, value.toInt())
                            return SimpleDateFormat("MMM dd", Locale.US).format(calendar.time)
                        }
                    }
                    lineChart.description.isEnabled = false // Disable the description
                    lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM // Set the x-axis position
                    val font = ResourcesCompat.getFont(requireContext(), R.font.be_vietnam)
                    lineChart.xAxis.typeface = font
                    lineChart.axisLeft.typeface = font
                    lineChart.axisRight.typeface = font
                    lineChart.legend.typeface = font
                    lineChart.data = lineData
                    lineChart.invalidate() // refresh the chart
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })
    }

    private fun configTopAppBar(name: String, thumbnail: String) {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.isVisible = true
        menuItem.title = null
        menuItem.setIcon(R.drawable.ic_edit)
        menuItem.setOnMenuItemClickListener {
            val groupEditFragment = GroupEdit()
            val bundle = Bundle()
            bundle.putString("groupId", groupId)
            bundle.putString("groupName", name)
            bundle.putString("thumbnail", thumbnail)
            groupEditFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.app_fragment, groupEditFragment)
                .addToBackStack("GroupEdit").commit()
            true
        }
        appBar.title = name
        appBar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupDetails.
         */ // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = GroupDetails().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
            }
        }
    }
}