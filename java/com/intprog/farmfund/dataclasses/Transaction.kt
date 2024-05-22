package com.intprog.farmfund.dataclasses

import com.google.type.DateTime

data class Transaction(
    var transactionId: String = "",
    val userId: String = "",
    val projId: String = "",
    val voucherId: String = "",
    val transactionType: String = "",
    val transactionDateTime: com.google.firebase.Timestamp?, // Use Firestore's Timestamp
    val transactionStatus: String = ""
)