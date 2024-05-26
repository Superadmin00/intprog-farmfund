package com.intprog.farmfund.dataclasses

import java.util.Date

data class User(
    var userId: String? = null,
    var email: String? = null,
    var firstName: String? = null,
    var midName: String? = null,
    var lastName: String? = null,
    var birthDate: Date? = null,
    val address: Map<String, String>? = null,
    var phoneNum: String? = null,
    var fundPoints: Double = 0.0,
    var verified: Boolean = false,
    var status: String? = null,
    val favoriteProjects: ArrayList<Long> = ArrayList()
)