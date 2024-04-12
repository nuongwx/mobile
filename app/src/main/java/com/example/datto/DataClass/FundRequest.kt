package com.example.datto.DataClass

data class FundRequest(
    val paidBy: String,
    val amount: Double,
    val info: String,
    val paidAt: String,
)