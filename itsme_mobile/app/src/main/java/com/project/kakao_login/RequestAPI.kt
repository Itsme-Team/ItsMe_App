package com.project.kakao_login

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RequestAPI {
    @GET("/api/mobile_request/")
    fun getPosts(@HeaderMap headers: Map<String, String>): Call<List<GetItem>>

    @POST("/api/delete/")
    fun delete(@Body deleteRequest: DeleteRequest): Call<Any>

    @Multipart
    @POST("/api/file_post/")
    fun postFile(
        @Part file: MultipartBody.Part,
        @Part("user_name") userName: RequestBody,
        @Part("user_id") userId: RequestBody
    ): Call<Any>
}

data class DeleteRequest(val user_id: String, val room: String)