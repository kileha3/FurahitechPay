package com.furahitechpay.exceptions;
/**
 * FurahitechPay exception handler
 */
public class FurahitechException extends RuntimeException {
    public FurahitechException(String message, String cause) {
        super(message, new Throwable(cause));
    }
}