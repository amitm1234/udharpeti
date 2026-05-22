package com.uddharpeti.app;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Person {
    private String id;
    private String name;
    private String phone;
    private double amount;
    private double interestRate;
    private String interestType;
    private String transactionType;
    private String startDate;
    private String notes;
    private String status;
    private String personGroupId;

    public Person() {
        // Required for Firestore
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    public String getInterestType() { return interestType; }
    public void setInterestType(String interestType) { this.interestType = interestType; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPersonGroupId() {
        return personGroupId;
    }
    public void setPersonGroupId(String personGroupId) {
        this.personGroupId = personGroupId;
    }
}
