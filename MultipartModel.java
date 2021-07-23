package com.example.getwithparam.Model;

import com.google.gson.annotations.SerializedName;

public class MultipartModel {

    @SerializedName("url")
    public String url;
    @SerializedName("message")
    public String message;
    @SerializedName("error")
    public boolean error;
    @SerializedName("status")
    public String status;
}
