package com.example.datto

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.BucketResponse
import com.example.datto.DataClass.EventResponse
import com.example.datto.DataClass.MemoryRequest
import com.example.datto.utils.FirebaseNotification
import com.example.datto.utils.WidgetUpdater
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "groupId"
private const val ARG_PARAM2 = "eventId"

/**
 * A simple [Fragment] subclass.
 * Use the [NewMemory.newInstance] factory method to
 * create an instance of this fragment.
 */

class NewMemory : Fragment() {
    private var groupId: String? = null
    private var eventId: String? = null

    val imageUpload: ImageView by lazy { requireView().findViewById(R.id.newMemoryImageUpload) }
    private lateinit var events: Map<String, String>
    private lateinit var createdAt: com.google.android.material.textfield.TextInputEditText

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

            if (eventId == null) {
                Toast.makeText(context, "Please select an event", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener false
            }

            val bitmap = (imageUpload.drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray = stream.toByteArray()

            APIService(requireContext()).doGet<BucketResponse>("groups/${groupId}/empty-memories-events", object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    data as BucketResponse
                    Log.d("API_SERVICE", "Data: $data")
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })

            // Get event name
            val eventName =
                view?.findViewById<MaterialAutoCompleteTextView>(R.id.create_memory_dropdown)?.text.toString()
            // if (eventId == null) {
            //     eventId = events.filterValues { it == eventName }.keys.first()
            // }

            // Create multipart body
            val requestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), byteArray)
            val multipartBody =
                MultipartBody.Part.createFormData("file", "thumbnail.jpg", requestBody)

            APIService(requireContext()).doPutMultipart<BucketResponse>(
                "files",
                multipartBody,
                object :
                    APICallback<Any> {
                    override fun onSuccess(data: Any) {
                        // Cast data to BucketResponse
                        data as BucketResponse

                        val originalFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
                        val targetFormat =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                        val tempFormatted =
                            originalFormat.parse(createdAt.text.toString())!!
                        val formattedCreatedAt = targetFormat.format(tempFormatted)

                        // Create new group with thumbnail
                        val newGroupRequest = MemoryRequest(
                            data.id,
                            eventName,
                            formattedCreatedAt
                        )

                        APIService(requireContext()).doPost<MemoryRequest>(
                            "events/$eventId/memories",
                            newGroupRequest,
                            object : APICallback<Any> {
                                override fun onSuccess(data: Any) {
                                    Toast.makeText(context, "Memory created", Toast.LENGTH_SHORT)
                                        .show()
                                    FirebaseNotification(requireContext()).compose(
                                        groupId!!,
                                        "A new memory has been created!",
                                        "Click to view? or something like that"
                                    )
                                    WidgetUpdater().update(requireContext())
                                    parentFragmentManager.popBackStack()
                                }

                                override fun onError(error: Throwable) {
                                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT)
                                        .show()
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
            eventId = it.getString(ARG_PARAM2)
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

        imageUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 42)
        }

        val eventList = ArrayList<EventResponse>()
        APIService(requireContext()).doGet<List<EventResponse>>(
            "groups/${groupId}/events",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")

                    data as List<EventResponse>

                    data.forEach {
                        if (it.memory == null) eventList.add(it)
                    }

                    val eventDropdown =
                        view.findViewById<MaterialAutoCompleteTextView>(R.id.create_memory_dropdown)
                    events = eventList.associate { it.id to it.name }
                    eventDropdown.setSimpleItems(events.values.toTypedArray())
                    eventDropdown.setOnItemClickListener { _, _, position, _ ->
                        eventId = events.keys.elementAt(position)
                    }
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })

        createdAt = view.findViewById(R.id.new_memory_created_at)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        createdAt.setText(current.format(formatter))

        createdAt.setOnClickListener {
            // Date Picker
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnPositiveButtonClickListener { dateLong ->
                // Time Picker
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build()
                timePicker.addOnPositiveButtonClickListener {
                    val date =
                        Instant.ofEpochMilli(dateLong).atZone(ZoneId.systemDefault()).toLocalDate()
                    val time = LocalTime.of(timePicker.hour, timePicker.minute)
                    val dateTime = LocalDateTime.of(date, time)
                    createdAt.setText(dateTime.format(formatter))
                }
                timePicker.show(parentFragmentManager, "time_picker")
            }
            datePicker.show(parentFragmentManager, "date_picker")
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
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) = NewMemory().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}