package com.intprog.farmfund.dataclasses

data class Voucher (
    var voucherId: Int,
    var voucherPoints: Int,
    var voucherLogo: Int,
    var voucherType: String,
    var isChecked: Boolean = false
)