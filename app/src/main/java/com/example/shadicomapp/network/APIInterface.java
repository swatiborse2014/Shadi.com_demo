package com.example.shadicomapp.network;

import com.example.shadicomapp.model.UserInfoModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APIInterface {

//    https://randomuser.me/api/?

//    String API_DOMAIN = "https://randomuser.me/api/?";
    String API_DOMAIN = "https://randomuser.me/api/?results=10";

    @GET(API_DOMAIN)
    Call<UserInfoModel> getUsers();

}
