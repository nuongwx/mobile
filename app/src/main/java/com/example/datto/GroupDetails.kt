package com.example.datto

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.db.williamchart.view.LineChartView
import com.google.android.material.appbar.MaterialToolbar
import java.net.URL


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupDetails.newInstance] factory method to
 * create an instance of this fragment.
 */

class MemoryThumbnail(val imgUrl: URL)

class MemoriesListAdapter(private val memories: ArrayList<MemoryThumbnail>) :
    RecyclerView.Adapter<MemoriesListAdapter.MemoriesViewHolder>() {

    inner class MemoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memoryThumbnail: ImageView = itemView.findViewById(R.id.memoryThumbnail)
        val imageButton: ImageButton = itemView.findViewById(R.id.floatingActionButton)

        init {
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, itemView.toString(), Toast.LENGTH_SHORT).show()
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

        if (position == 0) return
        else { // hide button
            holder.imageButton.visibility = View.GONE
        } // create a thread to fetch the image
        val thread = Thread {
            try {
                val bitmap =
                    BitmapFactory.decodeStream(currentItem.imgUrl.openConnection().getInputStream())
                holder.memoryThumbnail.post {
                    holder.memoryThumbnail.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }

    override fun getItemCount(): Int {
        return memories.size
    }
}


class GroupDetails : Fragment() {
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
        return inflater.inflate(R.layout.fragment_group_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val coverImage: ImageView = view.findViewById(R.id.imageView)
        val thread = Thread {
            try {
                val bitmap = BitmapFactory.decodeStream(
                    URL("https://www.gstatic.com/webp/gallery/1.jpg").openConnection()
                        .getInputStream()
                )
                coverImage.post {
                    coverImage.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()

        // create a thread to fetch the images
        val memories = arrayListOf<MemoryThumbnail>()
        memories.add(MemoryThumbnail(URL("https://www.gstatic.com/webp/gallery/1.jpg")))
        memories.add(MemoryThumbnail(URL("https://www.gstatic.com/webp/gallery/2.jpg")))
        memories.add(MemoryThumbnail(URL("https://www.gstatic.com/webp/gallery/3.jpg")))

        val memoriesRecyclerView: RecyclerView =
            view.findViewById(R.id.recyclerView) // scroll horizontally
        memoriesRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            view.context, RecyclerView.HORIZONTAL, false
        )
        memoriesRecyclerView.adapter = MemoriesListAdapter(memories)
        memoriesRecyclerView.setHasFixedSize(true)

        val toMemberList: ConstraintLayout = view.findViewById(R.id.membersListGroupDetailsLL)
        toMemberList.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.app_fragment, GroupDetailsMemberList())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val toEventList: ConstraintLayout = view.findViewById(R.id.eventsListGroupDetailsLL)
        toEventList.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.app_fragment, GroupDetailsEventList())
            transaction.addToBackStack(null)
            transaction.commit()
        }

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

        configTopAppBar()
    }

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isVisible = false
        appBar.title = "Group Name"
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
                putString(ARG_PARAM2, param2)
            }
        }
    }
}