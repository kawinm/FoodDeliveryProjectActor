package com.example.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AgentStatus {

    Long agentId;
    String status;

    @JsonCreator
    public AgentStatus(@JsonProperty("agentId") Long agentId)
    {
        this.agentId = agentId;
        this.status = "";
    }

    public Long getAgentId() {
        return this.agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}
