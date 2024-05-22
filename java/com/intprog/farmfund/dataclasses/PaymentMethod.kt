package com.intprog.farmfund.dataclasses

data class PaymentMethod (
    var paymethodId: String = "",
    var paymethodName: String = "",
    var paymethodLogo: String = "",
    var paymethodAccNumber: String = ""
)