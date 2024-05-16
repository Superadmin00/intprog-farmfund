package com.intprog.farmfund.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intprog.farmfund.R
import com.intprog.farmfund.dataclasses.Voucher

class VoucherAdapter(private val vouchers: List<Voucher>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Define constants for the two types of items in RecyclerView
    companion object {
        private const val TYPE_FUND_DETAILS = 0
        private const val TYPE_VOUCHER = 1
    }

    // ViewHolder for the fund details item
    inner class FundDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // ViewHolder for the voucher item
    inner class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // Determine the type of the current item
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_FUND_DETAILS else TYPE_VOUCHER
    }

    // Inflate the appropriate layout based on the item type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_FUND_DETAILS) {
            val view = layoutInflater.inflate(R.layout.item_fundpoints_details, parent, false)
            FundDetailsViewHolder(view)
        } else {
            val view = layoutInflater.inflate(R.layout.item_voucher, parent, false)
            VoucherViewHolder(view)
        }
    }

    // Bind data to the item based on its type
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VoucherViewHolder) {
            // Get the voucher data for this position
            val voucher = vouchers[position - 1] // Subtract 1 because the first position is occupied by FundDetailsViewHolder

            // Bind the voucher data to the item
            holder.itemView.apply {
                findViewById<TextView>(R.id.voucherCost).text = "${voucher.voucherPoints}\nPoints"
                findViewById<ImageView>(R.id.voucherBrandLogo).setImageResource(voucher.voucherLogo)
                findViewById<TextView>(R.id.voucherTitle).text = "${voucher.voucherPoints} Points"
                findViewById<TextView>(R.id.voucherDescription).text = voucher.voucherType

                // Update the isChecked property of the voucher when the checkbox is checked/unchecked
                val voucherCheckBox: CheckBox = findViewById(R.id.voucherCheckBox)
                voucherCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    voucher.isChecked = isChecked
                }
            }
        }
        // No need to bind data if holder is FundDetailsViewHolder
    }

    // The total item count is the size of the vouchers list plus 1 for the FundDetailsViewHolder
    override fun getItemCount() = vouchers.size + 1
}
