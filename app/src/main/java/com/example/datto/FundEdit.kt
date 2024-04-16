package com.example.datto

import NumberTextWatcher
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.EventMemberResponse
import com.example.datto.DataClass.FundRequest
import com.example.datto.DataClass.FundResponseUnit
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FundEdit.newInstance] factory method to
 * create an instance of this fragment.
 */

class FundEdit(
    id: String,
) : Fragment() {
    // TODO: Rename and change types of parameters
    private var id: String? = id

    private lateinit var description: com.google.android.material.textfield.TextInputEditText
    private lateinit var type: MaterialAutoCompleteTextView
    private lateinit var paidBy: MaterialAutoCompleteTextView
    private lateinit var amount: com.google.android.material.textfield.TextInputEditText
    private lateinit var paidAt: com.google.android.material.textfield.TextInputEditText

    fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.title = "Save"
        menuItem.setIcon(null)
        menuItem.setOnMenuItemClickListener(null)

        appBar.title = "Edit Fund"
        appBar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fund_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configTopAppBar()

        // Assign id to each element
        description = view.findViewById(R.id.fund_edit_description)
        type = view.findViewById(R.id.fund_edit_type)
        paidBy = view.findViewById(R.id.fund_edit_paid_by)
        amount = view.findViewById(R.id.fund_edit_amount)
        paidAt = view.findViewById(R.id.fund_edit_paid_at)
    }

    override fun onResume() {
        super.onResume()
        configTopAppBar()

        // Set type dropdown
        val typeItems = arrayOf("Add fund", "Expense")
        type.setSimpleItems(typeItems)

        APIService().doGet<FundResponseUnit>("funds/$id",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")

                    val outerData = data as FundResponseUnit

                    APIService().doGet<EventMemberResponse>("funds/$id/members",
                        object : APICallback<Any> {
                            override fun onSuccess(data: Any) {
                                Log.d("API_SERVICE", "Data: $data")

                                data as EventMemberResponse

                                // Set data in inner scope
                                description.setText(outerData.info)
                                amount.setText(outerData.amount.absoluteValue.toString())

                                // Parse the date string into a Date object
                                val originalFormat =
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                val dateObject = originalFormat.parse(outerData.paidAt)

                                // Format the Date object into the desired format
                                val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
                                val formattedDate = targetFormat.format(dateObject!!)

                                // Set value for paidAt field
                                paidAt.setText(formattedDate)

                                val paidByItems =
                                    data.members.associate { it.id to it.profile.fullName }
                                paidBy.setSimpleItems(paidByItems.values.toTypedArray())

                                // Set default value for paidBy to "Budget" when type is "Expense"
                                type.addTextChangedListener(object: TextWatcher {
                                    override fun afterTextChanged(s: Editable?) {
                                        if (s.toString() == typeItems[0]) {
                                            // Enable paidBy_title dropdown
                                            requireActivity().findViewById<TextInputLayout>(R.id.fund_edit_paid_by_title).isEnabled = true

                                            // Set paidBy to default value
                                            paidBy.setSimpleItems(paidByItems.values.toTypedArray())

                                            // Clear paidBy text
                                            paidBy.setText("")
                                        } else {
                                            // Disable paidBy_title dropdown
                                            requireActivity().findViewById<TextInputLayout>(R.id.fund_edit_paid_by_title).isEnabled = false

                                            // Set paidBy to "Budget" text
                                            paidBy.setText("Budget")
                                        }
                                    }

                                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                                        // Do nothing
                                    }

                                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                        // Do nothing
                                    }
                                })

                                type.setText(
                                    if (outerData.amount > 0) typeItems[0] else typeItems[1],
                                    false
                                )

                                // Set value for paidBy dropdown
                                if (paidBy.text.toString() != "Budget")
                                    paidBy.setText(paidByItems[outerData.paidBy.id], false)

                                // Set onClickListener for the top bar button
                                val appBar =
                                    requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
                                val menuItem = appBar.menu.findItem(R.id.edit)
                                menuItem.setOnMenuItemClickListener {
                                    // Check all fields are filled
                                    if ((paidBy.text.toString() == "" && type.text.toString() !== typeItems[1]) || amount.text.toString() == "" || description.text.toString() == "" || paidAt.text.toString() == "") {
                                        Toast.makeText(
                                            context,
                                            "Please fill all fields",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@setOnMenuItemClickListener true
                                    }

                                    // Reformat paidAt from dd/MM/yyyy HH:mm to yyyy-MM-ddTHH:mm
                                    val originalFormatField =
                                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
                                    val targetFormatMongo =
                                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                    val tempFormatted =
                                        originalFormatField.parse(paidAt.text.toString())!!
                                    val formattedPaidAt = targetFormatMongo.format(tempFormatted)

                                    val fundRequest = FundRequest(
                                        paidBy = if (paidBy.text.toString() == "Budget") "" else
                                            paidByItems.filterValues { it == paidBy.text.toString() }.keys.first(),
                                        amount = if (type.text.toString() == typeItems[0]) {
                                            amount.text.toString().replace(",", "").toDouble()
                                        } else {
                                            -amount.text.toString().replace(",", "").toDouble()
                                        },
                                        info = description.text.toString(),
                                        paidAt = formattedPaidAt,
                                    )

                                    APIService().doPatch<Any>("funds/$id", fundRequest, object :
                                        APICallback<Any> {
                                        override fun onSuccess(data: Any) {
                                            Log.d("API_SERVICE", "Data: $data")
                                            Toast.makeText(
                                                context,
                                                "Fund updated",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            requireActivity().supportFragmentManager.popBackStack()
                                        }

                                        override fun onError(error: Throwable) {
                                            Log.e("API_SERVICE", "Error: $error")
                                        }
                                    })

                                    true
                                }
                            }

                            override fun onError(error: Throwable) {
                                Log.e("API_SERVICE", "Error: $error")
                            }
                        }
                    )
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: $error")
                }
            }
        )

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
                    val date =
                        Instant.ofEpochMilli(dateLong).atZone(ZoneId.systemDefault()).toLocalDate()
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
         * @return A new instance of fragment FundEdit.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            FundEdit(param1).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}