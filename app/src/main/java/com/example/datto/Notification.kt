package com.example.datto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.utils.FirebaseNotification
import com.google.android.material.appbar.MaterialToolbar

/**
 * A fragment representing a list of Items.
 */
class Notification : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification_list, container, false)

        // Set the adapter
        // if (view is RecyclerView) {
        //     with(view) {
        //         layoutManager = when {
        //             columnCount <= 1 -> LinearLayoutManager(context)
        //             else -> GridLayoutManager(context, columnCount)
        //         }
        //         adapter = MyItemRecyclerViewAdapter(PlaceholderContent.ITEMS)
        //     }
        // }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configAppBar()

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val notifications = mutableListOf<NotificationItem>()
        val adapter = MyItemRecyclerViewAdapter(notifications)
        recyclerView.adapter = adapter

        APIService(requireContext()).doGet<List<NotificationItem>>(
            "notifications/${CredentialService().get()}",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    data as List<NotificationItem>
                    notifications.clear()
                    notifications.addAll(data)
                    adapter.notifyDataSetChanged()
                }

                override fun onError(error: Throwable) {
                    // Handle error
                }
            })
    }

    private fun configAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.title = "Test Notification"
        menuItem.setIcon(null)
        menuItem.setOnMenuItemClickListener {
            FirebaseNotification(requireContext()).compose(
                "66195ac2c2f63b123dc73596",
                "brand new message!",
                "oh body of the message!"
            )
            true
        }

        appBar.title = "Notification"
        appBar.navigationIcon = null

    }


    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            Notification().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}