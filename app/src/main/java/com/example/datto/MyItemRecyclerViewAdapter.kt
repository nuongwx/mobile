package com.example.datto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.databinding.FragmentNotificationBinding
import com.example.datto.placeholder.PlaceholderContent.PlaceholderItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.annotations.SerializedName

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */

class NotificationItem(
    @SerializedName("_id")
    val id: String,
    val topic: String,
    val title: String,
    val body: String,
)
class MyItemRecyclerViewAdapter(
    private val values: List<NotificationItem>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.id
        holder.titleView.text = item.title
        holder.bodyView.text = item.body

        if (position != values.size - 1) {
            // add divider
            holder.itemView.findViewById<View>(R.id.divider).visibility = View.VISIBLE
        } else {
            holder.itemView.findViewById<View>(R.id.divider).visibility = View.GONE
        }

        holder.onClickListener = View.OnClickListener {
            val groupDetailsFragment = GroupDetails()
            val bundle = Bundle()
            bundle.putString("groupId", item.topic)
            groupDetailsFragment.arguments = bundle

            (holder.itemView.context as MainActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.app_fragment, groupDetailsFragment)
                .addToBackStack(null)
                .commit()
            val bottomNavigation =
                (holder.itemView.context as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_home).isChecked =
                true
        }

    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val titleView: TextView = binding.notificationListItemTitle
        val bodyView: TextView = binding.notificationListItemBody
        var onClickListener: View.OnClickListener? = null

        init {
            binding.root.setOnClickListener {
                onClickListener?.onClick(it)
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + bodyView.text + "'"
        }
    }

}