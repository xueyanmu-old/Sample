

package com.poatek.sample.ui.scenes.first

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
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.poatek.sample.R
import com.poatek.sample.databinding.FragmentFirstBinding
import com.poatek.sample.ui.base.BaseFragment
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit

//TODO: REVIEW THE NEW IMPORTS
import interfaces.RestAPI
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.android.synthetic.main.fragment_first.view.*
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

    //view selfie
    override val viewModel: FirstViewModel by viewModels()

    //make dimensions/binding
    override fun onCreateBinding(inflater: LayoutInflater): FragmentFirstBinding {
        return FragmentFirstBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        imageView.setColorFilter(
//            ContextCompat.getColor(, R.color.COLOR_YOUR_COLOR),
//
//            android.graphics.PorterDuff.Mode.MULTIPLY);
        //go to the fragment that takes selfie picture
        binding.secondFragmentButton.setOnClickListener {
            takePicturePressed(CAPTURE_SELFIE_REQUEST_CODE)

            //Displays the selfie image (if theres an image on the view model)
            viewModel.expectedImageOutputPath?.also { setImageViewPicture(it, CAPTURE_SELFIE_REQUEST_CODE) }
        }

        //go to the fragment that takes ID picture
        binding.thirdFragmentButton.setOnClickListener {
            takePicturePressed(CAPTURE_ID_REQUEST_CODE)

            //Displays the selfie image (if theres an image on the view model)
            viewModel.expectedImageOutputPath?.also { setImageViewPicture(it, CAPTURE_ID_REQUEST_CODE) }
        }
    }



    //output: starts the camera activity with all directories/photo IDs set up
    private fun takePicturePressed(requestCode: Int) {

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
        if (requestCode == 700){
            viewModel.expectedImageOutputPath = outputFile.absolutePath
        }
        else if (requestCode == 699){
            viewModel.expectedImageOutputPathID = outputFile.absolutePath
        }
        else{ System.out.println("error with capture-image-request-code") }

        //TODO: ACTION_IMAGE_CAPTURE_SECURE
        //TODO: many pictures? how
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        }

        startActivityForResult(pictureIntent, requestCode)
    }

    //fragment manager for selfie
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //IF THE SELFIE IS TAKEN FIRST
        if (requestCode == CAPTURE_SELFIE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.expectedImageOutputPath?.also {
                setImageViewPicture(it, CAPTURE_SELFIE_REQUEST_CODE)
                Toast.makeText(context, "Selfie successfully captured!", Toast.LENGTH_SHORT).show()
            }
        }
        //IF THE ID IS TAKEN FIRST
        else if (requestCode == CAPTURE_ID_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.expectedImageOutputPathID?.also {
                setImageViewPicture(it, CAPTURE_ID_REQUEST_CODE)

                Toast.makeText(context, "ID successfully captured!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //dynamically display the selfie or ID photo
    private fun setImageViewPicture(filePath: String, requestCode: Int) {
        BitmapFactory.decodeFile(filePath)?.also {

            if (requestCode == 700){
                binding.imageView.setImageBitmap(it)
            }
            else if (requestCode == 699){
                binding.imageViewID.setImageBitmap(it)
            }
            analyzePhoto(bitmap = it)
        }
    }

    //dynamically display the selfie or ID photo
//    private fun setVerifyImages(filePath: String, filePathID: String,
//                                requestCode: Int, requestCodeID: Int) {
//        BitmapFactory.decodeFile(filePath)?.also {
//
//            if (requestCode == 700){
//                binding.imageView.setImageBitmap(it)
//            }
//            else if (requestCode == 699){
//                binding.imageViewID.setImageBitmap(it)
//            }
//            verifyPhoto(bitmap = it, bitmapID = )
//        }
//    }
    fun analyzePhoto(bitmap: Bitmap) {

        //create a file to write bitmap data
        val file = File(super.getContext()?.cacheDir, "test3.jpg")
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()
        file.writeBytes(bitmapdata)

        val mFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)

        val fileToUpload =
            MultipartBody.Part.createFormData("file", file.name, mFile)


        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        httpClient.connectTimeout(1000, TimeUnit.SECONDS)
        httpClient.readTimeout(2000000, TimeUnit.SECONDS)
        httpClient.addInterceptor(logging)
//        Execute Request!
        val retrofit = Retrofit.Builder()
            //.baseUrl("https://deepface-poatek.herokuapp.com/")
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

                val body = response.body()
                val pred = body!!.prediction
                val emo = pred!!.dominant_emotion
                Toast.makeText(context, emo, Toast.LENGTH_SHORT).show()

                Log.e(" Javab: ", response.message())
                Log.e(" Javab: ", emo.toString())

                //analyze_result_text.setText(response.message())
                analyze_result_text.setText(emo.toString())

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

    fun verifyPhoto(bitmap: Bitmap, bitmapID: Bitmap) {

        //create a file to write bitmap data
        val file = File(super.getContext()?.cacheDir, "test3.jpg")
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()
        file.writeBytes(bitmapdata)

        val mFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)

        val fileToUpload =
            MultipartBody.Part.createFormData("file", file.name, mFile)


        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        httpClient.connectTimeout(1000, TimeUnit.SECONDS)
        httpClient.readTimeout(2000000, TimeUnit.SECONDS)
        httpClient.addInterceptor(logging)
//        Execute Request!
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

                val body = response.body()
                val pred = body!!.prediction
                val emo = pred!!.dominant_emotion
                Toast.makeText(context, emo, Toast.LENGTH_SHORT).show()

                Log.e(" Javab: ", response.message())
                Log.e(" Javab: ", emo.toString())

                //analyze_result_text.setText(response.message())
                analyze_result_text.setText(emo.toString())

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

    fun uploadVerify(){
        binding.uploadImages.setOnClickListener {
            takePicturePressed(CAPTURE_ID_REQUEST_CODE)

            //Displays the selfie image (if theres an image on the view model)
            viewModel.expectedImageOutputPath?.also { setImageViewPicture(it, CAPTURE_ID_REQUEST_CODE) }
        }
    }

    companion object {
        private const val CAPTURE_SELFIE_REQUEST_CODE = 700
        private const val CAPTURE_ID_REQUEST_CODE = 699
        private const val UPLOAD_IMAGE_CODE = 600
    }

}