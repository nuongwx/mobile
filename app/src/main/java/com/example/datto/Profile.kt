package com.example.datto

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.AccountResponse
import com.example.datto.GlobalVariable.GlobalVariable
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Locale
import com.squareup.picasso.Picasso

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profile : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var avatar: ImageView
    private lateinit var largeFullName: TextView
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var fullName: TextView
    private lateinit var dob: TextView

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.setIcon(R.drawable.ic_edit)
        menuItem.setOnMenuItemClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.app_fragment, ProfileEdit())
                .addToBackStack(null)
                .commit()
            true
        }

        appBar.title = "Profile"
        appBar.navigationIcon = null
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

        // Handle change password btn
        val changePassword = view.findViewById<Button>(R.id.profile_change_password)
        changePassword.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.app_fragment, ChangePassword())
                .addToBackStack(null)
                .commit()
        }

        // Assign id to each element
        avatar = requireView().findViewById(R.id.profile_avatar)
        largeFullName = requireView().findViewById(R.id.profile_fullName)
        username = requireView().findViewById(R.id.profile_account_info_username)
        email = requireView().findViewById(R.id.profile_account_info_email)
        fullName = requireView().findViewById(R.id.profile_profile_info_fullName)
        dob = requireView().findViewById(R.id.profile_profile_info_dob)

        // Get data
        APIService().doGet<AccountResponse>("accounts/${CredentialService().get()}", object : APICallback<Any> {
            override fun onSuccess(data: Any) {
                Log.d("API_SERVICE", "Data: $data")

                // Cast data to Account
                data as AccountResponse

                // Format date of birth
                val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                val targetFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val date = originalFormat.parse(data.profile.dob)

                // Set text to each element
                largeFullName.text = data.profile.fullName
                username.text = data.username
                email.text = data.email
                fullName.text = data.profile.fullName
                dob.text = targetFormat.format(date!!)

                // Load image with Picasso and new thread
                    Thread {
                        try {
                            activity?.runOnUiThread {
                                val imageUrl =
                                    if (data.profile.avatar != null) GlobalVariable.BASE_URL + "files/" + data.profile.avatar else null
                                if (imageUrl != null) {
                                    Picasso.get().load(imageUrl).into(avatar)
                                } else {
                                    Picasso.get().load(R.drawable.avatar).into(avatar)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }.start()
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })
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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Profile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}