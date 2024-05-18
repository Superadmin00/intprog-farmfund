package com.intprog.farmfund.abstractclass

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    abstract fun validateInput(): Boolean
}