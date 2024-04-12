package com.example.datto

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewGroupInviteCode.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewGroupInviteCode(
    name: String,
    inviteCode: String
) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = name
    private var param2: String? = inviteCode

    private lateinit var inviteCodeText: TextView
    private lateinit var shareButton: Button
    private lateinit var copyButton: Button

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = false
        menuItem.title = null
        menuItem.setIcon(null)
        menuItem.setOnMenuItemClickListener(null)

        appBar.title = param1
        appBar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configTopAppBar()

        // Assign id for each element
        inviteCodeText = view.findViewById(R.id.new_group_invite_code)
        shareButton = view.findViewById(R.id.new_group_invite_code_share)
        copyButton = view.findViewById(R.id.new_group_invite_code_copy)

        // Set text for each element
        inviteCodeText.text = param2

        // Copy invite code to clipboard when click on it
        inviteCodeText.setOnClickListener {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("invite code", param2)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Invite code copied to clipboard", Toast.LENGTH_SHORT)
                .show()
        }

        // Copy invite code to clipboard when click on copy button
        copyButton.setOnClickListener {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("invite code", param2)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Invite code copied to clipboard", Toast.LENGTH_SHORT)
                .show()
        }

        // Share invite code when click on share button
        shareButton.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Join my group with this invite code: $param2")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        configTopAppBar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_group_invite_code, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewGroupInviteCode.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewGroupInviteCode(param1, param2).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}