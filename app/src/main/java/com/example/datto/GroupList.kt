package com.example.datto

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.GroupResponse
import com.example.datto.GlobalVariable.GlobalVariable
import com.google.android.material.appbar.MaterialToolbar
import com.squareup.picasso.Picasso

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupList.newInstance] factory method to
 * create an instance of this fragment.
 */

class GroupListAdapter(
    private val groups: ArrayList<GroupResponse>, private val fragmentManager: FragmentManager
) : androidx.recyclerview.widget.RecyclerView.Adapter<GroupListAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val groupName = itemView.findViewById<android.widget.TextView>(R.id.groupNameTextView)
        val groupImage = itemView.findViewById<android.widget.ImageView>(R.id.groupImageView)

        init {
            itemView.setOnClickListener {
                fragmentManager.beginTransaction().replace(R.id.app_fragment, GroupDetails())
                    .addToBackStack("GroupDetails").commit()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): GroupListAdapter.GroupViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.groups_list_items, parent, false)
        return GroupViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroupListAdapter.GroupViewHolder, position: Int) {
        val currentItem = groups[position]

        holder.groupName.text = currentItem.name

        // Load image with Picasso and new thread
        try {
            val imageUrl =
                if (currentItem.thumbnail != null) GlobalVariable.BASE_URL + "files/" + currentItem.thumbnail else null
            if (imageUrl != null) {
                Picasso.get().load(imageUrl).into(holder.groupImage)
            } else {
                Picasso.get().load(R.drawable.avatar).into(holder.groupImage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return groups.size
    }
}

class GroupList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.title = "Join"
        menuItem.setIcon(null)
        menuItem.setOnMenuItemClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.app_fragment, JoinGroup()).addToBackStack("JoinGroup").commit()
            true
        }

        appBar.title = "Groups"
        appBar.navigationIcon = null
    }

    override fun onPause() {
        super.onPause()

        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        appBar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        configTopAppBar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configTopAppBar()

        val newGroupBtn: Button = view.findViewById(R.id.newGroupButton)
        newGroupBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.app_fragment, NewGroup()).addToBackStack("NewGroup").commit()
        }

        val groupList = ArrayList<GroupResponse>()

        APIService().doGet<List<GroupResponse>>("groups/accounts/${CredentialService().get()}",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")

                    data as List<GroupResponse>

                    data.forEach {
                        groupList.add(it)
                    }

                    val groupRecyclerView =
                        view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView2)
                    groupRecyclerView.layoutManager =
                        androidx.recyclerview.widget.LinearLayoutManager(
                            view.context, androidx.recyclerview.widget.RecyclerView.VERTICAL, false
                        )
                    groupRecyclerView.adapter = GroupListAdapter(groupList, parentFragmentManager)
                    groupRecyclerView.setHasFixedSize(true)
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })

//        val toGroupDetails: Button = view.findViewById(R.id.to_group_details)
//        toGroupDetails.setOnClickListener {
//            val transaction = parentFragmentManager.beginTransaction()
//            transaction.replace(R.id.app_fragment, GroupDetails())
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = GroupList().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}