package com.intprog.farmfund.dataclasses

import com.google.firebase.Timestamp

data class Transaction(
    var transactionId: String = "",
    val userId: String = "",
    val projId: String = "",
    val voucherId: String = "",
    val transactionType: String = "",
    val transactionAmount: Int? = null,
    val paymentMethod: String = "",
    val transactionDateTime: Timestamp? = null, // Use Firestore's Timestamp
    val transactionStatus: String = ""
)