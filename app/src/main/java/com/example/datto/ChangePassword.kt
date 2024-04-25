package com.example.datto

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.ChangePasswordRequest
import com.google.android.material.appbar.MaterialToolbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChangePassword.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChangePassword : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var currentPassword: com.google.android.material.textfield.TextInputEditText
    private lateinit var newPassword: com.google.android.material.textfield.TextInputEditText
    private lateinit var confirmPassword: com.google.android.material.textfield.TextInputEditText

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.title = "Save"
        menuItem.setIcon(null)
        menuItem.setOnMenuItemClickListener{
            // Get data
            val body = ChangePasswordRequest(currentPassword.text.toString(), newPassword.text.toString(), confirmPassword.text.toString())

            // Set onClickListener to the button
            APIService(requireContext()).doPatch<ChangePasswordRequest>("accounts/${CredentialService().get()}/password", body, object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                    Log.d("API_SERVICE", "Data: $data")

                    // Back to previous fragment
                    requireActivity().supportFragmentManager.popBackStack()
                }

                override fun onError(error: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })

            true
        }

        appBar.title = "Change Password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        configTopAppBar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assign id to each element
        currentPassword = view.findViewById(R.id.change_password_current)
        newPassword = view.findViewById(R.id.change_password_new)
        confirmPassword = view.findViewById(R.id.change_password_confirm)
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
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChangePassword.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChangePassword().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}