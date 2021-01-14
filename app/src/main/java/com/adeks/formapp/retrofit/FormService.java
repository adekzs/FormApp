package com.adeks.formapp.retrofit;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FormService {

//    @Headers({
//            "Accept: application/json "
//    })
//    @Multipart
//    @POST("/worker/register")
//    Call<ResponseBody> createUser(@Part ("user")RequestBody user, @Part MultipartBody.Part image);

    @Headers({
            "Accept: application/json "
    })
    @POST("api/v1/worker/register")
    Call<ResponseBody> createUser(@Body RequestBody user);

}
