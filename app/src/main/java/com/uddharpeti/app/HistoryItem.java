package com.uddharpeti.app;

public class HistoryItem {
    private String personName;
    private String paymentType; // "lent", "borrowed", "interest", "principal"
    private String date;
    private double amount;
    private String transactionType; // "lent" or "borrowed" (from person)

    public HistoryItem(String personName, String paymentType, String date, double amount, String transactionType) {
        this.personName = personName;
        this.paymentType = paymentType;
        this.date = date;
        this.amount = amount;
        this.transactionType = transactionType;
    }

    public String getPersonName() { return personName; }
    public String getPaymentType() { return paymentType; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public String getTransactionType() { return transactionType; }
}
