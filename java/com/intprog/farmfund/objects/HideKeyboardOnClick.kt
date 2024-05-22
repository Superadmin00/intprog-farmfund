package com.intprog.farmfund.objects

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object HideKeyboardOnClick {
    // Function to hide keyboard
    fun hideKeyboardOnClick(view: View) {
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}