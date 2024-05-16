package com.intprog.farmfund.dataclasses

data class User (
    var userId: String?,
    var email: String?,
    var phoneNum: String? = null,
    var name: String,
    var fundPoints: Double,
    var verified: Boolean,
    var status: String
)