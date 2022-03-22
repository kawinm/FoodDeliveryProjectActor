package com.example;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
import akka.actor.typed.javadsl.Behaviors;


public class Agent extends AbstractBehavior<Agent.AgentCommand> {
    
    // Define members 
    Long agentId;
    int status;

    int SignOutLock;
    
    // Define the message type which 
    // actor can process
    interface AgentCommand {}

    // Define messsages here
    public static class SampleMessage implements AgentCommand { 
        String message;
        public SampleMessage(String message) {
            this.message = message;
        }
    }

    // Get Agent Status Message
    public static class getAgentStatusMessage implements AgentCommand { 

        public getAgentStatusMessage() { }
    }

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
       .onMessage(SampleMessage.class, this::onSampleMessage)
       .onMessage(getAgentStatusMessage.class, this::onGetAgentStatusMessage)
       .onMessage(AgentSignInMessage.class, this::onAgentSignInMessage)
       .onMessage(AgentSignOutMessage.class, this::onAgentSignOutMessage)
       .build();
    }

    // Define Message and Signal Handlers
    public Behavior<AgentCommand> onSampleMessage(SampleMessage sampleMessage) {

       System.out.println(sampleMessage.message);
       return this;
    }

    // Define Signal Handler for Agent SignIn Message
    public Behavior<AgentCommand> onGetAgentStatusMessage(getAgentStatusMessage agentStatus) {

        //System.out.println(agentSignIn.agentId);
        return this;
     }

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
}
