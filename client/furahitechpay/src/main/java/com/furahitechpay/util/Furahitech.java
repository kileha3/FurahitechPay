package com.furahitechpay.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

public class Furahitech {
    public static final String PAYMENT_DETAILS_TAG = "details";

    public enum Language{
        SWAHILI,
        ENGLISH
    }

    public enum GateWay{
        MPESA,
        TIGOPESA,
        CARD,
        MNO,
        NONE
    }

    public enum PaymentStatus{
        NOT_STARTED,
        PRE_SUCCESS,
        PROCESSING,
        SUCCESS,
        FAILURE,
        CANCELLED,
        COMPLETED,
        TIMEOUT
    }

    /**
     * Format amount into price format
     * @param price Amount int integer
     * @param currency Currency to be used
     * @return Formatted amount
     */
    @SuppressLint("DefaultLocale")
    public static String formatPrice(int price, @Nullable String currency){
        return String.format("%,d",Integer.parseInt(String.valueOf(price)))+"/="+(currency==null ? "":currency);
    }


    /**
     * Check if there is active internet connection
     * @param context Application context
     * @return TRUE if connection is active otherwise FALSE
     */
    public static boolean isConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null
                    && activeNetwork.isConnectedOrConnecting();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Get Payment Gateway from client's phone number
     * @param phone Customer phone number
     * @return String : Payment gateway MNO
     */
    public static GateWay getPaymentGateway(String phone){
        if(phone.startsWith("71") || phone.startsWith("65") || phone.startsWith("67")){
            return GateWay.TIGOPESA;
        }else if(phone.startsWith("74") || phone.startsWith("75") || phone.startsWith("76")){
            return GateWay.MPESA;
        }else{
            return GateWay.NONE;
        }

    }

}
