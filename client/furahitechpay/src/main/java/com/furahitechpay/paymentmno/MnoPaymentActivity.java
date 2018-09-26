package com.furahitechpay.paymentmno;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.furahitechpay.FurahitechPay;
import com.furahitechpay.R;
import com.furahitechpay.data.PaymentDataRequest;
import com.furahitechpay.data.PaymentResult;
import com.furahitechpay.interfaces.StatusDialogClickListener;
import com.furahitechpay.paymentstatus.PaymentStatusFragment;
import com.furahitechpay.util.Furahitech;

import br.com.sapereaude.maskedEditText.MaskedEditText;
import dmax.dialog.SpotsDialog;

import static com.furahitechpay.util.Furahitech.formatPrice;

public class MnoPaymentActivity extends AppCompatActivity implements Contract.View ,
        StatusDialogClickListener{

    private Presenter presenter;
    private Toolbar toolbar;
    private PaymentDataRequest request;
    private ImageView gatewayIcon;
    private Button startTransaction;
    private AlertDialog alertDialog;
    private CoordinatorLayout coordinatorLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mno_payment);
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

    @Override
    protected void onStart() {
        super.onStart();
        if(presenter != null){
           presenter.checkStatus();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
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
        final MaskedEditText phoneNumber = findViewById(R.id.phone_number);
        gatewayIcon = findViewById(R.id.payment_gateway);
        startTransaction = findViewById(R.id.start_transaction);
        coordinatorLayout = findViewById(R.id.coordination_layout);

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

        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                presenter.checkGateway(phoneNumber.getRawText(),phoneNumber.getRawText().length());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        phoneNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        hideKeyboard();
                    }
                }
                return false;
            }
        });

        startTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request.setPhoneNumber("255" + phoneNumber.getRawText());
                presenter.doPayment();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(MnoPaymentActivity.this);
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
    public void updateGatewayIcon(int gatewayIconResource) {
        this.gatewayIcon.setImageResource(gatewayIconResource);
    }

    @Override
    public void showUnsupportedGateway() {
        hideKeyboard();
        String message = "We currently don't support your mobile carrier, try other ones";
        Snackbar.make(coordinatorLayout,message,Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showSupported(int viewId) {
        findViewById(viewId).setVisibility(View.VISIBLE);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finishProcess();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(presenter != null){
            presenter.onDestroy();
            FurahitechPay.getInstance().destroy();
        }
        super.onDestroy();
    }
}
