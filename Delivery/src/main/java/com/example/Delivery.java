package com.example;

import akka.actor.typed.javadsl.ActorContext;

import java.util.HashMap;

import com.example.models.Item;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
import akka.actor.typed.javadsl.Behaviors;


public class Delivery extends AbstractBehavior<Delivery.DeliveryCommand> {
    
    //Define members 
    HashMap<Item, Long> itemMap;
    HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap;
    HashMap<Long, ActorRef<FullFillOrder.AgentCommand>> orderMap;

    // Define the message type which 
    // actor can process
    interface DeliveryCommand {}

    // Define messsages here
    public static class SampleMessage implements DeliveryCommand { 
        String message;
        public SampleMessage(String message) {
            this.message = message;
        }
    }
    
    //Constructor
    public Delivery(ActorContext<DeliveryCommand> context, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap) {
        super(context);
        this.itemMap = itemMap;
        this.agentMap = agentMap;
    }

    // Create method to spawn an actor
    public static Behavior<DeliveryCommand> create(Long DeliveryId, int status) {   

        return Behaviors.setup(context -> new Delivery(context,DeliveryId,status));
    }

    //Create Receive Method
    @Override
    public Receive<DeliveryCommand> createReceive() {
       return newReceiveBuilder()
       .onMessage(SampleMessage.class, this::onSampleMessage)
       .build();
    }

    // Define Message and Signal Handlers
    public Behavior<DeliveryCommand> onSampleMessage(SampleMessage sampleMessage) {

       System.out.println(sampleMessage.message);
       return this;
    }

}
