package com.intprog.farmfund.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intprog.farmfund.R
import com.intprog.farmfund.dataclasses.PaymentMethod
import com.intprog.farmfund.databinding.ItemPaymentMethodBinding

class PaymentMethodAdapter(private val paymentMethods: List<PaymentMethod>) :
    RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder>() {

    private var selectedPaymentMethod: PaymentMethod? = null

    inner class PaymentMethodViewHolder(val binding: ItemPaymentMethodBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                selectedPaymentMethod = paymentMethods[adapterPosition]
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val binding = ItemPaymentMethodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentMethodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        with(holder) {
            with(paymentMethods[position]) {
                binding.paymethodName.text = paymethodName
                binding.paymethodAccNumber.text = paymethodAccNumber
                Glide.with(binding.root).load(paymethodLogo).into(binding.paymethodLogo)

                if (selectedPaymentMethod == this) {
                    binding.payMethodLayout.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.lightgreen))
                } else {
                    binding.payMethodLayout.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.white))
                }
                holder.binding.payMethodLayout.isSelected = selectedPaymentMethod == paymentMethods[position]
            }
        }
    }

    override fun getItemCount() = paymentMethods.size

    fun getSelectedPaymentMethod(): PaymentMethod? {
        return selectedPaymentMethod
    }
}