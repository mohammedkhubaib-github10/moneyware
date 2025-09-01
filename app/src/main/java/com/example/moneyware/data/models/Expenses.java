package com.example.moneyware.data.models;

public class Expenses {
    private String expense_name;
    private String date;
    private double amount;
    private String Ekey;

    public Expenses(String expense_name, String date, double amount, String Ekey) {
        this.expense_name = expense_name;
        this.date = date;
        this.amount = amount;
        this.Ekey = Ekey;
    }

    public String getExpense_name() {
        return expense_name;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getEkey() {
        return Ekey;
    }
}
