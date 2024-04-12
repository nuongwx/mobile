package com.example.datto

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.AccountResponse
import com.example.datto.DataClass.EventResponse
import com.example.datto.GlobalVariable.GlobalVariable
import com.squareup.picasso.Picasso
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupDetailsMemberList.newInstance] factory method to
 * create an instance of this fragment.
 */

class MemberListAdapter(private val members: ArrayList<AccountResponse>) :
    RecyclerView.Adapter<MemberListAdapter.MemberViewHolder>() {

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberName: TextView = itemView.findViewById(R.id.memberNameTextView)
        val memberImage: ImageView = itemView.findViewById(R.id.memberCoverImageView)
        val removeButton: ImageButton = itemView.findViewById(R.id.memberRemoveButton)

        init {
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, itemView.toString(), Toast.LENGTH_SHORT).show()
            }

            removeButton.setOnClickListener {
                members.removeAt(absoluteAdapterPosition)
                notifyItemRemoved(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MemberListAdapter.MemberViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.group_details_members_list_items, parent, false)
        return MemberViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MemberListAdapter.MemberViewHolder, position: Int) {
        val currentItem = members[position]
        holder.memberName.text = currentItem.profile.fullName

        try {
            val imageUrl =
                if (currentItem.profile.avatar != null) GlobalVariable.BASE_URL + "files/" + currentItem.profile.avatar else null
            if (imageUrl != null) {
                Picasso.get().load(imageUrl).into(holder.memberImage)
            } else {
                Picasso.get().load(R.drawable.avatar).into(holder.memberImage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount() = members.size
}


class GroupDetailsMemberList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private fun configTopAppBar() {
        val topAppBar =
            requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.app_top_app_bar)
        topAppBar.title = "Members"
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
        return inflater.inflate(R.layout.fragment_group_details_member_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configTopAppBar()

        val memberIds = arguments?.getStringArrayList("memberIds")
        val inviteCode = arguments?.getString("inviteCode")

        val inviteCodeTextView = view.findViewById<TextView>(R.id.inviteCodeTextView)
        inviteCodeTextView.text = inviteCode

        val memberList = ArrayList<AccountResponse>()
        var completedRequests = 0
        for (memberId in memberIds!!) {
            APIService().doGet<AccountResponse>("accounts/${memberId}", object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")

                    data as AccountResponse

                    memberList.add(data)

                    completedRequests++ // Increment the count of completed requests

                    // Check if all requests have completed
                    if (completedRequests == memberIds.size) {
                        val memberRecyclerView =
                            view.findViewById<RecyclerView>(R.id.memberRecyclerView)
                        memberRecyclerView.layoutManager =
                            androidx.recyclerview.widget.LinearLayoutManager(view.context)
                        memberRecyclerView.adapter = MemberListAdapter(memberList)
                        memberRecyclerView.setHasFixedSize(true)
                    }
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })
        }

        val regenButton: ImageButton = view.findViewById(R.id.regenInviteCodeImageButton)
        regenButton.setOnClickListener {
            Toast.makeText(view.context, "Regenerating invite code", Toast.LENGTH_SHORT).show()
        }

        val copyButton: ImageButton = view.findViewById(R.id.copyInviteCodeImageButton)
        copyButton.setOnClickListener {
            val clipboard = android.content.Context.CLIPBOARD_SERVICE
            val clip = android.content.ClipData.newPlainText("invite code", inviteCode)
            val clipboardManager =
                view.context.getSystemService(clipboard) as android.content.ClipboardManager
            clipboardManager.setPrimaryClip(clip)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupDetailsMemberList.
         */ // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = GroupDetailsMemberList().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}