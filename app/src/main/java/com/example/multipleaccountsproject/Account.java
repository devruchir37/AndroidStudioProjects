package com.example.multipleaccountsproject;

public class Account {
    String accountName;
    String accountNumber;
    String accountType;
    String isUpiLiteEnabled;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getIsUpiLiteEnabled() {
        return isUpiLiteEnabled;
    }

    public void setIsUpiLiteEnabled(String isUpiLiteEnabled) {
        this.isUpiLiteEnabled = isUpiLiteEnabled;
    }
}