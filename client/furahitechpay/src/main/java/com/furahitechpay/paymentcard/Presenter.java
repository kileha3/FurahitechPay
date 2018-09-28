package com.furahitechpay.paymentcard;

import com.furahitechpay.FurahitechPay;
import com.furahitechpay.data.ConnectionRequestListener;
import com.furahitechpay.data.FurahitechResponse;
import com.furahitechpay.data.PaymentResult;
import com.furahitechpay.data.remote.RemoteCalls;
import com.furahitechpay.interfaces.PaymentResultListener;
import com.furahitechpay.util.Furahitech;
import com.furahitechpay.util.executors.ThreadExecutor;
import com.furahitechpay.util.executors.ThreadManager;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class Presenter implements Contract.Presenter,ConnectionRequestListener, PaymentResultListener{
    private Contract.View view;
    private FurahitechPay furahitechPay;
    private PaymentResult paymentResult = null;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
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
    }
    @Override
    public void onStart() {
        if(view != null){
            view.enablePaymentButton(false);
            furahitechPay = FurahitechPay.getInstance();
            FurahitechPay.getInstance().setPaymentResultListener(this);
        }
    }

    @Override
    public void doPayment() {
        if(view != null){
            new RemoteCalls(view.getContext(), ThreadExecutor.getInstance(),
                    ThreadManager.getInstance())
                    .authenticatePayments(this);
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
            synchronized (executor){
                view.showProgress(false);
                executor.schedule(paymentProcessor,1, TimeUnit.MINUTES);
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
    public void onPaymentCompleted(PaymentResult paymentResult) {
        if(view != null){
            this.paymentResult = paymentResult;
            synchronized (executor){
                handleShuttingDownExecutorService();
            }
        }
    }



    @Override
    public void checkCardValidity(CardInputWidget widget) {
        if(view != null){
            Card card = widget.getCard();
            view.enablePaymentButton(card !=null && card.validateCard());
        }
    }

    @Override
    public void validateCard(CardInputWidget widget) {
        if(view !=null){
            view.showProgress(true);
            Stripe stripe=new Stripe(view.getContext());
            stripe.setDefaultPublishableKey(furahitechPay.getStripePublishableKey());
            stripe.createToken(widget.getCard(), new TokenCallback() {
                @Override
                public void onError(Exception error) {
                    view.hideProgress();
                    view.showRequestFailure(error.getMessage());
                }
                @Override
                public void onSuccess(Token token) {
                    furahitechPay.getPaymentDataRequest().setCardToken(token.getId());
                    doPayment();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        if(view != null){
            view = null;
        }
    }

    private void handleShuttingDownExecutorService(){
        view.hideProgress();
        FurahitechPay.getInstance().removePaymentResultListener(this);
        executor.shutdownNow();
        view.showCompletionDialog(paymentResult);
    }
}
