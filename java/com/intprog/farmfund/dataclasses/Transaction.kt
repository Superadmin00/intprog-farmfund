package com.intprog.farmfund.dataclasses

data class Transaction(
    val icon: Int, // Resource ID for the transaction icon (e.g., drawable resource)
    val title: String, // Title of the transaction
    val type: String, // Transaction type (e.g., Deposit, Withdrawal, Transfer)
    val date: String, // Formatted date string (e.g., "MM/dd/yyyy")
    val time: String // Formatted time string (e.g., "hh:mm a")
)