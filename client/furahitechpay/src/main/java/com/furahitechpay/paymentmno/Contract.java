package com.furahitechpay.paymentmno;

import com.furahitechpay.BasePresenter;
import com.furahitechpay.BaseView;

interface Contract{

    interface View extends BaseView{

        void updateGatewayIcon(int gatewayIconResource);

        void showUnsupportedGateway();

        void showSupported(int viewId);

    }

    interface Presenter extends BasePresenter{

        void checkStatus();

        void checkGateway(String phoneNumber , int charCount);
    }
}
