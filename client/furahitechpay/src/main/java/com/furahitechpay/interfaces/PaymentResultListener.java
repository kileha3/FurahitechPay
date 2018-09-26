package com.furahitechpay.interfaces;

import com.furahitechpay.data.PaymentResult;

public interface PaymentResultListener {

    void onPaymentCompleted(PaymentResult paymentResult);
}
