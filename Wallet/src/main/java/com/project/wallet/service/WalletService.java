package com.project.wallet.service;
import java.io.File;
import java.util.*;

import com.project.wallet.model.CustomerWallet;

public class WalletService 
{

    private HashMap<Long,Long> wallet;
    private HashMap<Long,Long> initialData;
    
    public WalletService()
    {
        wallet = new HashMap<>();
        initialData = new HashMap<>();
        
        long wallet_amount = 0;
        List<Long> customers = new ArrayList<Long>();  /* Temporary list to hold customer ids */
        try{
            String userDirectory = new File("").getAbsolutePath();
            //System.out.println(userDirectory);
            File datafile = new File(userDirectory + "/initialData.txt");
            Scanner myReader = new Scanner(datafile);
            int count = 0 ;
            String fourstar = new String("****");
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(data.equals(fourstar)) /* Ignore reading the Item Catalogue */
                {
                    count+=1;
                }
                if(count==2) /* Start Reading Customer data */
                {
                    while(myReader.hasNextLine())
                    {
                        data = myReader.nextLine();
                        if(data.equals(fourstar))
                        {
                            /* Get the Initial Balance for all customers*/
                            String walletAmountString = myReader.nextLine();
                            wallet_amount = Long.parseLong(walletAmountString);
                            break;
                        }
                        else
                        {
                            /* Add customers to the temporary customers list */
                            customers.add(Long.parseLong(data));
                        }
                    }
                }
            }
            myReader.close();
        }
        catch(Exception e)
        {
            System.out.println("Error Opening File");
            e.printStackTrace();
        }
        
        if(customers.size()>0)
        {
            for(int i=0;i<customers.size();++i)
            {   
                /* Add all the customers along with their initial balance in a HashMap*/
                wallet.put(customers.get(i), wallet_amount);
            }
            initialData.putAll(wallet);
        }
        System.out.println("Customer Wallets Initialized");

    }

    /* Adds amount to the wallet of customer whose customer id is custId */
    public synchronized boolean addBalance(long custId, long amount)
    {
        if(wallet.containsKey(custId))
            wallet.put(custId, wallet.get(custId) + amount);
        System.out.println("/addBalance  " + custId + " successful " + " New Balance = " + wallet.get(custId));
        return true;
    }

    /* Deducts amount from the wallet of customer whose customer id is custId */
    public synchronized boolean deductBalance(long custId, long amount)
    {
        if(wallet.containsKey(custId))
        {
            if(wallet.get(custId) < amount)
            {
                /* Customers balance is less than the amount to be deducted */
                System.out.println("/deductBalance " + custId + "Unsuccessful Balance = " + wallet.get(custId));
                return false;
            }
            else
            {
                /* Perform Deduction */

                wallet.put(custId, wallet.get(custId)-amount);
                System.out.println(" /deductBalance " + custId + " Successful "+ " New Balance = " + wallet.get(custId));

                return true;
            }
        }
        else
        {
            return false;
        } 
    }

     /* Obtain the balance information of customer custId */
    public synchronized CustomerWallet getData(long custId)
    {
        if(!wallet.containsKey(custId))
        {
            /* The customer ID is Invalid */
            System.out.println("/getData " + custId + " UnSuccessful");
            return null;
        }
        else
        {
            /*Wrap the customer data*/
            CustomerWallet customerData = new CustomerWallet(custId, wallet.get(custId));
            System.out.println("/getData " + custId + " Successful");
            return customerData;
        }
    }

    /* Reinitialize all the customers balance to their initialBalance*/
    public synchronized boolean reInitialize()
    {
        /*Reinitialize the wallet hashmap to the initial balance*/
        wallet = new HashMap<>();
        wallet.putAll(initialData);
        System.out.println("/reInitialize Successful");
        return true;

    }

}
