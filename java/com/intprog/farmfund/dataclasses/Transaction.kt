package com.intprog.farmfund.dataclasses

import com.google.type.DateTime

data class Transaction(
    val transactionId: String,
    val userId: String,
    val projId: String,
    val voucherId: String,
    val transactionType: String,
    val transactionDateTime: DateTime,
    val transactionStatus: String
)