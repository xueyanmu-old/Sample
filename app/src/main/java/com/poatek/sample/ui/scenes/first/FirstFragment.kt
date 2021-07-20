package com.poatek.sample.ui.scenes.first
package com.example.deepfaceapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.poatek.sample.databinding.FragmentFirstBinding
import com.poatek.sample.ui.base.BaseFragment
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
//TODO: REVIEW THE NEW IMPORTS
import interfaces.RestAPI
import kotlinx.android.synthetic.main.fragment_first.*
import models.Analyze
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

                //TODO: FIX THIS NEW CODE
                analyzePhoto(it)

                Toast.makeText(context, "Photo successfully captured!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setImageViewPicture(filePath: String) {
        BitmapFactory.decodeFile(filePath)?.also {
            binding.imageView.setImageBitmap(it)
        }
    }

    fun analyzePhoto(bitmap: Bitmap) {

        //create a file to write bitmap data
        val file = File(super.getContext()?.cacheDir, "test3");
        file.createNewFile();

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        val bitmapdata = bos.toByteArray();

        //write the bytes in file
        val fos: FileOutputStream? = null;
        try {
            val fos = FileOutputStream(file);
        } catch (e: FileNotFoundException) {
            e.printStackTrace();
        }
        try {
            fos?.write(bitmapdata);
            fos?.flush();
            fos?.close();
        } catch (e: IOException) {
            e.printStackTrace();
        }
        //val file = File("/home/mahsa/Downloads/profile-photo.jpeg")
        val mFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)

        val fileToUpload =
            MultipartBody.Part.createFormData("image", file.name, mFile)


        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        httpClient.connectTimeout(1000, TimeUnit.SECONDS)
        httpClient.readTimeout(2000000, TimeUnit.SECONDS)
        httpClient.addInterceptor(logging)
        //Execute Request!
        val retrofit = Retrofit.Builder()
            .baseUrl("https://deepface-app.herokuapp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()

        val uploadImage: RestAPI =
            retrofit.create(RestAPI::class.java)
        val fileUpload: Call<Analyze> =
            uploadImage.analyze(fileToUpload)
        Log.e("URL", fileUpload.request().url().toString())

        fileUpload.enqueue(object : Callback<Analyze> {
            override fun onResponse(
                call: Call<Analyze>,
                response: Response<Analyze>
            ) {
                Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()

                Log.e(" Javab: ", response.message())
                analyze_result_text.setText(response.message())
            }

            override fun onFailure(
                call: Call<Analyze>,
                t: Throwable
            ) {
                Log.e("Mahsa Rideman", "Error " + t.message)
                Toast.makeText(context, "Error " + t.message, Toast.LENGTH_SHORT).show()


            }
        })
        Log.e("Heivoooon", "Miay asln?")

    }

    companion object {
        private const val CAPTURE_IMAGE_REQUEST_CODE = 700
    }

}