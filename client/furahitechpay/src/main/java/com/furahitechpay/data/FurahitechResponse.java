package com.furahitechpay.data;

import com.google.gson.annotations.SerializedName;

public class FurahitechResponse {
    @SerializedName("data")
    private String data;
    @SerializedName("transactionRef")
    private String transactionRef;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }
}
