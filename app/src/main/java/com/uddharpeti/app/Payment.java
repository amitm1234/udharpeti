package com.uddharpeti.app;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Payment {
    private String id;
    private double amount;
    private String paymentType;
    private String paymentDate;
    private String note;

    public Payment() {
        // Required for Firestore
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
