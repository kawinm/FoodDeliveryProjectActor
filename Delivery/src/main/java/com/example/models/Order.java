package com.example.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {

    public Long restId, itemId, qty, custId;
    @JsonCreator
    public Order(@JsonProperty("restId") Long restId, 
    @JsonProperty("itemId") Long itemId, 
    @JsonProperty("qty") Long qty, 
    @JsonProperty("custId") Long custId) {
        this.restId = restId;
        this.itemId = itemId;
        this.qty    = qty;
        this.custId = custId;
    }

    public Long getRestId() {
        return restId;
    }

    public void setRestId(Long restId) {
        this.restId = restId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public Long getQty() {
        return qty;
    }

    public void setQty(Long qty) {
        this.qty = qty;
    }
    
}