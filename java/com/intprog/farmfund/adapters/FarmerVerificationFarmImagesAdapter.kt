package com.intprog.farmfund.adapters

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intprog.farmfund.R
import com.intprog.farmfund.databinding.ItemImageUploadBinding
import com.intprog.farmfund.viewmodels.FarmerVerifViewModel

class FarmerVerificationFarmImagesAdapter(private val imageList: MutableList<Bitmap>, private val viewModel: FarmerVerifViewModel) :
    RecyclerView.Adapter<FarmerVerificationFarmImagesAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView =
            itemView.findViewById(R.id.imageView) // Correct ID for your ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_upload, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageBitmap(imageList[position])

        holder.imageView.setOnLongClickListener {
            // Show an AlertDialog to confirm the removal
            AlertDialog.Builder(it.context)
                .setTitle("Remove Image")
                .setMessage("Are you sure you want to remove this image?")
                .setPositiveButton("Yes") { _, _ ->
                    imageList.removeAt(position)
                    notifyDataSetChanged()

                    // Update farmImages in the ViewModel
                    viewModel.farmImages.value = imageList
                }
                .setNegativeButton("No", null)
                .show()

            true
        }
    }

    override fun getItemCount() = imageList.size
}