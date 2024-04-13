package com.example.datto

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.FundResponse
import com.example.datto.DataClassRecyclerView.FundItem
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FundListAdapter(private val funds: ArrayList<FundItem>) :
    RecyclerView.Adapter<FundListAdapter.FundViewHolder>() {

    inner class FundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fundDescription: TextView = itemView.findViewById(R.id.expense_card_description)
        val fundAmount: TextView = itemView.findViewById(R.id.expense_card_value)
        val fundUser: TextView = itemView.findViewById(R.id.expense_card_user)
        val fundDate: TextView = itemView.findViewById(R.id.expense_card_date)
        init {}
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): FundListAdapter.FundViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.expense_card, parent, false)
        return FundViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FundListAdapter.FundViewHolder, position: Int) {
        val currentItem = funds[position]
        holder.fundDescription.text = currentItem.description

        if (currentItem.amount < 0) {
            holder.fundAmount.text = currentItem.amount.toString()
            // Change color
            holder.fundAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.light_blue_600))
        } else {
            holder.fundAmount.text = "+" + currentItem.amount.toString()
            holder.fundAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.md_theme_primaryFixed_mediumContrast))
        }

        holder.fundUser.text = currentItem.user
        holder.fundDate.text = currentItem.date

        // Holder click listener
        holder.itemView.setOnClickListener{
            // Move to FundEdit
             (it.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.app_fragment, FundEdit(currentItem.id))
                .addToBackStack("FundList")
                .commit()
}
    }

    override fun getItemCount() = funds.size
}

/**
 * A simple [Fragment] subclass.
 * Use the [FundList.newInstance] factory method to
 * create an instance of this fragment.
 */
class FundList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = "6614a42b14e884a9d8eef02f"
    private var param2: String? = null

    private lateinit var shareMoneyBtn: com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

    private fun configTopAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        val menuItem = appBar.menu.findItem(R.id.edit)
        menuItem.isEnabled = true
        menuItem.title = null
        menuItem.setIcon(R.drawable.ic_create_black)
        menuItem.setOnMenuItemClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.app_fragment, NewFund("6614a42b14e884a9d8eef02f")).addToBackStack("FundList").commit()

            true
        }

        appBar.title = "Expense List"
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
        return inflater.inflate(R.layout.fragment_fund_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configTopAppBar()

        // Assign id for each element
        shareMoneyBtn = view.findViewById(R.id.fund_list_share_button)

        shareMoneyBtn.setOnClickListener{
            Toast.makeText(context, "Share Money", Toast.LENGTH_SHORT).show()
        }

        APIService().doGet<FundResponse>("events/$param1/funds",
            object: APICallback<Any> {
                override fun onSuccess(data: Any) {
                    Log.d("API_SERVICE", "Data: $data")

                    data as FundResponse

                    val fundList = ArrayList<FundItem>()

                    for (fund in data.funds) {
                        // Format paidAt date
                        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                        val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
                        val date = originalFormat.parse(fund.paidAt)

                        fundList.add(FundItem(fund.id, fund.info, fund.amount, fund.paidBy.profile.fullName, targetFormat.format(date!!)))
                    }

                    val recyclerView = view.findViewById<RecyclerView>(R.id.fund_list_list)
                    recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                    recyclerView.adapter = FundListAdapter(fundList)
                    recyclerView.setHasFixedSize(true)
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: $error")
                }
            }
        )
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
         * @return A new instance of fragment FundList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FundList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}