package com.example.datto

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.db.williamchart.view.LineChartView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.GroupResponse
import com.example.datto.DataClass.MemoryResponse
import com.example.datto.GlobalVariable.GlobalVariable
import com.google.android.material.appbar.MaterialToolbar
import com.squareup.picasso.Picasso
import java.net.URL


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


class GroupDetails : Fragment() {
    // TODO: Rename and change types of parameters
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

        APIService(requireContext()).doGet<GroupResponse>("groups/${groupId}",
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


        val chart: LineChartView = view.findViewById(R.id.chart)
        val entries: List<Pair<String, Float>> = listOf(
            "Jan" to 4f,
            "Feb" to 2f,
            "Mar" to 3f,
            "Apr" to 1f,
            "May" to 5f,
            "Jun" to 2f,
            "Jul" to 4f,
            "Aug" to 3f,
            "Sep" to 4f,
            "Oct" to 2f,
            "Nov" to 3f,
            "Dec" to 6f
        )
        chart.show(entries)

        chart.setOnClickListener {
            Toast.makeText(context, "Chart clicked", Toast.LENGTH_SHORT).show()
        }
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