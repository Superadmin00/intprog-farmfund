package com.intprog.farmfund.dataclasses

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.*

data class Project(
    val projId: String = "",
    val userId: String = "",
    val projTitle: String = "",
    val projDescription: String = "",
    val projMilestone: String = "",
    val projDonorsCount: Int = 0,
    val projFundGoal: Double = 0.0,
    val projFundsReceived: Double = 0.0,
    val projDueDate: Date? = null,
    val projStatus: String = "",
    val imageUrls: List<String> = emptyList(),
    val timestamp: Timestamp? = null // Add this line
): Serializable {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        0,
        0.0,
        0.0,
        null,
        "",
        emptyList(),
        null
    )
}