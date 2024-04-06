package com.example.datto

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.net.URL

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupDetailsMemberList.newInstance] factory method to
 * create an instance of this fragment.
 */

class MemberItem(val name: String, val image: URL)

class MemberListAdapter(private val members: ArrayList<MemberItem>) :
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
            LayoutInflater.from(parent.context).inflate(R.layout.group_details_members_list_items, parent, false)
        return MemberViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MemberListAdapter.MemberViewHolder, position: Int) {
        val currentItem = members[position]
        holder.memberName.text = currentItem.name

        val thread = Thread {
            try {
                val bitmap =
                    BitmapFactory.decodeStream(currentItem.image.openConnection().getInputStream())
                holder.memberImage.post {
                    holder.memberImage.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }

    override fun getItemCount() = members.size
}


class GroupDetailsMemberList : Fragment() {
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
        return inflater.inflate(R.layout.fragment_group_details_member_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val memberList = ArrayList<MemberItem>()
        memberList.add(MemberItem("John Doe", URL("https://via.assets.so/img.jpg?w=500&h=500&tc=&bg=#7f7f7f&t=Hello%20World")))
        memberList.add(MemberItem("Jane Doe", URL("https://via.assets.so/img.jpg?w=500&h=500&tc=&bg=#7f7f7f&t=Hello%20World")))
        memberList.add(MemberItem("John Smith", URL("https://via.assets.so/img.jpg?w=500&h=500&tc=&bg=#7f7f7f&t=Hello%20World")))

        val memberRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        memberRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(view.context)
        memberRecyclerView.adapter = MemberListAdapter(memberList)
        memberRecyclerView.setHasFixedSize(true)

        val regenButton: ImageButton = view.findViewById(R.id.regenInviteCodeImageButton)
        regenButton.setOnClickListener {
            Toast.makeText(view.context, "Regenerating invite code", Toast.LENGTH_SHORT).show()
        }

        val copyButton: ImageButton = view.findViewById(R.id.copyInviteCodeImageButton)
        copyButton.setOnClickListener {
            val inviteCode: TextView = view.findViewById(R.id.inviteCodeTextView)
            val clipboard = android.content.Context.CLIPBOARD_SERVICE
            val clip = android.content.ClipData.newPlainText("invite code", inviteCode.text)
            val clipboardManager = view.context.getSystemService(clipboard) as android.content.ClipboardManager
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