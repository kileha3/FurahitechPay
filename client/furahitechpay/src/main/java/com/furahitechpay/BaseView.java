package com.furahitechpay;

import android.content.Context;

import com.furahitechpay.data.PaymentResult;

public interface BaseView {

    void setUpViews();

    void setToolbar();

    void showRequestFailure(String message);

    void showProgress(boolean isAuthenticating);

    void hideProgress();

    void showCompletionDialog(PaymentResult paymentResult);

    void enablePaymentButton(boolean enable);

    Context getContext();

    void finishProcess();

    void hideKeyboard();

}
