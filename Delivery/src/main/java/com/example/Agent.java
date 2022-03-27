package com.example;

import akka.actor.typed.javadsl.ActorContext;

import com.example.models.AgentStatus;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
import ch.qos.logback.core.joran.conditional.ElseAction;
import akka.actor.typed.javadsl.Behaviors;


public class Agent extends AbstractBehavior<Agent.AgentCommand> {
    
    // Define members 
    Long agentId;
    int status;

    int SignOutLock;

    // Define the message type which 
    // actor can process
    interface AgentCommand {}

    // Agent Signin Message
    public static class AgentSignInMessage implements AgentCommand { 

        Long agentId;

        public AgentSignInMessage(Long agentId) {
            this.agentId = agentId;
        }
    }

    // Agent Signout Message
    public static class AgentSignOutMessage implements AgentCommand { 

        Long agentId;

        public AgentSignOutMessage(Long agentId) {
            this.agentId = agentId;
        }
    }
    
    
    // Get Agent Status Message
    public static class GetAgentStatusMessage implements AgentCommand { 
        
        ActorRef<GetAgentStatusResponse> agentStatusResponse;
        public GetAgentStatusMessage(ActorRef<GetAgentStatusResponse> agentStatusResponse) {
            this.agentStatusResponse = agentStatusResponse;
        }
    }

    public static class GetAgentStatusResponse {
        AgentStatus agentStatus;
        
        public GetAgentStatusResponse(AgentStatus agentStatus)
        {
            this.agentStatus = agentStatus;
        }
    }
  

    //Constructor
    public Agent(ActorContext<AgentCommand> context, Long agentId, int status) {
        super(context);
        this.agentId = agentId;
        this.status = status;
        this.SignOutLock = 0;
    }

    // Create method to spawn an actor
    public static Behavior<AgentCommand> create(Long agentId, int status) {   

        return Behaviors.setup(context -> new Agent(context,agentId,status));
    }

    //Create Receive Method
    @Override
    public Receive<AgentCommand> createReceive() {
       return newReceiveBuilder()
       .onMessage(AgentSignInMessage.class, this::onAgentSignInMessage)
       .onMessage(AgentSignOutMessage.class, this::onAgentSignOutMessage)
       .onMessage(GetAgentStatusMessage.class, this::onGetAgentStatusMessage)
       .build();
    }

    // Define Message and Signal Handlers
  
    // Define Signal Handler for Agent SignIn Message
    public Behavior<AgentCommand> onAgentSignInMessage(AgentSignInMessage agentSignIn) {

        if (status == Constants.AGENT_SIGNED_OUT) {
            status = Constants.AGENT_AVAILABLE;
        }

        return this;
    }

     // Define Signal Handler for Agent SignOut Message
    public Behavior<AgentCommand> onAgentSignOutMessage(AgentSignOutMessage agentSignOut) {

        this.SignOutLock = 0;

        System.out.println(agentSignOut.agentId);
        return this;
    }

     // Define Signal Handler for Agent Status Message
    public Behavior<AgentCommand> onGetAgentStatusMessage(GetAgentStatusMessage getAgentStatus) {
        AgentStatus agentStatus = new AgentStatus(this.agentId);
        if(this.status == Constants.AGENT_SIGNED_OUT)
        {
            agentStatus.setStatus("signed-out");
        }
        else if(this.status == Constants.AGENT_AVAILABLE)
        {
            agentStatus.setStatus("available");
        }
        else
        {
            agentStatus.setStatus("unavailable");

        }
        getAgentStatus.agentStatusResponse.tell(new GetAgentStatusResponse(agentStatus));
        return this;
     }

}
