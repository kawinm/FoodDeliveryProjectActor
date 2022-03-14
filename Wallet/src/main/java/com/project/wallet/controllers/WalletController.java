package com.project.wallet.controllers;

import com.project.wallet.model.CustomerWallet;
import com.project.wallet.model.TransactionData;
import com.project.wallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController 
{
    WalletService walletService;

    public WalletController()
    {
        walletService = new WalletService();
    }

    @PostMapping("/addBalance")
    public ResponseEntity<String> addBalance(@RequestBody TransactionData txn)
    {
        walletService.addBalance(txn.getCustId(), txn.getAmount());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/deductBalance")
    public ResponseEntity<String> deductBalance(@RequestBody TransactionData txn)
    {
        boolean result = walletService.deductBalance(txn.getCustId(),txn.getAmount());
        
        if(result == false)
        {
            return new ResponseEntity<>(HttpStatus.GONE);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    }

    @GetMapping("/balance/{num}")
    public ResponseEntity<CustomerWallet> getBalance(@PathVariable int num)
    {
        CustomerWallet customerData;
        customerData = walletService.getData(num);
        if(customerData!=null)
        {
            return new ResponseEntity<CustomerWallet>(customerData,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PostMapping("/reInitialize")
    public ResponseEntity<String> reInitialize()
    {
        walletService.reInitialize();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
