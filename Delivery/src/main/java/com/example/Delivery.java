package com.example;

import akka.actor.typed.javadsl.ActorContext;

import akka.actor.typed.ActorRef;

import java.lang.ref.Cleaner.Cleanable;
import java.util.HashMap;


import com.example.models.Item;
import com.example.models.Order;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.Client;
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
        ActorRef<ClientResponse> client;
        public RequestOrderMessage(Order order, ActorRef<ClientResponse> client) {
            this.order = order;
            this.client  = client;
        }
    }

    // Order Delivered Message
    public static class OrderDeliveredMessage implements DeliveryCommand { 

        Long orderId;
        ActorRef<ClientResponse> client;
        public OrderDeliveredMessage(Long orderId, ActorRef<ClientResponse> client) {
            this.orderId = orderId;
            this.client = client;
            
        }
    }

    // Agent Signin Message
    public static class AgentSignInMessage implements DeliveryCommand { 

        Long agentId;
        ActorRef<ClientResponse> client;
        public AgentSignInMessage(Long agentId, ActorRef<ClientResponse> client) {
            this.agentId = agentId;
            this.client = client;
        }
    }

    // Agent Signout Message
    public static class AgentSignOutMessage implements DeliveryCommand { 

        Long agentId;
        ActorRef<ClientResponse> client;
        public AgentSignOutMessage(Long agentId, ActorRef<ClientResponse> client) {
            this.agentId = agentId;
            this.client = client;
        }
    }

    // Reply messages to the client
    public static class ClientResponse
    {
        String response;
        public ClientResponse(String response)
        {
            this.response = response;
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
        requestOrder.client.tell(new ClientResponse("requestOrder OK"));
        return this;
     }

      // Define Message and Signal Handler for Order Delivered Message
    public Behavior<DeliveryCommand> onOrderDeliveredMessage(OrderDeliveredMessage orderDelivered) {

        System.out.println(orderDelivered.orderId);
        orderDelivered.client.tell(new ClientResponse("Order Delivered"));
        return this;
     }

     // Define Signal Handler for Agent SignIn Message
    public Behavior<DeliveryCommand> onAgentSignInMessage(AgentSignInMessage agentSignIn) {

        ActorRef<Agent.AgentCommand> currentAgent = agentRef.get(agentSignIn.agentId); 

        // SignIn Message send
        currentAgent.tell(new Agent.AgentSignInMessage(agentSignIn.agentId));
        agentSignIn.client.tell(new ClientResponse("Agent" + agentSignIn.agentId + " signed in"));
        System.out.println(agentSignIn.agentId);
        return this;
     }

     // Define Signal Handler for Agent SignOut Message
    public Behavior<DeliveryCommand> onAgentSignOutMessage(AgentSignOutMessage agentSignOut) {

        ActorRef<Agent.AgentCommand> currentAgent = agentRef.get(agentSignOut.agentId); 

        // Signout message send
        currentAgent.tell(new Agent.AgentSignOutMessage(agentSignOut.agentId));
        agentSignOut.client.tell(new ClientResponse("Agent" + agentSignOut.agentId + " signed out"));
        System.out.println(agentSignOut.agentId);
        return this;
     }

}
