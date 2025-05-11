package com.example.sikka_grphsfixed

data class Card(
    val amount: String = "",     // <-- Add default empty string
    val date: String? = null,
    val notes: String? = null,
    val accType: String? = null,
    val category: String? = null,
    val type: String? = null,
    val icon: Int? = null
)
