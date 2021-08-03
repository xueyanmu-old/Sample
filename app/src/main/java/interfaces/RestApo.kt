package interfaces

import models.Analyze
import models.VerificationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface RestAPI {
    @Multipart
    @POST("analyze/")
    fun analyze(@Part file: MultipartBody.Part?): Call<Analyze>

    @Multipart
    @POST("verification/")
    fun verify(@Part file: List<MultipartBody.Part>): Call<VerificationResponse>
}