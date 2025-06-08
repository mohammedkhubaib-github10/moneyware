package com.example.moneyware;

public class Budgets {
    private String budgetname;
    private double amount;
    private String key;
    private double balance;
    private double totexp;
    private String type;
    public Budgets(String budgetname,double amount,String key,double balance,double totexp,String type){
        this.budgetname=budgetname;
        this.amount=amount;
        this.key=key;
        this.balance=balance;
        this.totexp=totexp;
        this.type=type;
    }

    public String getBudgetname() {
        return budgetname;
    }

    public double getAmount() {
        return amount;
    }
    public String getBudgetKey(){return key;}
    public double getBalance(){return balance;}
    public double getTotexp(){return totexp;}
    public String getType() {
        return type;
    }
}
