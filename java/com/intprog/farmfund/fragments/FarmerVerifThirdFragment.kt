package com.intprog.farmfund.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.intprog.farmfund.abstractclass.BaseFragment
import com.intprog.farmfund.databinding.FragmentFarmverifFarmerdocsBinding
import com.intprog.farmfund.viewmodels.FarmerVerifViewModel

class FarmerVerifThirdFragment : BaseFragment() {

    private lateinit var viewModel: FarmerVerifViewModel
    private var _binding: FragmentFarmverifFarmerdocsBinding? = null

    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFarmverifFarmerdocsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[FarmerVerifViewModel::class.java]

        val buttons = listOf(binding.barangayCertUploadBTN, binding.validIDUploadBTN, binding.landOwnershipLeaseBTN, binding.rsbsaBTN)
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener { openGallery(index + 1) }
        }

        return binding.root
    }

    private fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)

            val imageViews = listOf(binding.barangayCertImage, binding.validIDImage, binding.landOwnershipLeaseImage, binding.rsbsaImage)
            val imageUris = listOf(viewModel.barangayCertImageUri, viewModel.validIDImageUri, viewModel.landOwnershipLeaseImageUri, viewModel.rsbsaImageUri)

            imageViews[requestCode - 1].setImageBitmap(bitmap)
            imageUris[requestCode - 1].value = uri
        }
    }

    override fun validateInput(): Boolean {
        val imageUris = listOf(viewModel.barangayCertImageUri, viewModel.validIDImageUri, viewModel.landOwnershipLeaseImageUri)
        val errorMessages = listOf("Please upload the Barangay Certificate image!", "Please upload the Valid ID image!", "Please upload Proof of Land Ownership or Lease Agreement!")
        val errorTexts = listOf(binding.errorText1, binding.errorText2, binding.errorText3)

        var isValid = true

        imageUris.forEachIndexed { index, uri ->
            if (uri.value == null) {
                Toast.makeText(context, errorMessages[index], Toast.LENGTH_SHORT).show()
                errorTexts[index].visibility = View.VISIBLE
                isValid = false
            } else {
                errorTexts[index].visibility = View.INVISIBLE
            }
        }
        return isValid
    }
}