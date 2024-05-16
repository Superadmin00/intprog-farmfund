package com.intprog.farmfund.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intprog.farmfund.R
import com.intprog.farmfund.dataclasses.BankInfo

// BankInfoAdapter class
class BankInfoAdapter(private val bankList: List<BankInfo>) : RecyclerView.Adapter<BankInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankInfoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bank_option, parent, false)
        return BankInfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BankInfoViewHolder, position: Int) {
        val bankInfo = bankList[position]
        holder.bankLogo.setImageResource(bankInfo.logo)
        holder.bankTitle.text = bankInfo.title
        holder.bankDescription.text = bankInfo.description
        // Set checkbox logic here (optional)
    }

    override fun getItemCount(): Int {
        return bankList.size
    }
}

// BankInfoViewHolder class
class BankInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val bankLogo: ImageView = itemView.findViewById(R.id.bankLogo)
    val bankTitle: TextView = itemView.findViewById<TextView>(R.id.bankDetails)
    val bankDescription = itemView.findViewById<TextView>(R.id.bankExpiry)
}