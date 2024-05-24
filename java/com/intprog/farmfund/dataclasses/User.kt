package com.intprog.farmfund.dataclasses

import java.util.Date

data class User(
    var userId: String?,
    var email: String?,
    var firstName: String?,
    var midName: String?,
    var lastName: String?,
    var birthDate: Date?,
    val address: Map<String, String>?,
    var phoneNum: String? = null,
    var fundPoints: Double,
    var verified: Boolean,
    var status: String?,
    val favoriteProjects: ArrayList<Long> = ArrayList()
)