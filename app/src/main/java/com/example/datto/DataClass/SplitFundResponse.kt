package com.example.datto.DataClass


data class SplitFundResponseUnit (
    var account: AccountResponse,
    var amount: Double
)
data class SplitFundResponse (
    var data: List<SplitFundResponseUnit>,
    var remainingFunds: Float
)