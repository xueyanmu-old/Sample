

package com.poatek.sample.ui.scenes.first

//TODO: REVIEW THE NEW IMPORTS
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
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.poatek.sample.databinding.FragmentFirstBinding
import com.poatek.sample.ui.base.BaseFragment
import interfaces.RestAPI
import kotlinx.android.synthetic.main.fragment_first.*
import models.Analyze
import models.VerificationResponse
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


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
            takePicturePressed(CAPTURE_SELFIE_REQUEST_CODE).also { outputUrl ->
                viewModel.selfieImageOutputPath = outputUrl
            }
        }

        //go to the fragment that takes ID picture
        binding.thirdFragmentButton.setOnClickListener {
            takePicturePressed(CAPTURE_ID_REQUEST_CODE).also { outputUrl ->
                viewModel.idImageOutputPath = outputUrl
            }
        }

        //Displays the selfie image (if theres an image on the view model)
        viewModel.selfieImageOutputPath?.also { setImageViewPicture(it, binding.imageView) }
        viewModel.idImageOutputPath?.also { setImageViewPicture(it, binding.imageViewID) }
    }



    //output: starts the camera activity with all directories/photo IDs set up
    private fun takePicturePressed(requestCode:Int): String {

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

        //TODO: ACTION_IMAGE_CAPTURE_SECURE
        //TODO: many pictures? how
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        }

        startActivityForResult(pictureIntent, requestCode)

        return outputFile.absolutePath
    }

    //fragment manager for selfie
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        //IF THE SELFIE IS TAKEN FIRST
        if (requestCode == CAPTURE_SELFIE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.selfieImageOutputPath?.also {
                setImageViewPicture(it, binding.imageView)
                Toast.makeText(context, "Selfie successfully captured!", Toast.LENGTH_SHORT).show()

            }
        }
        //IF THE ID IS TAKEN FIRST
        else if (requestCode == CAPTURE_ID_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.idImageOutputPath?.also {
                setImageViewPicture(it, binding.imageViewID)
                Toast.makeText(context, "ID successfully captured!", Toast.LENGTH_SHORT).show()

            }
        }

        if(viewModel.selfieImageOutputPath != null && viewModel.idImageOutputPath != null){
            verifyPhoto(viewModel.idImageOutputPath ?: return, viewModel.selfieImageOutputPath ?: return)
        }
    }

    //dynamically display the selfie or ID photo
    private fun setImageViewPicture(filePath: String, imageView: ImageView) {
        BitmapFactory.decodeFile(filePath)?.also {
            imageView.setImageBitmap(it)
        }
    }

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

    fun verifyPhoto(pathID: String, pathSelfie: String) {
        val file = File(pathID)
        val fileSelfie = File(pathSelfie)


//        //create a file to write bitmap data
//        val file = File(super.getContext()?.cacheDir, "test3.jpg")
//        file.createNewFile()
//
//        Bitmap bitmap = pathID
//        //Convert bitmap to byte array
//        val bos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
//        val bitmapdata = bos.toByteArray()
//        file.writeBytes(bitmapdata)



        val filesToUpload = listOf(
            MultipartBody.Part.createFormData("file", fileSelfie.name, RequestBody.create(MediaType.parse("multipart/form-data"), fileSelfie)),
            MultipartBody.Part.createFormData("file", file.name, RequestBody.create(MediaType.parse("multipart/form-data"), file))
        )

//        val uploadBody = MultipartBody.Builder().apply {
//            val mFileSelfie = RequestBody.create(MediaType.parse("multipart/form-data"), fileSelfie)
//            val mFileId = RequestBody.create(MediaType.parse("multipart/form-data"), fileId)
//
//            addFormDataPart("file[]", fileId.name, mFileId)
//            addFormDataPart("file[]", fileSelfie.name, mFileSelfie)
//        }.build()

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
        val fileUpload = uploadImage.verify(filesToUpload)
        Log.e("URL", fileUpload.request().url().toString())

        fileUpload.enqueue(object : Callback<VerificationResponse> {
            override fun onResponse(
                call: Call<VerificationResponse>,
                response: Response<VerificationResponse>
            ) {
                Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()

                val body = response.body()
                val result = body?.result
                Toast.makeText(context, result.toString(), Toast.LENGTH_SHORT).show()



                //analyze_result_text.setText(response.message())
                analyze_result_text.text = result?.verified?.toString()

            }

            override fun onFailure(
                call: Call<VerificationResponse>,
                t: Throwable
            ) {
                Log.e("Mahsa Rideman", "Error " + t.message)
                Toast.makeText(context, "Error " + t.message, Toast.LENGTH_SHORT).show()


            }
        })
        Log.e("Heivoooon", "Miay asln?")


    }


    companion object {
        private const val CAPTURE_SELFIE_REQUEST_CODE = 700
        private const val CAPTURE_ID_REQUEST_CODE = 699
        var selectedPath1 = "NONE"
        var selectedPath2 = "NONE"
    }

}