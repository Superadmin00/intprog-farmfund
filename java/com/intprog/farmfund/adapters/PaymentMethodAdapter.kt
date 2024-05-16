package com.intprog.farmfund.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.intprog.farmfund.R
import com.intprog.farmfund.dataclasses.PaymentMethod

class PaymentMethodAdapter(private val paymentMethods: List<PaymentMethod>) :
    RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder>() {

    private var currentChecked: CheckBox? = null

    inner class PaymentMethodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.paymethodCheckBox)
        val paymethodLabel: TextView = view.findViewById(R.id.paymethodLabel)
        val paymethodLogo: ImageView = view.findViewById(R.id.paymethodLogo)
        val payMethodLayout: RelativeLayout = view.findViewById(R.id.payMethodLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_paymethod_buttons_donate, parent, false)
        return PaymentMethodViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        val paymentMethod = paymentMethods[position]
        holder.paymethodLabel.text = paymentMethod.payMethodName
        holder.paymethodLogo.setImageResource(paymentMethod.payMethodLogo)

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = holder.checkBox == currentChecked
        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            currentChecked = if (isChecked) {
                currentChecked?.run {
                    if (this != buttonView) {
                        setChecked(false)
                        // Set the background color of the unchecked layout to default
                        (this.parent as View).setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
                    }
                }
                // Set the background color of the checked layout to light green
                (buttonView.parent as View).setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.lightgreen))
                buttonView as CheckBox
            } else {
                // Set the background color of the unchecked layout to default
                (buttonView.parent as View).setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
                null
            }
        }

        holder.payMethodLayout.setOnClickListener {
            holder.checkBox.performClick()
        }
    }

    override fun getItemCount() = paymentMethods.size
}


