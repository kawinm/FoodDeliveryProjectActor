package com.example;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
import akka.actor.typed.javadsl.Behaviors;


public class FullFillOrder extends AbstractBehavior<FullFillOrder.FullFillOrderCommand> {
    
    //Define members 
    Long FullFillOrderId;
    int status;
    int orderId;

    // Define the message type which 
    // actor can process
    interface FullFillOrderCommand {}

    // Define messsages here
    public static class SampleMessage implements FullFillOrderCommand { 
        String message;
        public SampleMessage(String message) {
            this.message = message;
        }
    }
    
    //Constructor
    public FullFillOrder(ActorContext<FullFillOrderCommand> context, Long FullFillOrderId, int status) {
        super(context);
        this.FullFillOrderId = FullFillOrderId;
        this.status = status;
        this.orderId = -1;
    }

    // Create method to spawn an actor
    public static Behavior<FullFillOrderCommand> create(Long FullFillOrderId, int status) {   

        return Behaviors.setup(context -> new FullFillOrder(context,FullFillOrderId,status));
    }

    //Create Receive Method
    @Override
    public Receive<FullFillOrderCommand> createReceive() {
       return newReceiveBuilder()
       .onMessage(SampleMessage.class, this::onSampleMessage)
       .build();
    }

    // Define Message and Signal Handlers
    public Behavior<FullFillOrderCommand> onSampleMessage(SampleMessage sampleMessage) {

       System.out.println(sampleMessage.message);
       return this;
    }

}
