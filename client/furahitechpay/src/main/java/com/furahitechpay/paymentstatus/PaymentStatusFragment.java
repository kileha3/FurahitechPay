package com.furahitechpay.paymentstatus;


import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.furahitechpay.FurahitechPay;
import com.furahitechpay.R;
import com.furahitechpay.data.PaymentResult;
import com.furahitechpay.interfaces.StatusDialogClickListener;
import com.furahitechpay.util.Furahitech;
import com.furahitechpay.uxi.TextJustification;

/**
 * A simple {@link Fragment} subclass.
 */
public class PaymentStatusFragment extends DialogFragment {
    public static final String PAYMENT_RESULT_TAG = "payment_result_tag";
    private StatusDialogClickListener listener;
    public PaymentStatusFragment newInstance(PaymentResult paymentResult){
        Bundle bundle = new Bundle();
        bundle.putSerializable(PAYMENT_RESULT_TAG, paymentResult);
        PaymentStatusFragment fragment = new PaymentStatusFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    public void setListener(StatusDialogClickListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.furahitech_fragment_payment_status, container, false);
        TextView paymentMemo = view.findViewById(R.id.payment_memo);
        TextView statusText = view.findViewById(R.id.payment_status);
        ImageView paymentStatusIcon = view.findViewById(R.id.payment_status_icon);
        Button closeDialog = view.findViewById(R.id.close_dialog);
        FrameLayout paymentStatusHolder = view.findViewById(R.id.payment_status_holder);
        PaymentResult result = ((PaymentResult)getArguments().getSerializable(PAYMENT_RESULT_TAG));
        Enum status = result.getStatus();
        int paymentStatusColors= ContextCompat.getColor(getActivity(),
                status.equals(Furahitech.PaymentStatus.SUCCESS)
                ? R.color.colorSuccess : status.equals(Furahitech.PaymentStatus.TIMEOUT)
                ? android.R.color.black: R.color.colorError);

        int paymentStatusIconsRes= status.equals(Furahitech.PaymentStatus.SUCCESS)
                ? R.drawable.ic_check_white_24dp: status.equals(Furahitech.PaymentStatus.TIMEOUT)
                ? R.drawable.ic_timer_off_white_24dp:R.drawable.ic_close_white_24dp;
        String paymentStatusLabel= status.equals(Furahitech.PaymentStatus.SUCCESS) ?
                "Success": status.equals(Furahitech.PaymentStatus.TIMEOUT) ? "Timeout":"Failure";
        String message = result.getMessage();
        paymentMemo.setText(Html.fromHtml(message));
        statusText.setText(paymentStatusLabel);
        TextJustification.justify(statusText);
        paymentStatusIcon.setImageResource(paymentStatusIconsRes);
        GradientDrawable bgShape = (GradientDrawable)paymentStatusHolder.getBackground();
        bgShape.setColor(paymentStatusColors);
        closeDialog.setBackgroundColor(paymentStatusColors);
        setCancelable(false);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                listener.onDialogClosed();
            }
        });
        return view;
    }


    /**
     * Setting up extra dialog views details on resuming it
     */
    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        super.onResume();
    }

}
