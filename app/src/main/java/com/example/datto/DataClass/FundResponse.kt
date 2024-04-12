package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName
import java.util.Date

data class FundResponseUnit (
    @SerializedName("_id")
    var id: String,
    var paidBy: AccountResponse,
    var amount: Double,
    var info: String,
    var paidAt: String,
)

data class FundResponse (
    var funds: List<FundResponseUnit>
)