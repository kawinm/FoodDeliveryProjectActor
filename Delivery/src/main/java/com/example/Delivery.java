package com.example;

import akka.actor.typed.javadsl.ActorContext;

import akka.actor.typed.ActorRef;

import java.lang.ref.Cleaner.Cleanable;
import java.util.HashMap;


import com.example.models.Item;
import com.example.models.Order;
import com.example.models.OrderIdResponse;
import com.example.models.OrderStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    List<ActorRef<FullFillOrder.FullFillOrderCommand>> pendingOrderRef;
    
    Long currentOrderId = 1000L;
    Long version = 0l;

    // Define the message type which 
    // actor can process
    interface DeliveryCommand {}

    // Define messsages here
    

    // Request Order Message
    public static class RequestOrderMessage implements DeliveryCommand { 

        Order order;
        ActorRef<RequestOrderResponse> client;
        public RequestOrderMessage(Order order, ActorRef<RequestOrderResponse> client) {
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

    // Agent Status Message
    public static class AgentStatusMessage implements DeliveryCommand {
        Long agentId;
        ActorRef<Agent.GetAgentStatusResponse> client;
        public AgentStatusMessage(Long agentId, ActorRef<Agent.GetAgentStatusResponse> client)
        {
            this.agentId = agentId;
            this.client = client;
        }
    }

    // Order status Message
    public static class OrderStatusMessage implements DeliveryCommand { 

        Long orderId;
        ActorRef<FullFillOrder.ClientStatusResponse> client;
        public OrderStatusMessage(Long orderId, ActorRef<FullFillOrder.ClientStatusResponse> client) {
            this.orderId = orderId;
            this.client = client;
        }
    }

    // Reinitialize Message
    public static class ReinitializeMessage implements DeliveryCommand { 

        ActorRef<ClientResponse> client;
        public ReinitializeMessage(ActorRef<ClientResponse> client) {
            this.client = client;
        }
    }

    // Order Success Message
    public static class OrderSuccessMessage implements DeliveryCommand {
        Long version;
        Long orderId;

        public OrderSuccessMessage(Long version, Long orderId) {
            this.version = version;
            this.orderId = orderId;
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
    // Response given as a result of call to enpoint /requestOrder
    public static class RequestOrderResponse
    {
        OrderIdResponse response;
        @JsonCreator
        public RequestOrderResponse(@JsonProperty("orderId") OrderIdResponse response)
        {
            this.response = response;
        }
    }
    
    //Constructor
    public Delivery(ActorContext<DeliveryCommand> context, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentRef) {
        super(context);
        this.itemMap = itemMap;
        this.agentRef = agentRef;
        this.orderRef = new HashMap<>();
        this.pendingOrderRef = new ArrayList<>();
    }

    // Create method to spawn an actor
    public static Behavior<DeliveryCommand> create(HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentRef) {   

        return Behaviors.setup(context -> new Delivery(context,itemMap, agentRef));
    }

    //Create Receive Method
    @Override
    public Receive<DeliveryCommand> createReceive() {
       return newReceiveBuilder()
       .onMessage(RequestOrderMessage.class, this::onRequestOrderMessage)
       .onMessage(OrderDeliveredMessage.class, this::onOrderDeliveredMessage)
       .onMessage(AgentSignInMessage.class, this::onAgentSignInMessage)
       .onMessage(AgentSignOutMessage.class, this::onAgentSignOutMessage)
       .onMessage(OrderStatusMessage.class, this::onOrderStatusMessage)
       .onMessage(AgentStatusMessage.class, this::onAgentStatusMessage)
       .onMessage(ReinitializeMessage.class, this::onReinitializeMessage)
       .onMessage(OrderSuccessMessage.class, this::onOrderSuccessMessage)
       .build();
    }

    // Define Message and Signal Handlers
  
    // Define Signal Handler for Request Order Message
    public Behavior<DeliveryCommand> onRequestOrderMessage(RequestOrderMessage requestOrder) {

        ActorRef<FullFillOrder.FullFillOrderCommand> orderActor = getContext().spawn(FullFillOrder.create(this.version, requestOrder.order, Constants.ORDER_UNASSIGNED, itemMap, agentRef), "order_"+currentOrderId);
        requestOrder.client.tell(new RequestOrderResponse(new OrderIdResponse(currentOrderId)));
        orderRef.put(currentOrderId++, orderActor);
        //System.out.println(requestOrder.order.getCustId());
        orderActor.tell(new FullFillOrder.InitiateOrder());
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


      // Define Message and Signal Handler for Order Delivered Message
    public Behavior<DeliveryCommand> onOrderDeliveredMessage(OrderDeliveredMessage orderDelivered) {

        System.out.println(orderDelivered.orderId);
        orderDelivered.client.tell(new ClientResponse("Order Delivered"));
        return this;
     }

     // Define Message Handler for AgentStatus Message
     public Behavior<DeliveryCommand> onAgentStatusMessage(AgentStatusMessage agentStatusMessage)
     {
        ActorRef<Agent.AgentCommand> agent = agentRef.get(agentStatusMessage.agentId);
        agent.tell(new Agent.GetAgentStatusMessage(agentStatusMessage.client));
        return this;
     }

     // Define Signal Handler for Order status Message
    public Behavior<DeliveryCommand> onOrderStatusMessage(OrderStatusMessage orderStatus) {

        Long orderId = orderStatus.orderId;

        if (orderRef.containsKey(orderId)) {
            ActorRef<FullFillOrder.FullFillOrderCommand> order = this.orderRef.get(orderId);
            order.tell(new FullFillOrder.OrderStatusMessage(orderId, orderStatus.client));
            return this;            
        }
        
        orderStatus.client.tell(new FullFillOrder.ClientStatusResponse(false, null));
        return this;
     }

    // Define Signal Handler for Order status Message
    public Behavior<DeliveryCommand> onReinitializeMessage(ReinitializeMessage reinit) {

        for (Long orderId = 1000L; orderId < currentOrderId; orderId++) {
            orderRef.get(orderId).tell(new FullFillOrder.StopMessage());
            orderRef.remove(orderId);
        }
        this.version +=1;
        currentOrderId = 1000L;
        reinit.client.tell(new ClientResponse(""));
        
        return this;
     }

    public Behavior<DeliveryCommand> onOrderSuccessMessage(OrderSuccessMessage orderSuccessMessage) {
        if(orderSuccessMessage.version!= this.version)
        {
            return this;
        }
        ActorRef<FullFillOrder.FullFillOrderCommand> order = orderRef.get(orderSuccessMessage.orderId);
        this.pendingOrderRef.add(order);
        return this;
    }

}
