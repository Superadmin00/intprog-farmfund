package com.intprog.farmfund.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Date

class FarmerVerifViewModel : ViewModel() {

    // For the first fragment
    var firstName = MutableLiveData<String>()
    val midName = MutableLiveData<String>()
    val lastName = MutableLiveData<String>()
    val province = MutableLiveData<String>()
    val municipality = MutableLiveData<String>()
    val barangay = MutableLiveData<String>()
    val zipCode = MutableLiveData<String>()
    val phoneNum = MutableLiveData<String>()
    val birthDate = MutableLiveData<Date>()
    val selfieImage = MutableLiveData<Bitmap>()

    // For the second fragment
    val farmLocation = MutableLiveData<String>()
    val farmArea = MutableLiveData<String>()
    val farmType = MutableLiveData<String>()
    val farmImages = MutableLiveData<MutableList<Bitmap>>(mutableListOf())

    // For the third fragment
    val barangayCertImageUri = MutableLiveData<Uri>()
    val validIDImageUri = MutableLiveData<Uri>()
    val landOwnershipLeaseImageUri = MutableLiveData<Uri>()
    val rsbsaImageUri = MutableLiveData<Uri>()
}

