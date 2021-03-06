package com.example;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.Actor;
import akka.actor.typed.ActorRef;

import java.lang.ref.Cleaner.Cleanable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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

import java.util.*;

//import com.example.FullFillOrder;
//import com.example.Agent;


public class Delivery extends AbstractBehavior<Delivery.DeliveryCommand> {
    
    //Define members 
    HashMap<Item, Long> itemMap;
    HashMap<Long, ActorRef<Agent.AgentCommand>> agentRef;
    HashMap<Long, ActorRef<FullFillOrder.FullFillOrderCommand>> orderRef;
    List<Long> agentsList;
    //List<ActorRef<FullFillOrder.FullFillOrderCommand>> pendingOrderRef;
    List<Long> pendingOrderRef;
    
    Long currentOrderId = 1000L;
    Long version=0l;

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

    // Message sent by a FullFillOrder Actor when he gets an agent assigned to him.
    public static class GotAgentAssignedMessage implements DeliveryCommand {
        
        Long orderId;
        Long version;

        public GotAgentAssignedMessage(Long orderId, Long version) {
            this.orderId = orderId;
            this.version = version;
        }

    }

    // Message sent to the delivery when an agent becomes available
    public static class AgentAvailableMessage implements DeliveryCommand {
        Long agentId;
        Long version;

        public AgentAvailableMessage(Long agentId, Long version) {
            this.agentId = agentId;
            this.version = version;
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

    /* Notify and Renotify Agent to Orders */

    public static class ReNotifyAgentMessage implements DeliveryCommand {

        Long agentId, orderId;
        Long version;
        public ReNotifyAgentMessage(Long agentId, Long orderId, Long version) {
            this.agentId = agentId;
            this.orderId = orderId;
            this.version = version;
        }
    }
    
    //Constructor
    public Delivery(ActorContext<DeliveryCommand> context, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentRef,List<Long> agentsList) {
        super(context);
        this.version=0l;
        this.itemMap = itemMap;
        this.agentRef = agentRef;
        this.orderRef = new HashMap<>();
        this.pendingOrderRef = new ArrayList<>();
        this.agentsList = new ArrayList<>();
        this.agentsList = agentsList;
    }

    // Create method to spawn an actor
    public static Behavior<DeliveryCommand> create(HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentRef, List<Long> agentsList) {   

        return Behaviors.setup(context -> new Delivery(context,itemMap, agentRef,agentsList));
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
       .onMessage(GotAgentAssignedMessage.class, this::OnGotAgentAssignedMessage)
       .onMessage(AgentAvailableMessage.class, this::onAgentAvailableMessage)
       .onMessage(ReNotifyAgentMessage.class, this::onRenotifyAgentMesssage)
       .build();
    }

    /* Define Message and Signal Handlers */
  
    // Define Signal Handler for Request Order Message
    public Behavior<DeliveryCommand> onRequestOrderMessage(RequestOrderMessage requestOrder) {

        ActorRef<FullFillOrder.FullFillOrderCommand> orderActor = getContext().spawn(FullFillOrder.create(this.version, currentOrderId, requestOrder.order, Constants.ORDER_UNASSIGNED, itemMap, agentRef, getContext().getSelf()), "order_v_"+ this.version +"_"+currentOrderId);
        requestOrder.client.tell(new RequestOrderResponse(new OrderIdResponse(currentOrderId)));
        // Added to Pending Order References
        this.pendingOrderRef.add(currentOrderId);
        orderRef.put(currentOrderId++, orderActor);
        //System.out.println(requestOrder.order.getCustId());
        orderActor.tell(new FullFillOrder.InitiateOrder());
        return this;
    }

    // Define Signal Handler for Agent SignIn Message
    public Behavior<DeliveryCommand> onAgentSignInMessage(AgentSignInMessage agentSignIn) {

        ActorRef<Agent.AgentCommand> currentAgent = agentRef.get(agentSignIn.agentId); 

        // SignIn Message send
        currentAgent.tell(new Agent.AgentSignInMessage(agentSignIn.agentId,getContext().getSelf()));
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
        ActorRef<FullFillOrder.FullFillOrderCommand> order = this.orderRef.get(orderDelivered.orderId);
        order.tell(new FullFillOrder.OrderDeliveredMessage(orderDelivered.orderId));
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

    // Define Signal Handler for Reinitialize Message
    public Behavior<DeliveryCommand> onReinitializeMessage(ReinitializeMessage reinit) {

        for (Long orderId = 1000L; orderId < currentOrderId; orderId++) {
            orderRef.get(orderId).tell(new FullFillOrder.StopMessage());
            orderRef.remove(orderId);
        }

        this.orderRef.clear();
        for(int i=0; i<agentsList.size(); ++i) {
            ActorRef<Agent.AgentCommand> agent = this.agentRef.get(this.agentsList.get(i));
            agent.tell(new Agent.StopMessage());
        }

        this.agentRef.clear();
        this.pendingOrderRef.clear();
        this.version +=1;
        currentOrderId = 1000L;

        for(int i=0; i<this.agentsList.size(); ++i) { 
            Long agentId = this.agentsList.get(i);
            ActorRef<Agent.AgentCommand> agent = getContext().spawn(Agent.create(agentId,Constants.AGENT_SIGNED_OUT,this.version), "agent_v_"+this.version + "_" +agentId);
            
            this.agentRef.put(agentId, agent);
        }

        reinit.client.tell(new ClientResponse(""));

        return this;
     }

     // Message Handler for Order Success Message
    public Behavior<DeliveryCommand> onOrderSuccessMessage(OrderSuccessMessage orderSuccessMessage) {
        
        if(orderSuccessMessage.version != this.version) {
            return this;
        }

        //ActorRef<FullFillOrder.FullFillOrderCommand> order = orderRef.get(orderSuccessMessage.orderId);
        // this.pendingOrderRef.add(orderSuccessMessage.orderId);
        System.out.println("Order waiting for agents");
        return this;
    }

    public Behavior<DeliveryCommand> OnGotAgentAssignedMessage(GotAgentAssignedMessage gotAgentAssignedMessage){
        
        if(gotAgentAssignedMessage.version != this.version) {
            return this;
        }

        this.pendingOrderRef.remove(gotAgentAssignedMessage.orderId);
        System.out.println("Removed from waiting list");
        return this;
    }

    public Behavior<DeliveryCommand> onAgentAvailableMessage(AgentAvailableMessage agentAvailableMessage) {

        if(agentAvailableMessage.version != this.version) {
            return  this;
        }

        if(!this.pendingOrderRef.isEmpty()) {
            System.out.println("Pending order references there");
            Long orderId = this.pendingOrderRef.get(0);
            ActorRef<FullFillOrder.FullFillOrderCommand> order = this.orderRef.get(orderId);
            order.tell(new FullFillOrder.PingthisAgentMessage(agentAvailableMessage.agentId));
        }
        System.out.println("Reached here");

        return this;
    }

    public Behavior<DeliveryCommand> onRenotifyAgentMesssage(ReNotifyAgentMessage reNotifyAgentMessage) {

        if (reNotifyAgentMessage.version != this.version) {
            return this;
        }

        if (!this.pendingOrderRef.isEmpty()) {

            this.pendingOrderRef.remove(reNotifyAgentMessage.orderId);

            if(! this.pendingOrderRef.isEmpty()) {
                Long orderId = this.pendingOrderRef.get(0);
                ActorRef<FullFillOrder.FullFillOrderCommand> order = this.orderRef.get(orderId);
                order.tell(new FullFillOrder.PingthisAgentMessage(reNotifyAgentMessage.agentId));
            }
        }

        return this;
    }


}
