package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

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

data class FundRequest(
    val paidBy: String,
    val amount: Double,
    val info: String,
    val paidAt: String,
)