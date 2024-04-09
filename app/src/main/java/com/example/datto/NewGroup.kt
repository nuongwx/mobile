package com.example.datto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.BucketResponse
import com.example.datto.DataClass.NewGroupRequest
import com.example.datto.DataClass.NewGroupResponse
import com.google.android.material.appbar.MaterialToolbar
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewGroup.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewGroup : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val requestCode = 37

    private lateinit var name: com.google.android.material.textfield.TextInputEditText
    private lateinit var thumbnail: ImageView
    private var thumbnailChangeStatus: Boolean = false

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.title = "Create"
        menuItem.setIcon(null)
        menuItem.setOnMenuItemClickListener {
            Thread {
                try {
                    // Case 1: Create new group without thumbnail
                    if (!thumbnailChangeStatus) {
                        val newGroupRequest =
                            NewGroupRequest(CredentialService().get(), name.text.toString(), "")

                        APIService().doPost<NewGroupResponse>("groups", newGroupRequest, object :
                            APICallback<Any> {
                            override fun onSuccess(data: Any) {
                                Log.d("API_SERVICE", "Data: $data")

                                // Cast data to NewGroupResponse
                                data as NewGroupResponse

                                // Move to NewGroupInviteCode fragment
                                val newGroupInviteCode =
                                    NewGroupInviteCode(name.text.toString(), data.inviteCode)
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.app_fragment, newGroupInviteCode)
                                    .addToBackStack("NewGroupInviteCode")
                                    .commit()
                            }

                            override fun onError(error: Throwable) {
                                Log.e("API_SERVICE", "Error: ${error.message}")
                            }
                        })
                    } else {
                        // Case 2: Create new group with thumbnail
                        // Get image from thumbnail
                        val bitmap = (thumbnail.drawable as BitmapDrawable).bitmap
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        val byteArray = stream.toByteArray()

                        // Create multipart body
                        val requestBody =
                            RequestBody.create(MediaType.parse("multipart/form-data"), byteArray)
                        val multipartBody =
                            MultipartBody.Part.createFormData("file", "thumbnail.jpg", requestBody)

                        APIService().doPutMultipart<BucketResponse>("files", multipartBody, object :
                            APICallback<Any> {
                            override fun onSuccess(data: Any) {
                                // Cast data to BucketResponse
                                data as BucketResponse

                                // Create new group with thumbnail
                                val newGroupRequest = NewGroupRequest(
                                    CredentialService().get(),
                                    name.text.toString(),
                                    data.id
                                )

                                // Call API to patch profile
                                APIService().doPost<NewGroupResponse>(
                                    "groups",
                                    newGroupRequest,
                                    object :
                                        APICallback<Any> {
                                        override fun onSuccess(data: Any) {
                                            Log.d("API_SERVICE", "Data: $data")

                                            // Cast data to NewGroupResponse
                                            data as NewGroupResponse

                                            // Move to NewGroupInviteCode fragment
                                            val newGroupInviteCode = NewGroupInviteCode(
                                                name.text.toString(),
                                                data.inviteCode
                                            )
                                            requireActivity().supportFragmentManager.beginTransaction()
                                                .replace(R.id.app_fragment, newGroupInviteCode)
                                                .commit()
                                        }

                                        override fun onError(error: Throwable) {
                                            Log.e("API_SERVICE", "Error: ${error.message}")
                                        }
                                    })

                                Log.d("API_SERVICE", "ProfileEditRequest: $data")
                            }

                            override fun onError(error: Throwable) {
                                Log.e("API_SERVICE", "Error: ${error.message}")
                            }
                        })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("API_SERVICE", "Error: ${e.message}")
                }
            }.start()

            true
        }

        appBar.title = "New Group"
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

        // Assign id to each element
        name = view.findViewById(R.id.new_group_group_name)
        thumbnail = view.findViewById(R.id.new_group_image)

        // Change thumbnail option
        thumbnail.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, this.requestCode);
        }
    }

    private var isFirstCall = true
    override fun onResume() {
        super.onResume()
        configTopAppBar()

        if (isFirstCall) {
            isFirstCall = false

        } else {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK && data != null) {
            Picasso.get().load(data.data).into(thumbnail)
            thumbnailChangeStatus = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_group, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewGroup.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewGroup().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}