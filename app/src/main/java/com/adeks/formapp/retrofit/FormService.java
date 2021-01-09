package com.adeks.formapp.retrofit;

import com.adeks.formapp.model.User;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

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
