package com.example.datto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlanningEdit.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlanningEdit : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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

        val data: Planning? =
            Planning("Planning 1", "Description 1", Date(), Duration.ofMinutes(30))


        val textView: TextView = view.findViewById(R.id.totallyUniqueTextView)
        textView.text = arguments?.getString("id") ?: "No ID"

        val planningName: TextInputEditText = view.findViewById(R.id.planningNameEditText)
        val planningDescription: TextInputEditText =
            view.findViewById(R.id.planningDescriptionEditText)
        val planningStartTime: TextInputEditText = view.findViewById(R.id.planningStartTimeEditText)
        val planningEndTime: TextInputEditText = view.findViewById(R.id.planningEndTimeEditText)

        val startTime: Date
        val endTime: Date

        if (data != null) {
            planningName.setText(data.name)
            planningDescription.setText(data.description)
            startTime = data.start
            endTime = Date(data.start.time + data.duration.toMillis())

            // val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            // val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            planningStartTime.setText(formatter.format(startTime))
            planningEndTime.setText(formatter.format(endTime))
        }

        planningStartTime.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker().setTitleText("Start Date").build()
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
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build()
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