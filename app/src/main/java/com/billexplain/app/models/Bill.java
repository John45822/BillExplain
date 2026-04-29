package com.billexplain.app.models;

public class Bill {
    public int id;
    public String month;
    public int year;
    public float totalAmount;
    public float generationCharge;
    public float transmissionCharge;
    public float distributionCharge;
    public float otherCharges;
    public String status;
    public String dueDate;

    public String getMonthYear() { return month + " " + year; }
}
