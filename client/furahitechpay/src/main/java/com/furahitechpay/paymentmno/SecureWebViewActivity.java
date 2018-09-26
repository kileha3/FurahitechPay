package com.furahitechpay.paymentmno;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.furahitechpay.FurahitechPay;
import com.furahitechpay.R;
import com.furahitechpay.data.PaymentDataRequest;
import com.furahitechpay.util.Furahitech;

public class SecureWebViewActivity extends AppCompatActivity{
    public static final String REDIRECTION_URL_TAG = "redirection_url_tag";
    private ProgressBar progressDialog;
    private WebView secureWebView;
    private boolean finished = false;
    private String securePageURL;
    private ImageView emptyLoading;
    private Toolbar toolbar;
    private PaymentDataRequest dataRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_payment);
        securePageURL = getIntent().getStringExtra(REDIRECTION_URL_TAG);
        dataRequest = FurahitechPay.getInstance().getPaymentDataRequest();
        setUpViews();
        setToolbar();
        setUpWebView();
    }

    public void setUpViews() {
        TextView toolBarTitle = findViewById(R.id.tool_bar_title);
        toolbar = findViewById(R.id.tool_bar);
        secureWebView = findViewById(R.id.secure_web);
        emptyLoading = findViewById(R.id.emptyLoading);
        progressDialog = findViewById(R.id.progressBar);

        toolBarTitle.setText(R.string.secure_payment);
    }

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


    private void setUpWebView(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            secureWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            secureWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        secureWebView.clearCache(true);

        secureWebView.clearHistory();
        secureWebView.getSettings().setJavaScriptEnabled(true);
        secureWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        secureWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.progressDialog.setProgress(0);
        this.progressDialog.setMax(100);
        secureWebView.setBackgroundColor(Color.TRANSPARENT);
        renderWebPage();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && secureWebView.canGoBack()) {
            secureWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        secureWebView.clearHistory();
        secureWebView.clearFormData();
    }

    @Override
    public void onBackPressed() {
        showCancelDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            showCancelDialog();
        }
        return true;
    }



    private class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            //Strapping HTML page and extract keyword for the transaction status
            if(html.toLowerCase().contains(getString(R.string.success_swahili).toLowerCase())
                    || html.toLowerCase().contains(getString(R.string.success_english).toLowerCase())){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!finished){
                            dataRequest.setPaymentStatus(Furahitech.PaymentStatus.PRE_SUCCESS);
                            finished=true;
                            finish();
                        }
                    }
                });

            }else if(html.toLowerCase().contains(getString(R.string.failure_swahili).toLowerCase())
                    || html.toLowerCase().contains(getString(R.string.failure_swahili_not_available).toLowerCase())
                    || html.toLowerCase().contains(getString(R.string.failure_blocked_eng).toLowerCase())
                    || html.toLowerCase().contains(getString(R.string.failure_blocked_swa).toLowerCase())
                    || html.toLowerCase().contains(getString(R.string.failure_swahili_no_account).toLowerCase())
                    || html.toLowerCase().contains(getString(R.string.failure_expired).toLowerCase())
                    || html.toLowerCase().contains(getString(R.string.failure_no_enough_funds).toLowerCase())
                    || html.toLowerCase().equals(getString(R.string.failure_networks).toLowerCase())
                    || html.toLowerCase().contains(getString(R.string.redirection).toLowerCase())
                    || html.toLowerCase().contains(getString(R.string.session_termination_message).toLowerCase())){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!finished){
                            dataRequest.setPaymentStatus(Furahitech.PaymentStatus.FAILURE);
                            finished = true;
                            finish();
                        }
                    }
                });
            }else if(html.toLowerCase().contains("webpage not available") || html.toLowerCase().contains("the webpage at ")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!finished){
                            dataRequest.setPaymentStatus(Furahitech.PaymentStatus.FAILURE);
                            finished = true;
                            finish();
                        }
                    }
                });
            }

        }

    }

    protected void renderWebPage(){
        secureWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                progressDialog.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressDialog.setVisibility(View.GONE);
                secureWebView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(!finished){
                    view.loadUrl(url);
                }
                return true;
            }
        });
        secureWebView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int newProgress){
                progressDialog.setProgress(newProgress);
                if(newProgress == 100){
                    emptyLoading.setVisibility(View.GONE);
                    progressDialog.setVisibility(View.GONE);
                }
            }
        });
        secureWebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        secureWebView.loadUrl(securePageURL);
    }

    public void showCancelDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SecureWebViewActivity.this);
        builder.setTitle("Cancel Payment?");
        builder.setMessage("This will stop all the payment processes going on right now, would you like to proceed?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataRequest.setPaymentStatus(Furahitech.PaymentStatus.CANCELLED);
                finished = true;
                dialog.dismiss();
                finish();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
