package com.intprog.farmfund.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.VoucherAdapter
import com.intprog.farmfund.databinding.FragmentVouchersCenterBinding
import com.intprog.farmfund.dataclasses.Voucher

class VouchersCenterFragment : Fragment() {

    private var _binding: FragmentVouchersCenterBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVouchersCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vouchers = listOf(
            Voucher(1, 20, R.drawable.ic_globe_logo, "Globe Prepaid Load Credits"),
            Voucher(2, 50, R.drawable.ic_globe_logo, "Globe Prepaid Load Credits"),
            Voucher(3, 100, R.drawable.ic_globe_logo, "Globe Prepaid Load Credits"),
            Voucher(4, 200, R.drawable.ic_globe_logo, "Globe Prepaid Load Credits"),
            Voucher(5, 20, R.drawable.ic_globe_logo, "SMART Prepaid Load Credits"),
            Voucher(6, 50, R.drawable.ic_globe_logo, "SMART Prepaid Load Credits"),
            Voucher(7, 100, R.drawable.ic_globe_logo, "SMART Prepaid Load Credits"),
            Voucher(8, 200, R.drawable.img_valo, "SMART Prepaid Load Credits")
        )
        binding.vouchersRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.vouchersRecyclerView.adapter = VoucherAdapter(vouchers)
    }
}