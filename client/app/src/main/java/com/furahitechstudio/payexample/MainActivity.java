package com.furahitechstudio.payexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.furahitechpay.FurahitechPay;
import com.furahitechpay.data.PaymentDataRequest;
import com.furahitechpay.data.PaymentResult;
import com.furahitechpay.interfaces.PaymentResultListener;
import com.furahitechpay.util.Furahitech;

public class MainActivity extends AppCompatActivity implements PaymentResultListener{

    private FurahitechPay furahitechPay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PaymentDataRequest request = new PaymentDataRequest();
        request.setAmount(50000);
        request.setEmailAddress("exampleclient@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry");
        request.setProductId("2");
        request.setProductName("Lorem Ipsum");
        request.setGatewayType(Furahitech.GateWay.MNO);
        request.useLiveApi(true);
        request.setDataReceiver("");
        request.setOneSignalApiKey("");
        request.setOneSignalAppId("");

        furahitechPay = FurahitechPay.getInstance()
                .with(this)
                .setBaseUrl("")
                .setPaymentDataRequest(request)
                .setStripePublishableKey("")
                .setPaymentResultListener(this)
                .setSupportedGateway(Furahitech.GateWay.TIGOPESA);
        furahitechPay.pay();


    }

    @Override
    public void onPaymentCompleted(PaymentResult paymentResult) {
        Log.d("Payment Result ",paymentResult.toString());
        furahitechPay.acknowledgePaymentResult(paymentResult);
    }
}
