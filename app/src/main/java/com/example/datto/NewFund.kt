package com.example.datto

import NumberTextWatcher
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.EventMemberResponse
import com.example.datto.DataClass.FundRequest
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewFund.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewFund (
    eventId: String
) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = eventId
    private var param2: String? = null

    private lateinit var description: com.google.android.material.textfield.TextInputEditText
    private lateinit var type: MaterialAutoCompleteTextView
    private lateinit var paidBy: MaterialAutoCompleteTextView
    private lateinit var amount: com.google.android.material.textfield.TextInputEditText
    private lateinit var paidAt: com.google.android.material.textfield.TextInputEditText

    fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.title = null
        menuItem.setIcon(R.drawable.ic_create_black)
        menuItem.setOnMenuItemClickListener(null)

        appBar.title = "New Fund"
        appBar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_fund, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configTopAppBar()

        // Assign id to each element
        description = view.findViewById(R.id.new_fund_description)
        type = view.findViewById(R.id.new_fund_type)
        paidBy = view.findViewById(R.id.new_fund_paid_by)
        amount = view.findViewById(R.id.new_fund_amount)
        paidAt = view.findViewById(R.id.new_fund_paid_at)
    }

    override fun onResume() {
        super.onResume()
        configTopAppBar()

        // Set type dropdown
        val typeItems = arrayOf("Add fund", "Expense")
        type.setSimpleItems(typeItems)

        // Set paidBy dropdown
        APIService().doGet<EventMemberResponse>("events/${param1}/members", object :
            APICallback<Any> {
            override fun onSuccess(data: Any) {
                data as EventMemberResponse
                Log.d("API_SERVICE", "Data: $data")

                val members = data.members.toTypedArray().toMutableList()

                // Set value for paidBy dropdown
                // key = id, value = fullName
                val paidByItems = members.associate { it.id to it.profile.fullName }
                paidBy.setSimpleItems(paidByItems.values.toTypedArray())

                // Override add button
                val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
                val menuItem = appBar.menu.findItem(R.id.edit)
                menuItem.setOnMenuItemClickListener {
                    // Check all fields are filled
                    if (paidBy.text.toString() == "" || amount.text.toString() == "" || description.text.toString() == "" || paidAt.text.toString() == "") {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }

                    // Reformat paidAt from dd/MM/yyyy HH:mm to yyyy-MM-ddTHH:mm
                    val originalFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
                    val targetFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    val tempFormatted =
                        originalFormat.parse(paidAt.text.toString())!!
                    val formattedPaidAt = targetFormat.format(tempFormatted)

                    val fundRequest = FundRequest(
                        paidBy = paidByItems.filterValues { it == paidBy.text.toString() }.keys.first(),
                        amount = if (type.text.toString() == typeItems[0]) {
                            amount.text.toString().replace(",", "").toDouble()
                        } else {
                            -amount.text.toString().replace(",", "").toDouble()
                        },
                        info = description.text.toString(),
                        paidAt = formattedPaidAt,
                    )

                    APIService().doPost<Any>("events/$param1/funds", fundRequest, object :
                        APICallback<Any> {
                        override fun onSuccess(data: Any) {
                            Log.d("API_SERVICE", "Data: $data")
                            Toast.makeText(context, "Fund added", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }

                        override fun onError(error: Throwable) {
                            Log.d("API_SERVICE", "Error: $error")
                            Toast.makeText(context, "Failed to add fund", Toast.LENGTH_SHORT).show()
                        }
                    })

                    true
                }
            }

            override fun onError(error: Throwable) {
                Log.d("API_SERVICE", "Error: $error")
                Toast.makeText(context, "Failed to get members", Toast.LENGTH_SHORT).show()
            }
        })

        // Auto add . every 3 digits for amount
        amount.addTextChangedListener(NumberTextWatcher(amount))

        // Set onClick for paidAt
        paidAt.setOnClickListener {
            // Date Picker
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnPositiveButtonClickListener { dateLong ->
                // Time Picker
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build()
                timePicker.addOnPositiveButtonClickListener {
                    val date = Instant.ofEpochMilli(dateLong).atZone(ZoneId.systemDefault()).toLocalDate()
                    val time = LocalTime.of(timePicker.hour, timePicker.minute)
                    val dateTime = LocalDateTime.of(date, time)
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    paidAt.setText(dateTime.format(formatter))
                }
                timePicker.show(parentFragmentManager, "time_picker")
            }
            datePicker.show(parentFragmentManager, "date_picker")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewFund.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewFund(param1).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}