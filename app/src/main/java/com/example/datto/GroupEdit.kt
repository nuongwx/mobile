package com.example.datto

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.BucketResponse
import com.example.datto.DataClass.GroupEditRequest
import com.example.datto.GlobalVariable.GlobalVariable
import com.google.android.material.appbar.MaterialToolbar
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream

private const val ARG_PARAM1 = "groupId"
private const val ARG_PARAM2 = "groupName"
private const val ARG_PARAM3 = "thumbnail"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupEdit.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupEdit : Fragment() {
    private var groupId: String? = null
    private var groupName: String? = null
    private var thumbnail: String? = null
    private val requestCode = 70

    private lateinit var editGroupName: com.google.android.material.textfield.TextInputEditText
    private lateinit var editGroupThumbnail: ImageView
    private var avatarChangeStatus: Boolean = false

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isVisible = true
        menuItem.isEnabled = true
        menuItem.title = "Save"
        menuItem.setIcon(null)
        menuItem.setOnMenuItemClickListener {
            Thread {
                try {
                    // Case 1: Do not change thumbnail
                    if (!avatarChangeStatus) {
                        // Get text from each element
                        val groupEditRequest = GroupEditRequest(
                            editGroupName.text.toString(),
                            ""
                        )

                        // Patch group
                        APIService(requireContext()).doPatch<GroupEditRequest>(
                            "groups/${CredentialService().get()}",
                            groupEditRequest,
                            object :
                                APICallback<Any> {
                                override fun onSuccess(data: Any) {
                                    Log.d("API_SERVICE", "Data: $data")
                                    Toast.makeText(
                                        requireContext(),
                                        "Group updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Back to previous fragment
                                    parentFragmentManager.popBackStack()
                                }

                                override fun onError(error: Throwable) {
                                    Log.e("API_SERVICE", "Error: ${error.message}")
                                }
                            })
                    } else {
                        // Case 2: Change thumbnail
                        // Get image from thumbnail
                        val bitmap = (editGroupThumbnail.drawable as BitmapDrawable).bitmap
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        val byteArray = stream.toByteArray()

                        // Create multipart body
                        val requestBody =
                            RequestBody.create(MediaType.parse("multipart/form-data"), byteArray)
                        val multipartBody =
                            MultipartBody.Part.createFormData(
                                "file",
                                "thumbnail_${groupName}.jpg",
                                requestBody
                            )

                        APIService(requireContext()).doPutMultipart<BucketResponse>("files", multipartBody, object :
                            APICallback<Any> {
                            override fun onSuccess(data: Any) {
                                // Cast data to BucketResponse
                                data as BucketResponse

                                // Patch group with new avatar
                                // Get text from each element
                                val groupEditRequest = GroupEditRequest(
                                    editGroupName.text.toString(),
                                    data.id
                                )

                                // Call API to patch profile
                                APIService(requireContext()).doPatch<GroupEditRequest>(
                                    "groups/${groupId}",
                                    groupEditRequest,
                                    object :
                                        APICallback<Any> {
                                        override fun onSuccess(data: Any) {
                                            Log.d("API_SERVICE", "Data: $data")
                                            Toast.makeText(
                                                requireContext(),
                                                "Group updated successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Back to previous fragment
                                            parentFragmentManager.popBackStack()
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

        appBar.title = "Edit group"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString(ARG_PARAM1)
            groupName = it.getString(ARG_PARAM2)
            thumbnail = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configTopAppBar()

        editGroupName =
            view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_group_name)
        editGroupThumbnail =
            view.findViewById<ImageView>(R.id.edit_group_image)


        editGroupName.setText(groupName)
        // Load image with Picasso and new thread
        Thread {
            try {
                activity?.runOnUiThread {
                    val imageUrl =
                        if (thumbnail != "") GlobalVariable.BASE_URL + "files/" + thumbnail else null
                    if (imageUrl != null) {
                        Picasso.get().load(imageUrl).into(editGroupThumbnail)
                    } else {
                        Picasso.get().load(R.drawable.cover).into(editGroupThumbnail)
                    }
                    val layoutParams = editGroupThumbnail.layoutParams
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    val density = resources.displayMetrics.density
                    layoutParams.height = (200 * density).toInt()
                    editGroupThumbnail.layoutParams = layoutParams
                    editGroupThumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        editGroupThumbnail.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, this.requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == this.requestCode && resultCode == -1 && data != null) {
            Picasso.get().load(data.data).into(editGroupThumbnail)
            avatarChangeStatus = true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String, param3: String) =
            GroupEdit().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                }
            }
    }
}