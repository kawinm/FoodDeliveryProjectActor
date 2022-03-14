package com.project.wallet.model;
/*
    Wrapper class for holding the JSON response for /balance/{num}
*/
public class CustomerWallet
{
    long custId;
    long balance;

    public CustomerWallet(long custId, long balance)
    {
        this.custId = custId;
        this.balance = balance;
    }

    public long getCustId()
    {
        return this.custId;
    }

    public long getBalance()
    {
        return this.balance;
    }
}
