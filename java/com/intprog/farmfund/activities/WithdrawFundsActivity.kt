package com.intprog.farmfund.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.BankInfoAdapter
import com.intprog.farmfund.dataclasses.BankInfo

class WithdrawFundsActivity : AppCompatActivity() {

    private lateinit var bankRecyclerView: RecyclerView
    private val bankList = listOf(
        BankInfo(R.drawable.ic_gcashlogo, "**** **** **** 1999", "Expires 12/26"),
        BankInfo(R.drawable.ic_gcashlogo, "**** **** **** 2024", "Expires 04/20"),
        BankInfo(R.drawable.ic_gcashlogo, "**** **** **** 3782", "Expires 09/11")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw_funds)

        bankRecyclerView = findViewById(R.id.bankRecyclerView)
        val adapter = BankInfoAdapter(bankList)
        bankRecyclerView.layoutManager = LinearLayoutManager(this)
        bankRecyclerView.adapter = adapter

        val withdrawButton = findViewById<Button>(R.id.withdrawButton)
        withdrawButton.setOnClickListener {
            val dialog = WithdrawalSuccessDialog(this)
            dialog.show()
        }
    }

    class WithdrawalSuccessDialog(context: Context) : Dialog(context) {
        init {
            // Set custom dialog properties
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_withdrawal_succesful)

            val closeDialogButton = findViewById<Button>(R.id.closeDialogBTN)
            closeDialogButton.setOnClickListener {
                // Close the dialog
                dismiss()

                // Start NavigatorActivity
                val intent = Intent(context, NavigatorActivity::class.java)
                context.startActivity(intent)

                // If you want to finish the current activity (optional)
                if (context is Activity) {
                    context.finish()
                }
            }
        }
    }
}