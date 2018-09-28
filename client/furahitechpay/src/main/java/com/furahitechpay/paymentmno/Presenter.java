package com.furahitechpay.paymentmno;

import android.content.Intent;

import com.furahitechpay.FurahitechPay;
import com.furahitechpay.R;
import com.furahitechpay.data.ConnectionRequestListener;
import com.furahitechpay.data.FurahitechResponse;
import com.furahitechpay.data.PaymentResult;
import com.furahitechpay.data.remote.RemoteCalls;
import com.furahitechpay.interfaces.PaymentResultListener;
import com.furahitechpay.util.Furahitech;
import com.furahitechpay.util.executors.ThreadExecutor;
import com.furahitechpay.util.executors.ThreadManager;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.furahitechpay.paymentmno.SecureWebViewActivity.REDIRECTION_URL_TAG;
import static com.furahitechpay.util.Furahitech.getPaymentGateway;

class Presenter implements Contract.Presenter , ConnectionRequestListener, PaymentResultListener{

    private Contract.View view;
    private HashMap<Furahitech.GateWay,Integer> supportedGatewayIcons = new HashMap<>();
    private HashMap<Furahitech.GateWay,Integer> supportedGatewayViews = new HashMap<>();
    private Furahitech.GateWay gateWay = null;
    private PaymentResult paymentResult = null;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService executorProcess = Executors.newSingleThreadScheduledExecutor();

    private Runnable paymentProcessor = new Runnable() {
        @Override
        public void run() {
            if(paymentResult == null){
                paymentResult = new PaymentResult();
                paymentResult.setMessage("Unfortunately, your payment process has timed out. " +
                        "Rest assured that we will notify you when your payment status changes");
                paymentResult.setPaymentState(Furahitech.PaymentStatus.TIMEOUT);
                handleShuttingDownExecutorService();
            }

        }
    };

    Presenter(Contract.View view){
        this.view = view;
        supportedGatewayIcons.put(Furahitech.GateWay.MPESA, R.drawable.voda_icon);
        supportedGatewayIcons.put(Furahitech.GateWay.TIGOPESA,R.drawable.tigo_icon);
        supportedGatewayIcons.put(Furahitech.GateWay.NONE,R.drawable.unknown_gateway);

        supportedGatewayViews.put(Furahitech.GateWay.MPESA, R.id.mPesa);
        supportedGatewayViews.put(Furahitech.GateWay.TIGOPESA,R.id.tigoPesa);
    }

    @Override
    public void onStart() {
        if(view != null){
            view.enablePaymentButton(false);
            FurahitechPay.getInstance().setPaymentResultListener(this);

            for(Enum gateway: FurahitechPay.getInstance().getSupportedGateWay()){
                int viewId = supportedGatewayViews.containsKey(gateway) ? supportedGatewayViews.get(gateway):-1;
                if(viewId != -1){
                    view.showSupported(viewId);
                }
            }
        }
    }

    @Override
    public void checkGateway(String phoneNumber, int charCount) {
        if(view != null){
            List<Enum> supportedGatewayList = FurahitechPay.getInstance().getSupportedGateWay();
            if(charCount >=2 && charCount <= 3){
                gateWay = getPaymentGateway(phoneNumber.substring(0,2));
                if(supportedGatewayList.indexOf(gateWay) != -1){
                    FurahitechPay.getInstance().getPaymentDataRequest().setGatewayType(gateWay);
                    view.updateGatewayIcon(supportedGatewayIcons.get(gateWay));
                }else{
                    view.showUnsupportedGateway();
                }
            }else{
                if(charCount == 0){
                    view.updateGatewayIcon(supportedGatewayIcons.get(Furahitech.GateWay.NONE));
                }
            }

            if(charCount >= 8 && charCount <= 9){
                view.enablePaymentButton(charCount == 9 && supportedGatewayList.indexOf(gateWay) != -1);
            }
        }
    }

    @Override
    public void doPayment() {
        if(view != null){
            view.showProgress(true);
            new RemoteCalls(view.getContext(), ThreadExecutor.getInstance(),
                    ThreadManager.getInstance())
                    .authenticatePayments(this);
        }
    }

    @Override
    public void checkStatus() {
        if(view != null){
            Enum status = FurahitechPay.getInstance().getPaymentDataRequest().getPaymentStatus();
            if(status.equals(Furahitech.PaymentStatus.CANCELLED)){
                view.finishProcess();
            }else{
                if(!status.equals(Furahitech.PaymentStatus.NOT_STARTED)){
                    if(paymentResult == null){
                        synchronized (executor){
                            view.showProgress(false);
                            executor.schedule(paymentProcessor,1,TimeUnit.MINUTES);
                        }
                    }else{
                        handleShuttingDownExecutorService();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestFailed(String message) {
        if(view !=null){
            view.hideProgress();
            view.showRequestFailure(message);
        }
    }

    @Override
    public void onRequestCompleted(FurahitechResponse response) {
        if(view != null){
            if(gateWay.equals(Furahitech.GateWay.TIGOPESA)){
                view.hideProgress();
                Intent secureIntent = new Intent(view.getContext(),SecureWebViewActivity.class);
                secureIntent.putExtra(REDIRECTION_URL_TAG,response.getData());
                view.getContext().startActivity(secureIntent);
            }else{
                synchronized (executorProcess){
                    view.showProgress(false);
                    executorProcess.schedule(paymentProcessor,3,TimeUnit.MINUTES);
                }
            }
        }
    }

    @Override
    public void onNetworkError() {
        if(view != null){
            view.hideProgress();
            view.showRequestFailure("There was no active internet connection, " +
                    "make sure you have one before retrying");
        }
    }


    @Override
    public void onDestroy() {
        if(view != null){
            view = null;
        }
    }

    @Override
    public void onPaymentCompleted(PaymentResult paymentResult) {
        if(view != null){
            this.paymentResult = paymentResult;
            synchronized (executor){
                handleShuttingDownExecutorService();
            }
        }
    }

    private void handleShuttingDownExecutorService(){
        view.hideProgress();
        FurahitechPay.getInstance().removePaymentResultListener(Presenter.this);
        executor.shutdownNow();
        executorProcess.shutdownNow();
        view.showCompletionDialog(paymentResult);
    }
}
