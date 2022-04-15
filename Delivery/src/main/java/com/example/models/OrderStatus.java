package com.example.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({"orderId","status","agentId"})
public class OrderStatus {
    
    Long orderId;
    String status;
	Long agentId;

    @JsonCreator
    public OrderStatus( @JsonProperty("orderId") Long orderId, @JsonProperty("status") String status, @JsonProperty("agentId") Long agentId)
    {
        this.orderId = orderId;
        this.status = status;
		this.agentId = agentId;
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

	public Long getAgentId() {
		return this.agentId;
	}

	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}

}
