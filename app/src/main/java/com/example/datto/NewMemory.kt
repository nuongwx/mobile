package com.example.datto

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.BucketResponse
import com.example.datto.DataClass.MemoryResponse
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.net.URL

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "groupId"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewMemory.newInstance] factory method to
 * create an instance of this fragment.
 */
data class Memory(val info: String, val image: URL)

class NewMemory : Fragment() {
    // TODO: Rename and change types of parameters
    private var groupId: String? = null
    private var param2: String? = null

    val imageUpload: ImageView by lazy { requireView().findViewById(R.id.newMemoryImageUpload) }
    val memoryInfo: TextInputEditText by lazy { requireView().findViewById(R.id.newMemoryNameEditText) }

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        // set transparent background
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.title = "Save"
        menuItem.setIcon(null)
        menuItem.isVisible = true
        menuItem.setOnMenuItemClickListener {
            if (imageUpload.drawable is VectorDrawable) {
                Toast.makeText(context, "Please upload an image", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener false
            }

            val bitmap = (imageUpload.drawable as BitmapDrawable).bitmap
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
                    val newGroupRequest = MemoryResponse(
                        data.id,
                        memoryInfo.text.toString()
                    )

                    APIService().doPost<MemoryResponse>(
                        "groups/$groupId/memories",
                        newGroupRequest,
                        object : APICallback<Any> {
                            override fun onSuccess(data: Any) {
                                Toast.makeText(context, "Memory created", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.popBackStack()
                            }

                            override fun onError(error: Throwable) {
                                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                }

                override fun onError(error: Throwable) {
                }
            })
            true
        }
        appBar.title = "Memories"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_memory, container, false)
    }

    override fun onResume() {
        super.onResume()
        configTopAppBar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configTopAppBar()

        Toast.makeText(context, groupId, Toast.LENGTH_SHORT).show()

        imageUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 42)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42 && resultCode == RESULT_OK) {
            val uri = data?.data
            imageUpload.setImageURI(uri)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewMemory.
         */ // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = NewMemory().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}