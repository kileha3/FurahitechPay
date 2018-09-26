package com.furahitechpay.paymentcard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.furahitechpay.FurahitechPay;
import com.furahitechpay.R;
import com.furahitechpay.data.PaymentDataRequest;
import com.furahitechpay.data.PaymentResult;
import com.furahitechpay.interfaces.StatusDialogClickListener;
import com.furahitechpay.paymentstatus.PaymentStatusFragment;
import com.furahitechpay.util.Furahitech;
import com.stripe.android.view.CardInputWidget;

import dmax.dialog.SpotsDialog;

import static com.furahitechpay.util.Furahitech.formatPrice;

public class CardPaymentActivity extends AppCompatActivity implements Contract.View ,
        StatusDialogClickListener{

    private Presenter presenter;
    private Toolbar toolbar;
    private PaymentDataRequest request;
    private CardInputWidget cardInputWidget;
    private Button startTransaction;
    private AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment);

        presenter = new Presenter(this);
        request = FurahitechPay.getInstance().getPaymentDataRequest();
        setUpViews();
        setToolbar();
        presenter.onStart();
        SpotsDialog.Builder builder = new SpotsDialog.Builder().setContext(this);
        builder.setTheme(R.style.Custom);
        alertDialog = builder.build();
        alertDialog.setCancelable(false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setUpViews() {
        EditText clientName = findViewById(R.id.first_name_last_name);
        toolbar = findViewById(R.id.tool_bar);
        EditText emailAddress = findViewById(R.id.email_address);
        TextView paymentDescription = findViewById(R.id.payment_description);
        TextView totalAmount = findViewById(R.id.total_amount);
        TextView extraCharges = findViewById(R.id.extra_charges);
        cardInputWidget = findViewById(R.id.card_input_widget);
        startTransaction = findViewById(R.id.start_transaction);

        totalAmount.setText(formatPrice(request.getAmount(),""));
        String charges = request.getFeeAmount() > 0 && request.getTaxAmount() == 0
                ? formatPrice(request.getFeeAmount(),"")+" Fee":
                (request.getTaxAmount() > 0 && request.getFeeAmount()== 0 ?
                        formatPrice(request.getTaxAmount(),"")+" Tax" :
                        (request.getTaxAmount() == 0 && request.getFeeAmount() == 0 ? "No extra charges":
                                formatPrice(request.getTaxAmount(),"")+" Tax and "
                                        +formatPrice(request.getFeeAmount(),"")+" Fee"));
        extraCharges.setText(charges);
        clientName.setText(request.getFirstName()+" "+request.getLastName());
        emailAddress.setText(request.getEmailAddress());
        paymentDescription.setText(request.getDescription());

        cardInputWidget.setCardInputListener(new CardInputWidget.CardInputListener() {
            @Override
            public void onFocusChange(String focusField) {
                presenter.checkCardValidity(cardInputWidget);
            }

            @Override
            public void onCardComplete() {
                presenter.checkCardValidity(cardInputWidget);
            }

            @Override
            public void onExpirationComplete() {
                presenter.checkCardValidity(cardInputWidget);
            }

            @Override
            public void onCvcComplete() {
                presenter.checkCardValidity(cardInputWidget);
            }
        });

        startTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.validateCard(cardInputWidget);
            }
        });

    }

    @Override
    public void setToolbar() {
        setSupportActionBar(toolbar);
        if(toolbar!=null){
            ActionBar actionBar = getSupportActionBar();
            if(actionBar !=null){
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }
            toolbar.setTitle("");
        }
    }

    @Override
    public void showRequestFailure(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CardPaymentActivity.this);
        builder.setTitle("Authentication Failure");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finishProcess();
            }
        });
        builder.show();
    }


    @Override
    public void showProgress(boolean isAuth) {
        if(alertDialog != null){
            alertDialog.setMessage(isAuth ? "Authenticating...": "Processing...");
            alertDialog.show();
        }
    }

    @Override
    public void hideProgress() {
        if(alertDialog != null){
            alertDialog.setMessage("");
            alertDialog.dismiss();
        }
    }

    @Override
    public void showCompletionDialog(PaymentResult paymentResult) {
        android.app.FragmentManager fm = getFragmentManager();
        PaymentStatusFragment fragment = new PaymentStatusFragment().newInstance(paymentResult);
        fragment.setListener(this);
        fragment.show(fm, "fragment_alert");
    }


    @Override
    public void enablePaymentButton(boolean enable) {
        startTransaction.setEnabled(enable);
        startTransaction.setBackgroundColor(ContextCompat.getColor(this,enable ?
                R.color.colorAccent:android.R.color.darker_gray));

        startTransaction.setTextColor(ContextCompat.getColor(this,enable ?
                R.color.colorWhite:android.R.color.black));

        if(enable){
            hideKeyboard();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void finishProcess() {
        finish();
        FurahitechPay.getInstance().destroy();
    }


    @Override
    public void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onDialogClosed() {
        FurahitechPay.getInstance()
                .getPaymentDataRequest()
                .setPaymentStatus(Furahitech.PaymentStatus.COMPLETED);
        finishProcess();
    }

    @Override
    protected void onDestroy() {
        if(presenter != null){
            presenter.onDestroy();
            FurahitechPay.getInstance().destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finishProcess();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
