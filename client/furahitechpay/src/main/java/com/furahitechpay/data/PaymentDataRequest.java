package com.furahitechpay.data;

import com.furahitechpay.util.Furahitech;

public class PaymentDataRequest {
    public static final String NULL = "null";
    private int amount;
    private int taxAmount = 0;
    private int feeAmount = 0;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private String cardToken;
    private String description;
    private String productName = NULL;
    private String productId = NULL;
    private String remoteDataSource;
    private String dataReceiver = NULL;
    private String oneSignalApiKey = NULL;
    private String oneSignalAppId = NULL;
    private Enum gatewayType;
    private Enum language = Furahitech.Language.SWAHILI;
    private Enum paymentStatus = Furahitech.PaymentStatus.NOT_STARTED;
    private boolean environment = false;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(int taxAmount) {
        this.taxAmount = taxAmount;
    }

    public int getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(int feeAmount) {
        this.feeAmount = feeAmount;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Enum getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(Enum gatewayType) {
        this.gatewayType = gatewayType;
    }

    public Enum getLanguage() {
        return language;
    }

    public void setLanguage(Enum language) {
        this.language = language;
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLiveApi() {
        return environment;
    }

    public void useLiveApi(boolean environment) {
        this.environment = environment;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getRemoteDataSource() {
        return remoteDataSource;
    }

    public void setRemoteDataSource(String remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    public Enum getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Enum paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getDataReceiver() {
        return dataReceiver;
    }

    public void setDataReceiver(String dataReceiver) {
        this.dataReceiver = dataReceiver;
    }

    public String getOneSignalApiKey() {
        return oneSignalApiKey;
    }

    public void setOneSignalApiKey(String oneSignalApiKey) {
        this.oneSignalApiKey = oneSignalApiKey;
    }

    public String getOneSignalAppId() {
        return oneSignalAppId;
    }

    public void setOneSignalAppId(String oneSignalAppId) {
        this.oneSignalAppId = oneSignalAppId;
    }
}
