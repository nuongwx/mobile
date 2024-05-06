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
import com.example.datto.utils.FirebaseNotification
import com.example.datto.utils.WidgetUpdater
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val ARG_PARAM1 = "groupId"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Create.newInstance] factory method to
 * create an instance of this fragment.
 */

data class EventRequest(
    var name: String,
    var start_date: String,
    var end_date: String,
)

class Create : Fragment() {
    // TODO: Rename and change types of parameters
    private var groupId: String? = null
    private var param2: String? = null

    private lateinit var groups: Map<String, String>
    private lateinit var startDate: TextInputEditText
    private lateinit var endDate: TextInputEditText

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.isVisible = true
        menuItem.setIcon(null)
        menuItem.title = "Save"
        appBar.title = "New event"

        menuItem.setOnMenuItemClickListener {
            val name = view?.findViewById<TextInputEditText>(R.id.create_name)?.text.toString()
            val startDateValue = startDate.text.toString()
            val endDateValue = endDate.text.toString()

            // Check name is not empty
            if (name == "") {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }

            // Check start date is not empty
            if (startDateValue == "") {
                Toast.makeText(requireContext(), "Start date cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }

            // Check end date is not empty
            if (endDateValue == "") {
                Toast.makeText(requireContext(), "End date cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }

            val groupName = view?.findViewById<MaterialAutoCompleteTextView>(R.id.create_group_dropdown)?.text.toString()
            if (groupName == "") {
                Toast.makeText(requireContext(), "Group cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }

            val group =
                groups.filterValues { it == groupName }.keys.first()

            // Format date
            val originalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val targetFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val formattedStart =
                originalFormat.parse(startDateValue)!!.let { targetFormat.format(it) }
            val formattedEnd = originalFormat.parse(endDateValue)!!.let { targetFormat.format(it) }

            // Format date before
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val originalDate = LocalDate.parse(startDateValue, formatter)
            val newFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val oneDayBefore = originalDate.minusDays(1)
            val formattedDate = oneDayBefore.atStartOfDay().format(newFormatter)


            val data = mapOf(
                "name" to name, "start" to formattedStart, "end" to formattedEnd
            )

            APIService(requireContext()).doPost<EventRequest>(
                "groups/${group}/events",
                data,
                object : APICallback<Any> {
                    override fun onSuccess(data: Any) {
                        if (groupId != null) {
                            // Update widget
                            WidgetUpdater().update(requireContext())

                            requireActivity().supportFragmentManager.popBackStack()
                            return
                        }

                        FirebaseNotification(requireContext()).compose(
                            group,
                            "Get ready for $name",
                            "$name will take place on $startDateValue",
                            formattedDate
                        )

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.app_fragment, GroupList()).addToBackStack("GroupList")
                            .commit()

                        Log.d("API_SERVICE", "Data: $data")
                    }

                    override fun onError(error: Throwable) {
                        Log.e("API_SERVICE", "Error: ${error.message}")
                    }
                })
            true
        }
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
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configTopAppBar()

        val groupList = ArrayList<CustomGroupResponse>()

        APIService(requireContext()).doGet<List<CustomGroupResponse>>("accounts/${CredentialService().get()}/groups",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")

                    data as List<CustomGroupResponse>

                    data.forEach {
                        groupList.add(it)
                    }

                    val groupSelect =
                        view.findViewById<MaterialAutoCompleteTextView>(R.id.create_group_dropdown)
                    groups = groupList.associate { it.id to it.name }
                    groupSelect.setSimpleItems(groups.values.toTypedArray())

                    // If groupId is defined, select the corresponding group and disable groupSelect
                    groupId?.let {
                        val groupName = groups[it]
                        groupName?.let { name ->
                            val index = groups.values.indexOf(name)
                            groupSelect.setText(name, false)
                            requireActivity().findViewById<TextInputLayout>(R.id.create_group_dropdown_title).isEnabled = false
                        }
                    }
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                }
            })

        startDate = view.findViewById(R.id.create_start_date)
        endDate = view.findViewById(R.id.create_end_date)

        startDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnPositiveButtonClickListener {
                val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                startDate.setText(date.format(formatter))
            }
            datePicker.show(parentFragmentManager, "date_picker")
        }

        endDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnPositiveButtonClickListener {
                val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                endDate.setText(date.format(formatter))
            }
            datePicker.show(parentFragmentManager, "date_picker")
        }
    }

    override fun onResume() {
        super.onResume()

        configTopAppBar()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Create.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = Create().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}