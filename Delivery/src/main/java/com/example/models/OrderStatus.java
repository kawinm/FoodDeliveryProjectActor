package com.example.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderStatus {
    
    Long orderId;
    String status;

    @JsonCreator
    public OrderStatus( @JsonProperty("orderId") Long orderId, @JsonProperty("status") String status)
    {
        this.orderId = orderId;
        this.status = status;
    }
    
    public Long getOrderId() {
		return this.orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
