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
    RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    // ViewHolder for the voucher item
    inner class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val voucherCost: TextView = itemView.findViewById(R.id.voucherCost)
        val voucherBrandLogo: ImageView = itemView.findViewById(R.id.voucherBrandLogo)
        val voucherTitle: TextView = itemView.findViewById(R.id.voucherTitle)
        val voucherDescription: TextView = itemView.findViewById(R.id.voucherDescription)
        val voucherCheckBox: CheckBox = itemView.findViewById(R.id.voucherCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_voucher, parent, false)
        return VoucherViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        // Get the voucher data for this position
        val voucher = vouchers[position]

        // Bind the voucher data to the item
        holder.voucherCost.text = "${voucher.voucherPoints}\nPoints"
        holder.voucherBrandLogo.setImageResource(voucher.voucherLogo)
        holder.voucherTitle.text = "${voucher.voucherPoints} Points"
        holder.voucherDescription.text = voucher.voucherType

        // Update the isChecked property of the voucher when the checkbox is checked/unchecked
        holder.voucherCheckBox.setOnCheckedChangeListener { _, isChecked ->
            voucher.isChecked = isChecked
        }
    }

    override fun getItemCount() = vouchers.size
}
