package com.furahitechpay.data.remote;

import android.content.Context;

import com.furahitechpay.FurahitechPay;
import com.furahitechpay.data.ConnectionRequestListener;
import com.furahitechpay.data.FurahitechResponse;
import com.furahitechpay.data.PaymentDataRequest;
import com.furahitechpay.data.PaymentResult;
import com.furahitechpay.util.Furahitech;
import com.furahitechpay.util.executors.ThreadExecutor;
import com.furahitechpay.util.executors.ThreadManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles all network tasks like connection to all endpoints and pull / send data
 */
public class RemoteCalls {

    private ThreadExecutor executor;
    private ThreadManager manager;
    private Context context;

    public RemoteCalls(Context context, ThreadExecutor executor, ThreadManager manager){
        this.executor = executor;
        this.manager = manager;
        this.context = context;
    }

    /**
     * Authenticate tigopesa payment
     * @param listener Connection state change listener
     */
    public void authenticatePayments(final ConnectionRequestListener listener){
        if(Furahitech.isConnected(context)){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    RemoteApi endpoints = new RemoteClient().getClient().create(RemoteApi.class);
                    PaymentDataRequest request = FurahitechPay.getInstance().getPaymentDataRequest();

                    Map<String, String> params = new Gson().fromJson(
                            new Gson().toJson(request), new TypeToken<HashMap<String, String>>() {}.getType()
                    );

                    Call<FurahitechResponse> mResponse = request.getGatewayType().equals(Furahitech.GateWay.TIGOPESA)
                            ? endpoints.authenticateTigoPesa((HashMap<String, String>) params):
                            ( request.getGatewayType().equals(Furahitech.GateWay.CARD)
                            ? endpoints.authenticateStripe((HashMap<String, String>) params)
                            : endpoints.authenticateMpesa((HashMap<String, String>) params));

                    mResponse.enqueue(new Callback<FurahitechResponse>() {
                        @Override
                        public void onResponse(Call<FurahitechResponse> call, final Response<FurahitechResponse> response) {
                            manager.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(response.body() != null){
                                        listener.onRequestCompleted(response.body());
                                    }else{
                                        listener.onRequestFailed("Request failed when, try again later");
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<FurahitechResponse> call, final Throwable t) {
                            manager.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onRequestFailed(t.getLocalizedMessage());
                                }
                            });
                        }
                    });

                }
            });
        }else{
            listener.onNetworkError();
        }
    }

    /**
     * Acknowledge that you have received payment status, this will delete temporary logged file
     * @param listener Connection state change listener
     */
    public void acknowledgePayment(final PaymentResult result, final ConnectionRequestListener listener){
        if(Furahitech.isConnected(context)){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    RemoteApi endpoints = new RemoteClient().getClient().create(RemoteApi.class);

                    Call<FurahitechResponse> mResponse = endpoints.acknowledgePaymentResult(result.getTransactionRef());

                    mResponse.enqueue(new Callback<FurahitechResponse>() {
                        @Override
                        public void onResponse(Call<FurahitechResponse> call, final Response<FurahitechResponse> response) {
                            manager.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(response.body() != null){
                                        listener.onRequestCompleted(response.body());
                                    }else{
                                        listener.onRequestFailed("Request failed when, try again later");
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<FurahitechResponse> call, final Throwable t) {
                            manager.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onRequestFailed(t.getLocalizedMessage());
                                }
                            });
                        }
                    });

                }
            });
        }else{
            listener.onNetworkError();
        }
    }
}
