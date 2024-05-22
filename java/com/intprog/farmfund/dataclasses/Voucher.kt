package com.intprog.farmfund.dataclasses

data class Voucher (
    var voucherId: String = "",
    var voucherBrand: String = "",
    var voucherLogo: String = "",
    var voucherPoints: Int = 0,
    var voucherReward: Int = 0,
    var voucherType: String = ""
)