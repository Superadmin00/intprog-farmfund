package com.intprog.farmfund.objects

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.intprog.farmfund.R

object LoadingDialog {

    private var dialog: Dialog? = null

    fun show(context: Context, cancelable: Boolean) {
        dismiss() // Dismiss any previous dialog to avoid overlapping dialogs

        val loadingView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        dialog = Dialog(context, R.style.SemiTransparentDialog).apply {
            setContentView(loadingView)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val loadingImageView: ImageView = findViewById(R.id.loadingImageView)
            Glide.with(context).load(R.drawable.ic_loading).into(loadingImageView)

            setCancelable(cancelable)
            setCanceledOnTouchOutside(cancelable)

            show()
        }
    }
    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}
