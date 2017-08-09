package com.cityparking.pratteln.webservices.entity;

public class PaymentTransactionResponse {
    private SendUUIDResponseBody paymentTransactionResponse;

    public SendUUIDResponseBody getPaymentTransactionResponse() {
        return paymentTransactionResponse;
    }

    @SuppressWarnings("unused")
    public void setPaymentTransactionResponse(SendUUIDResponseBody paymentTransactionResponse) {
        this.paymentTransactionResponse = paymentTransactionResponse;
    }
}
