package com.example;

import akka.actor.typed.javadsl.ActorContext;

import akka.actor.typed.ActorRef;

import java.util.HashMap;


import com.example.models.Item;
import com.example.models.Order;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
import akka.actor.typed.javadsl.Behaviors;


public class Delivery extends AbstractBehavior<Delivery.DeliveryCommand> {
    
    //Define members 
    HashMap<Item, Long> itemMap;
    HashMap<Long, ActorRef<Agent.AgentCommand>> agentRef;
    HashMap<Long, ActorRef<FullFillOrder.FullFillOrderCommand>> orderRef;
    Long currentOrderId = 1000L;
    

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

    // Request Order Message
    public static class RequestOrderMessage implements DeliveryCommand { 

        Order order;

        public RequestOrderMessage(Order order) {
            this.order = order;
        }
    }

    // Order Delivered Message
    public static class OrderDeliveredMessage implements DeliveryCommand { 

        Long orderId;

        public OrderDeliveredMessage(Long orderId) {
            this.orderId = orderId;
        }
    }

    // Agent Signin Message
    public static class AgentSignInMessage implements DeliveryCommand { 

        Long agentId;

        public AgentSignInMessage(Long agentId) {
            this.agentId = agentId;
        }
    }

    // Agent Signout Message
    public static class AgentSignOutMessage implements DeliveryCommand { 

        Long agentId;

        public AgentSignOutMessage(Long agentId) {
            this.agentId = agentId;
        }
    }
    
    //Constructor
    public Delivery(ActorContext<DeliveryCommand> context, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentRef) {
        super(context);
        this.itemMap = itemMap;
        this.agentRef = agentRef;
    }

    // Create method to spawn an actor
    public static Behavior<DeliveryCommand> create(HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentRef) {   

        return Behaviors.setup(context -> new Delivery(context,itemMap, agentRef));
    }

    //Create Receive Method
    @Override
    public Receive<DeliveryCommand> createReceive() {
       return newReceiveBuilder()
       .onMessage(SampleMessage.class, this::onSampleMessage)
       .onMessage(RequestOrderMessage.class, this::onRequestOrderMessage)
       .onMessage(OrderDeliveredMessage.class, this::onOrderDeliveredMessage)
       .onMessage(AgentSignInMessage.class, this::onAgentSignInMessage)
       .onMessage(AgentSignOutMessage.class, this::onAgentSignOutMessage)
       .build();
    }

    // Define Message and Signal Handlers
    public Behavior<DeliveryCommand> onSampleMessage(SampleMessage sampleMessage) {

       System.out.println(sampleMessage.message);
       return this;
    }

    // Define Signal Handler for Request Order Message
    public Behavior<DeliveryCommand> onRequestOrderMessage(RequestOrderMessage requestOrder) {

        ActorRef<FullFillOrder.FullFillOrderCommand> orderActor = getContext().spawn(FullFillOrder.create(currentOrderId, requestOrder.order, Constants.ORDER_UNASSIGNED, itemMap, agentRef), "order_"+currentOrderId);
        
        orderRef.put(currentOrderId++, orderActor);

        System.out.println(requestOrder.order.getCustId());
        return this;
     }

      // Define Message and Signal Handler for Order Delivered Message
    public Behavior<DeliveryCommand> onOrderDeliveredMessage(OrderDeliveredMessage orderDelivered) {

        System.out.println(orderDelivered.orderId);
        return this;
     }

     // Define Signal Handler for Agent SignIn Message
    public Behavior<DeliveryCommand> onAgentSignInMessage(AgentSignInMessage agentSignIn) {

        ActorRef<Agent.AgentCommand> currentAgent = agentRef.get(agentSignIn.agentId); 

        // SignIn Message send
        currentAgent.tell(new Agent.AgentSignInMessage(agentSignIn.agentId));

        System.out.println(agentSignIn.agentId);
        return this;
     }

     // Define Signal Handler for Agent SignOut Message
    public Behavior<DeliveryCommand> onAgentSignOutMessage(AgentSignOutMessage agentSignOut) {

        ActorRef<Agent.AgentCommand> currentAgent = agentRef.get(agentSignOut.agentId); 

        // Signout message send
        currentAgent.tell(new Agent.AgentSignOutMessage(agentSignOut.agentId));

        System.out.println(agentSignOut.agentId);
        return this;
     }

}
