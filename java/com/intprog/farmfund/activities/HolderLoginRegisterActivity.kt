package com.intprog.farmfund.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.intprog.farmfund.R
import com.intprog.farmfund.fragments.LoginBottomSheetDialogFragment
import com.intprog.farmfund.fragments.RegisterBottomSheetDialogFragment

class HolderLoginRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_holder_login_regis)

        val dialogToShow = intent.getStringExtra("dialogToShow")

        if (dialogToShow == "login") {
            // Show the login page
            val loginBottomSheetDialogFragment = LoginBottomSheetDialogFragment()
            loginBottomSheetDialogFragment.show(supportFragmentManager, loginBottomSheetDialogFragment.tag)
        } else if (dialogToShow == "register") {
            // Show the register page
            val registerBottomSheetDialogFragment = RegisterBottomSheetDialogFragment()
            registerBottomSheetDialogFragment.show(supportFragmentManager, registerBottomSheetDialogFragment.tag)
        }
    }
}