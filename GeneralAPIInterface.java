package com.example.getwithparam.Utils;

import com.example.getwithparam.Model.GeneralModel;
import com.example.getwithparam.Model.MultipartModel;
import com.example.getwithparam.Model.RawModel;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface GeneralAPIInterface {

    // GET method with PARAM. - RegisterActivity.
    @GET("register_service.php")
    Call<GeneralModel> getStatus(@Query("first_name") String firstname, @Query("last_name") String lastname, @Query("email") String email,
                                 @Query("mobile_number") String mobile, @Query("city") String city, @Query("password") String password);


    // GET method with Direct (or Dynamic) URL.- LoginActivity
    @GET
    Call<GeneralModel> getStatus2(@Url String url);


    // POST method with Form data with PARAMs- LoginActivity2
    @POST("login_service.php")
    @FormUrlEncoded
    Call<GeneralModel> getDetails(@Field("email") String Email, @Field("password") String Password);


    // POST method with Raw data in body, Header & Authorization.- RawActivity
    @POST("GetNotifications")
    Call<RawModel> getDetails3(@Header("Authorization")String token, @Body Map<String, String> body);

    @Multipart
    @POST("imf.php")
    Call<MultipartModel> getStatus3(@Part MultipartBody.Part part);
}
