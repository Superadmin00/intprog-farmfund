package com.intprog.farmfund.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R
import com.intprog.farmfund.activities.HolderLoginRegisterActivity
import com.intprog.farmfund.databinding.ItemVoucherBinding
import com.intprog.farmfund.dataclasses.Transaction
import com.intprog.farmfund.dataclasses.Voucher
import com.intprog.farmfund.fragments.VouchersCenterFragment
import com.intprog.farmfund.objects.LoadingDialog

class VoucherAdapter(
    private val vouchers: List<Voucher>,
    private val fragment: VouchersCenterFragment
) :
    RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val binding = ItemVoucherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VoucherViewHolder(binding, parent.context, fragment) // Pass the context here
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucher = vouchers[position]
        holder.bind(voucher)
    }

    override fun getItemCount(): Int = vouchers.size

    class VoucherViewHolder(
        private val binding: ItemVoucherBinding,
        private val context: Context,
        private val fragment: VouchersCenterFragment
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(voucher: Voucher) {

            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val db = FirebaseFirestore.getInstance()

            binding.voucherPoints.text = "${voucher.voucherPoints}\nPoints"
            binding.voucherTitle.text =
                "${voucher.voucherPoints} ${voucher.voucherBrand} ${voucher.voucherType}"
            binding.voucherDescription.text =
                "For only ${voucher.voucherPoints} FundPoints, get ${voucher.voucherPoints} ${voucher.voucherBrand} ${voucher.voucherType}."
            Glide.with(binding.voucherLogo.context)
                .load(voucher.voucherLogo)
                .into(binding.voucherLogo)

            binding.voucherCheckoutBTN.setOnClickListener {

                if (user == null) {
                    val loginNoticeDialog = LayoutInflater.from(binding.root.context)
                        .inflate(R.layout.dialog_login_notice, null)
                    val builder =
                        AlertDialog.Builder(binding.root.context).setView(loginNoticeDialog)
                    val alertDialog = builder.show()
                    alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val gotoLoginButton: Button =
                        loginNoticeDialog.findViewById(R.id.gotoLoginButton)
                    gotoLoginButton.setOnClickListener {
                        val intent = Intent(context, HolderLoginRegisterActivity::class.java)
                        intent.putExtra("dialogToShow", "login")
                        alertDialog.dismiss()
                        context.startActivity(intent)
                        (context as Activity).finish()
                    }
                } else {
                    // Get the reference of the user document
                    var userDocRef = db.collection("users").document(user.uid)
                    userDocRef.get().addOnSuccessListener { documentSnapshot ->
                        var currentFundPoints = documentSnapshot.getDouble("fundPoints") ?: 0.0
                        if (currentFundPoints < voucher.voucherPoints) {
                            // User has insufficient fundPoints
                            val insufficientFundsDialog = LayoutInflater.from(binding.root.context)
                                .inflate(
                                    R.layout.dialog_voucher_redeem_insufficient_fundpoints,
                                    null
                                )
                            val builder = AlertDialog.Builder(binding.root.context)
                                .setView(insufficientFundsDialog)
                            val alertDialog = builder.show()
                            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            // Handle the actions in your insufficient funds dialog
                            val closeDialogBTN: Button =
                                insufficientFundsDialog.findViewById(R.id.closeDialogBTN)
                            closeDialogBTN.setOnClickListener {
                                alertDialog.dismiss()
                            }
                        } else {
                            val voucherClaimSummaryDialog =
                                LayoutInflater.from(binding.root.context)
                                    .inflate(R.layout.dialog_voucher_redeem_confirmation, null)
                            val builder =
                                AlertDialog.Builder(binding.root.context)
                                    .setView(voucherClaimSummaryDialog)
                            val alertDialog = builder.show()
                            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                            val voucherText =
                                voucherClaimSummaryDialog.findViewById<TextView>(R.id.voucherText)
                            val costText =
                                voucherClaimSummaryDialog.findViewById<TextView>(R.id.costText)

                            voucherText.text =
                                "${voucher.voucherPoints} ${voucher.voucherBrand} ${voucher.voucherType}"
                            costText.text = "${voucher.voucherPoints} FundPoints"

                            val confirmRedemptionBTN: Button =
                                voucherClaimSummaryDialog.findViewById(R.id.confirmRedemptionBTN)
                            confirmRedemptionBTN.setOnClickListener {
                                alertDialog.dismiss()
                                LoadingDialog.show(context, false)
                                // Get the reference of the user document
                                userDocRef = db.collection("users").document(user.uid)
                                // Run a transaction
                                db.runTransaction { transaction ->
                                    // Get the current user document
                                    val snapshot = transaction.get(userDocRef)
                                    currentFundPoints = snapshot.getDouble("fundPoints") ?: 0.0
                                    val newFundPoints = currentFundPoints - voucher.voucherPoints
                                    transaction.update(userDocRef, "fundPoints", newFundPoints)
                                    newFundPoints
                                }.addOnSuccessListener { newFundPoints ->
                                    // Record transaction for voucher redemption
                                    val transaction = user.uid.let { it1 ->
                                        Transaction(
                                            transactionId = "",
                                            userId = it1,
                                            projId = "",
                                            voucherId = voucher.voucherId,
                                            transactionType = "Voucher Redemption",
                                            transactionAmount = voucher.voucherPoints.toDouble(),
                                            paymentMethod = "",
                                            transactionDateTime = Timestamp.now(),
                                            transactionStatus = "Completed"
                                        )
                                    }
                                    val transactionDocRef = db.collection("transactions").document()
                                    transaction.transactionId = transactionDocRef.id

                                    transactionDocRef.set(transaction)
                                        .addOnSuccessListener {
                                            Log.d(
                                                "VoucherRedemption",
                                                "Transaction recorded successfully"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                "VoucherRedemption",
                                                "Failed to record transaction: ${e.message}"
                                            )
                                        }

                                    LoadingDialog.dismiss()
                                    Toast.makeText(
                                        context,
                                        "Voucher redeemed successfully! New FundPoints: $newFundPoints",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    fragment.loadUserData()
                                    val voucherClaimSuccessDialog =
                                        LayoutInflater.from(binding.root.context)
                                            .inflate(
                                                R.layout.dialog_voucher_redemption_success,
                                                null
                                            )
                                    val builder2 = AlertDialog.Builder(binding.root.context)
                                        .setView(voucherClaimSuccessDialog)
                                    val alertDialog2 = builder2.show()
                                    alertDialog2.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                                    val closeDialogBTN: Button =
                                        voucherClaimSuccessDialog.findViewById(R.id.closeDialogBTN)
                                    closeDialogBTN.setOnClickListener {
                                        alertDialog2.dismiss()
                                    }
                                }.addOnFailureListener { e ->
                                    // Transaction failure. Handle accordingly.
                                    Toast.makeText(
                                        context,
                                        "Failed to redeem voucher: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}