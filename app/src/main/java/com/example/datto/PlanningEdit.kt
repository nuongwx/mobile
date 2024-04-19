package com.example.datto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.Planning
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "eventId"
private const val ARG_PARAM2 = "id"

/**
 * A simple [Fragment] subclass.
 * Use the [PlanningEdit.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlanningEdit : Fragment() {
    private var eventId: String? = null
    private var planningId: String? = null

    val planningName: TextInputEditText by lazy { view?.findViewById(R.id.planningNameEditText)!! }
    val planningDescription: TextInputEditText by lazy { view?.findViewById(R.id.planningDescriptionEditText)!! }
    val planningStartTime: TextInputEditText by lazy { view?.findViewById(R.id.planningStartTimeEditText)!! }
    val planningEndTime: TextInputEditText by lazy { view?.findViewById(R.id.planningEndTimeEditText)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
            planningId = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_planning_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (planningId != null && planningId!!.isNotEmpty()) {
            APIService().doGet<Planning>("events/${arguments?.getString("eventId")}/timeline/${
                arguments?.getString(
                    "id"
                )
            }", object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    data as Planning

                    planningName.setText(data.name)
                    planningDescription.setText(data.description)

                    val originalFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
                    val startTime = originalFormat.parse(data.start)
                    val endTime = originalFormat.parse(data.end)

                    planningStartTime.setText(targetFormat.format(startTime))
                    planningEndTime.setText(targetFormat.format(endTime))
                }

                override fun onError(error: Throwable) {
                    println(error)
                }
            })
        }

        planningStartTime.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker().setTitleText("Start Date").build()
            datePicker.addOnPositiveButtonClickListener { dateLong ->
                // Time Picker
                val timePicker =
                    MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).build()
                timePicker.addOnPositiveButtonClickListener {
                    val date =
                        Instant.ofEpochMilli(dateLong).atZone(ZoneId.systemDefault()).toLocalDate()
                    val time = LocalTime.of(timePicker.hour, timePicker.minute)
                    val dateTime = LocalDateTime.of(date, time)
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    planningStartTime.setText(dateTime.format(formatter))

                    if (planningEndTime.text?.isEmpty() == true) {
                        planningEndTime.callOnClick()
                    }

                }
                timePicker.show(parentFragmentManager, null)
            }
            datePicker.show(parentFragmentManager, null)
        }

        planningEndTime.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker().setTitleText("End Date").build()
            datePicker.addOnPositiveButtonClickListener { dateLong ->
                // Time Picker
                val timePicker =
                    MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).build()
                timePicker.addOnPositiveButtonClickListener {
                    val date =
                        Instant.ofEpochMilli(dateLong).atZone(ZoneId.systemDefault()).toLocalDate()
                    val time = LocalTime.of(timePicker.hour, timePicker.minute)
                    val dateTime = LocalDateTime.of(date, time)
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    planningEndTime.setText(dateTime.format(formatter))
                }
                timePicker.show(parentFragmentManager, null)
            }
            datePicker.show(parentFragmentManager, null)
        }

        configTopAppBar()
    }

    private fun save() {
        val name = planningName.text.toString()
        val description = planningDescription.text.toString()
        val startTime = planningStartTime.text.toString()
        val endTime = planningEndTime.text.toString()

        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val targetFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val start = formatter.parse(startTime)!!.let { targetFormat.format(it) }
        val end = formatter.parse(endTime)!!.let { targetFormat.format(it) }

        val planning = Planning(name, description, start, end)

        if (planningId == null) {
            APIService().doPost<Any>("events/${eventId}/timeline",
                planning,
                object : APICallback<Any> {
                    override fun onSuccess(data: Any) {
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()

                        parentFragmentManager.popBackStack()
                    }

                    override fun onError(error: Throwable) {
                        println(error)
                    }
                })
        } else {
            APIService().doPatch<Any>("events/${eventId}/timeline/${planningId}",
                planning,
                object : APICallback<Any> {
                    override fun onSuccess(data: Any) {
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()

                        parentFragmentManager.popBackStack()
                    }

                    override fun onError(error: Throwable) {
                        println(error)
                    }
                })
        }
    }

    fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.title = "Save"
        menuItem.setIcon(null)
        menuItem.setOnMenuItemClickListener {
            save()
            true
        }

        appBar.title = "Plannings"
        appBar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlanningEdit.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = PlanningEdit().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}