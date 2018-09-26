package com.furahitechpay;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.furahitechpay.data.ConnectionRequestListener;
import com.furahitechpay.data.FurahitechResponse;
import com.furahitechpay.data.PaymentDataRequest;
import com.furahitechpay.data.PaymentResult;
import com.furahitechpay.data.remote.RemoteCalls;
import com.furahitechpay.exceptions.FurahitechException;
import com.furahitechpay.interfaces.PaymentResultListener;
import com.furahitechpay.paymentcard.CardPaymentActivity;
import com.furahitechpay.paymentmno.MnoPaymentActivity;
import com.furahitechpay.util.Furahitech;
import com.furahitechpay.util.executors.ThreadExecutor;
import com.furahitechpay.util.executors.ThreadManager;
import com.google.gson.Gson;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.furahitechpay.data.PaymentDataRequest.NULL;

public class FurahitechPay {
    @SuppressLint("StaticFieldLeak")
    private static FurahitechPay instance;
    private Context context;
    private PaymentDataRequest paymentDataRequest;
    private static final String INTENT_PAYMENT_RESULT_DATA = "intent_payment_result_data";
    private static final String EXTRA_PAYMENT_RESULT_DATA = "extra_payment_result_data";
    private String baseUrl;
    private String stripePublishableKey;
    private List<PaymentResultListener> listeners = new CopyOnWriteArrayList<>();
    private List<Enum> supportedGateWay = new ArrayList<>();


    /**
     * Acquire FurahitechPay instance
     * @return FurahitechPay instance
     */
    public static FurahitechPay getInstance() {
        if(instance == null){
            instance = new FurahitechPay();
        }
        return instance;
    }

    /**
     * Broadcast receiver to listen for payment result callback
     */
    private BroadcastReceiver paymentResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(listeners != null){
                PaymentResult paymentResult =
                        (PaymentResult) intent.getSerializableExtra(EXTRA_PAYMENT_RESULT_DATA);
                FurahitechPay.getInstance()
                        .getPaymentDataRequest()
                        .setPaymentStatus(paymentResult.isSuccess() ?
                                Furahitech.PaymentStatus.SUCCESS: Furahitech.PaymentStatus.FAILURE);
                for(PaymentResultListener paymentResultListener : listeners){
                    paymentResultListener.onPaymentCompleted(paymentResult);
                }
            }
        }
    };

    /**
     * Set application context
     * @param context Application context
     * @return FurahitechPay instance
     */
    public FurahitechPay with(Context context){
        this.context = context;
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(paymentResultReceiver, new IntentFilter(INTENT_PAYMENT_RESULT_DATA));
        OneSignal.startInit(context)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationReceivedHandler(new NotificationReceiver())
                .unsubscribeWhenNotificationsAreDisabled(false)
                .init();
        return  this;
    }

    /**
     * Listen for all received payment notification
     */
    private class NotificationReceiver implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(OSNotification notification) {
            PaymentResult paymentResult = new Gson().fromJson(notification.payload.additionalData
                            .optString("notification_data", null),
                    PaymentResult.class);
            Intent paymentIntent = new Intent(INTENT_PAYMENT_RESULT_DATA);
            paymentIntent.putExtra(EXTRA_PAYMENT_RESULT_DATA,paymentResult);
            LocalBroadcastManager.getInstance(context).sendBroadcast(paymentIntent);
        }
    }

    /**
     * Set payment data request details
     * @param request Transaction request instance
     * @return FurahitechPay instance
     */
    public FurahitechPay setPaymentDataRequest(PaymentDataRequest request){
        this.paymentDataRequest = request;
        return this;
    }

    /**
     * Get payment data request details
     * @return PaymentDataRequest instance
     */
    public PaymentDataRequest getPaymentDataRequest(){
        return paymentDataRequest;
    }

    /**
     * Add payment result process listener
     * @param listener PaymentResultListener to be set
     * @return FurahitechPay instance
     */
    public FurahitechPay setPaymentResultListener(PaymentResultListener listener){
        if(listeners != null && !listeners.contains(listener)){
            this.listeners.add(listener);
        }
        return this;
    }

    /**
     * Remove payment result process listener
     * @param listener PaymentResultListener to be set
     */
    public void removePaymentResultListener(PaymentResultListener listener){
        if(listeners != null && listeners.contains(listener)){
            this.listeners.remove(listener);
        }
    }

    /**
     * Set base URL where your server logic exists
     * @param baseUrl Remote URL
     * @return FurahitechPay instance
     */
    public FurahitechPay setBaseUrl(String baseUrl){
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Set stripe publishable key for card payment
     * @param stripePublishableKey Stripe publishable key
     * @return FurahitechPay instance
     */
    public FurahitechPay setStripePublishableKey(String stripePublishableKey){
        this.stripePublishableKey = stripePublishableKey;
        return this;
    }

    /**
     * Get stripe publishable key
     * @return publishable key
     */
    public String getStripePublishableKey() {
        return stripePublishableKey;
    }

    /**
     * Get base URL when your server logic exists
     * @return Server's base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Restrict which gateways will be used for payment
     * @param gateway List of gateways to be used
     * @return FurahitechPay instance
     */
    public FurahitechPay setSupportedGateway(Enum ... gateway){
        this.supportedGateWay = Arrays.asList(gateway);
        return this;
    }

    /**
     * Get all qualified payment gateway
     * @return List of all supported gateway
     */
    public List<Enum> getSupportedGateWay() {
        return supportedGateWay;
    }


    /**
     * Acknowledge that you have received payment result
     * @param paymentResult Payment result received
     */
    public void acknowledgePaymentResult(PaymentResult paymentResult){
        new RemoteCalls(context, ThreadExecutor.getInstance(), ThreadManager.getInstance())
                .acknowledgePayment(paymentResult, new ConnectionRequestListener() {
                    @Override
                    public void onRequestFailed(String message) {
                        Log.e(FurahitechPay.class.getSimpleName(),message);
                    }

                    @Override
                    public void onRequestCompleted(FurahitechResponse response) {
                        Log.d(FurahitechPay.class.getSimpleName(),response.getData());
                    }

                    @Override
                    public void onNetworkError() {
                        Log.e(FurahitechPay.class.getSimpleName(),"Network error");
                    }
                });
    }

    /**
     * Destroy FurahitechPay instance
     */
    public synchronized void destroy(){
        if(instance != null){
            instance.listeners.clear();
            instance.paymentDataRequest = null;
            instance.baseUrl = null;
            instance = null;

        }
    }

    /**
     * Start transaction
     */
    public void pay(){
        if(context != null){
            if(paymentDataRequest != null){
                if(baseUrl.isEmpty()){
                    throw new FurahitechException("Missing Base URL",
                            "Provide payment endpoint base URL");
                }else{
                   if(listeners.size() == 0){
                       throw new FurahitechException("Missing Payment result listener",
                               "Set your payment result listener");
                   }else{
                       if(paymentDataRequest.getDescription().isEmpty()){
                           throw new FurahitechException("Missing payment description",
                                   "Provide payment description before starting your transaction");
                       }else{
                           boolean isValidClientDetails = !paymentDataRequest.getFirstName().isEmpty()
                                   && !paymentDataRequest.getLastName().isEmpty()
                                   && !paymentDataRequest.getEmailAddress().isEmpty()
                                   && paymentDataRequest.getAmount()!= 0
                                   && paymentDataRequest.getGatewayType()!= null;
                           if(paymentDataRequest.getLanguage() != null){
                               if(isValidClientDetails){
                                   Intent paymentIntent;
                                   OneSignal.sendTag("clientId",paymentDataRequest.getEmailAddress());
                                   OneSignal.setEmail(paymentDataRequest.getEmailAddress());

                                   if(paymentDataRequest.getOneSignalApiKey().equals(NULL)
                                           || paymentDataRequest.getOneSignalAppId().equals(NULL)){
                                       throw new FurahitechException("Missing notification settings",
                                               "Provide one signal Api key / app ID to receive payment " +
                                                       "callbacks instant");
                                   }else{
                                       if(paymentDataRequest.getGatewayType().equals(Furahitech.GateWay.CARD)){
                                           if(stripePublishableKey.isEmpty()){
                                               throw new FurahitechException("Missing publishable key",
                                                       "Provide stripe publishable key from your stripe dashboard");
                                           }else{
                                               if(supportedGateWay.indexOf(Furahitech.GateWay.CARD) != -1){
                                                   paymentIntent = new Intent(context, CardPaymentActivity.class);
                                               }else{
                                                   throw new FurahitechException("Unsupported Gateway",
                                                           "Please make sure you include on the supported gateway" +
                                                                   " for making request");
                                               }
                                           }
                                       }else{
                                           paymentIntent = new Intent(context, MnoPaymentActivity.class);
                                       }
                                       context.startActivity(paymentIntent);
                                   }
                               }
                           }else{
                               throw new FurahitechException("Missing default language",
                                       "Please specify your default language");
                           }
                       }
                   }
                }
            }else{
                throw new FurahitechException("Missing PaymentDataRequest",
                        "Provide payment data request");
            }
        }else{
            throw new FurahitechException("Missing application context ",
                    "Provide application context");
        }
    }
}
