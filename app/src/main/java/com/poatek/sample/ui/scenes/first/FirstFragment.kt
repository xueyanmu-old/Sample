package com.poatek.sample.ui.scenes.first

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.poatek.sample.databinding.FragmentFirstBinding
import com.poatek.sample.ui.base.BaseFragment
import java.io.File
import java.util.*


@Suppress("DEPRECATION")
class FirstFragment : BaseFragment<FragmentFirstBinding>() {

    //view
    override val viewModel: FirstViewModel by viewModels()

    //make dimensions/binding
    override fun onCreateBinding(inflater: LayoutInflater): FragmentFirstBinding {
        return FragmentFirstBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //go to the fragment that takes selfie picture
        binding.secondFragmentButton.setOnClickListener {
            takePicturePressed()
        }

        //go to the fragment that takes ID picture
        binding.thirdFragmentButton.setOnClickListener {
            takePicturePressed()
        }

        //Displays the image (if theres an image on the view model)
        viewModel.expectedImageOutputPath?.also { setImageViewPicture(it) }
    }

    //what happens after pressing OPEN SECOND FRAGMENT
    //output: starts the camera activity with all directories/photo IDs set up
    private fun takePicturePressed() {

        val directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val outputFile = File.createTempFile(
            "picture_${Calendar.getInstance().timeInMillis}",
            ".jpg",
            directory
        )

        val outputFileUri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.android.fileprovider",
            outputFile
        )

        //Save the output path locally
        viewModel.expectedImageOutputPath = outputFile.absolutePath

        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        }

        startActivityForResult(pictureIntent, CAPTURE_IMAGE_REQUEST_CODE)
    }

    //fragment manager
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAPTURE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.expectedImageOutputPath?.also {
                setImageViewPicture(it)

            }
        }
    }

    private fun setImageViewPicture(filePath: String) {
        BitmapFactory.decodeFile(filePath)?.also {
            binding.imageView.setImageBitmap(it)
        }
    }

    companion object {
        private const val CAPTURE_IMAGE_REQUEST_CODE = 700
    }

}