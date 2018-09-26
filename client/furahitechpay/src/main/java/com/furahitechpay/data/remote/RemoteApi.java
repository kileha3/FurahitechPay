package com.furahitechpay.data.remote;

import com.furahitechpay.data.FurahitechResponse;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Handles interfacing between client and server side
 */
interface RemoteApi {
    @FormUrlEncoded
    @POST("v1/tigo/authenticate")
    Call<FurahitechResponse> authenticateTigoPesa(@FieldMap HashMap<String, String> requestParam);

    @FormUrlEncoded
    @POST("v1/vodacom/authenticate")
    Call<FurahitechResponse> authenticateMpesa(@FieldMap HashMap<String, String> requestParam);

    @FormUrlEncoded
    @POST("v1/stripe/authenticate")
    Call<FurahitechResponse> authenticateStripe(@FieldMap HashMap<String, String> requestParam);

    @DELETE("v1/{transactionRef}/acknowledge")
    Call<FurahitechResponse> acknowledgePaymentResult(@Path("transactionRef")String transactionRef);

}
