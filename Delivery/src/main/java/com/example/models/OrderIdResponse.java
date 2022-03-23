package com.example.models;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OrderIdResponse {
   
    Long orderId;

    @JsonCreator
    public OrderIdResponse(Long orderId)
    {
        this.orderId = orderId;
    }
    
    public Long getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
