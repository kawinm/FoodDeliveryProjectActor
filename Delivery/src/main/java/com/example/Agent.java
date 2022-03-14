package com.example;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
import akka.actor.typed.javadsl.Behaviors;


public class Agent extends AbstractBehavior<Agent.AgentCommand>
{
    //Define members 
    Long agentId;
    int status;
    int orderId;

    // Define the message type which 
    // actor can process
    interface AgentCommand {}

    // Define messsages here
    public static class SampleMessage implements AgentCommand
    { 
        String message;
        public SampleMessage(String message)
        {
            this.message = message;
        }
    }
    
    //Constructor
    public Agent(ActorContext<AgentCommand> context, Long agentId, int status)
    {
        super(context);
        this.agentId = agentId;
        this.status = status;
        this.orderId = -1;
    }

    // Create method to spawn an actor
    public static Behavior<AgentCommand> create(Long agentId, int status)
    {   
        return Behaviors.setup(context -> new Agent(context,agentId,status));
    }
    //Create Receive Method
    @Override
    public Receive<AgentCommand> createReceive() {
       return newReceiveBuilder()
       .onMessage(SampleMessage.class, this::onSampleMessage)
       .build();
    }

    // Define Message and Signal Handlers
    public Behavior<AgentCommand> onSampleMessage(SampleMessage sampleMessage)
    {
       System.out.println(sampleMessage.message);
       return this;
    }

}
