package com.furahitechpay.paymentcard;

import com.furahitechpay.BasePresenter;
import com.furahitechpay.BaseView;
import com.stripe.android.view.CardInputWidget;

interface Contract{

    interface View extends BaseView{

    }

    interface Presenter extends BasePresenter{

        void checkCardValidity(CardInputWidget widget);

        void validateCard(CardInputWidget widget);
    }
}
