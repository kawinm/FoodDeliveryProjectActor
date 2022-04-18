package com.example;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.impl.ActorRefSinkStage;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.ActorRef;
import akka.actor.typed.PostStop;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.net.URI;
import java.io.*;
import java.util.*;

import com.example.models.Item;
import com.example.models.Order;
import com.example.models.OrderStatus;
import com.example.models.AgentStatus;

//import com.example.Delivery;
//import com.example.Agent;

public class FullFillOrder extends AbstractBehavior<FullFillOrder.FullFillOrderCommand> {
    
    //Define members 
    Long deliveryVersion;
    ActorRef<Delivery.DeliveryCommand> delivery;
    Long orderId;
    Order order;
    int status;
    Long agentId=-1l;
    HashMap<Item, Long> itemMap;
    HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap;

    ActorRef<Delivery.DeliveryCommand> deliveryRef;

    int waitingagentslock = 0;
    List<Long> waitingNotifyAgents;

    // Define the message type which 
    // actor can process
    interface FullFillOrderCommand {}

    // Define messsages here
    // Define Order Delivered
    public static class OrderDeliveredMessage implements FullFillOrderCommand { 

        Long orderId;

        public OrderDeliveredMessage(Long orderId) {
            this.orderId = orderId;
        }
    }

    // Define Order Status Message
    public static class OrderStatusMessage implements FullFillOrderCommand { 

        Long orderId;
        ActorRef<ClientStatusResponse> client;
        public OrderStatusMessage(Long orderId, ActorRef<ClientStatusResponse> client) {
            this.orderId = orderId;
            this.client = client;
        }
    }

    // Define Order Status Message
    public static class StopMessage implements FullFillOrderCommand { 


        public StopMessage() {
        }
    }
    
    public static class InitiateOrder implements FullFillOrderCommand {

    }

    public static class ClientStatusResponse {

        boolean response;
        OrderStatus orderStatusResponse;
        
        public ClientStatusResponse(boolean response, OrderStatus orderStatusResponse) {
            this.response = response;
            this.orderStatusResponse = orderStatusResponse;
        }
    }
    
    /* Messages for Assigning Agent */

    // Agent Status Response Message (From Agent)
    public static class RequestAgentStatusResponse implements FullFillOrderCommand { 

        Long agentId;
        int agentStatus;
        public RequestAgentStatusResponse(Long agentId, int agentStatus) {
            this.agentId = agentId;
            this.agentStatus = agentStatus;
        }
    }

    public static class PingthisAgentMessage implements FullFillOrderCommand {
        Long agentId;
        public PingthisAgentMessage(Long agentId) { 
            this.agentId = agentId;
        }
    }

    //Constructor
    public FullFillOrder(ActorContext<FullFillOrderCommand> context, Long version, Long orderId, Order order, int status, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap, ActorRef<Delivery.DeliveryCommand> deliveryRef) {
        super(context);
        this.deliveryVersion = version;
        this.orderId = orderId;
        this.order = order;
        this.status = status;
        this.itemMap = itemMap;
        this.agentMap = agentMap;
        this.waitingNotifyAgents = new ArrayList<Long>();
        this.deliveryRef = deliveryRef;
        this.agentId=-1l;
        this.waitingagentslock = 0;
    }

    // Create method to spawn an actor
    public static Behavior<FullFillOrderCommand> create(Long version,Long orderId, Order order, int status, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap, ActorRef<Delivery.DeliveryCommand> deliveryRef) {   

        return Behaviors.setup(context -> new FullFillOrder(context,version, orderId, order, status, itemMap, agentMap, deliveryRef));
    }

    //Create Receive Method
    @Override
    public Receive<FullFillOrderCommand> createReceive() {
       return newReceiveBuilder()
       .onMessage(OrderDeliveredMessage.class, this::onOrderDeliveredMessage)
       .onMessage(InitiateOrder.class, this::onInitiateOrder)
       .onMessage(OrderStatusMessage.class, this::onOrderStatusMessage)
       .onMessage(StopMessage.class, this::onPostStop)
       .onMessage(RequestAgentStatusResponse.class, this::onRequestAgentStatusResponse)
       .onMessage(PingthisAgentMessage.class, this::onPingThisAgentMessage)
       .build();
    }

    // Define Message and Signal Handlers
   
    public Behavior<FullFillOrderCommand> onInitiateOrder(InitiateOrder initiateOrder)
    {
        Long  price = 0l;
        this.status = Constants.ORDER_UNASSIGNED;
        Item item = new Item(this.order.restId,this.order.itemId);
        price = this.itemMap.get(item);
        
        Long amount = price * this.order.getQty();
        String wallet_request = "{ \"custId\" : " + this.order.getCustId() 
                                        + ", \"amount\" : " + amount + " }";
        String restuarant_request = "{ \"restId\" : " + this.order.getRestId() 
                                    + ", \"itemId\" : " + this.order.getItemId() 
                                    + ", \"qty\" : " + this.order.getQty() + "}";
        System.out.println(amount);
        HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8082/deductBalance"))
                                    .header("Content-Type","application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString(wallet_request))
                                    .build();
                        
                                //HttpClient client = HttpClient.newHttpClient();
        HttpResponse response = null;
        try
        {
            HttpClient client = HttpClient.newHttpClient();
            response = client.send(request,HttpResponse.BodyHandlers.ofString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        if(response.statusCode()==410)
        {
            System.out.println("Insufficient balance");
            this.status = Constants.ORDER_REJECTED;
            return this;
        } 
        request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/acceptOrder"))
                .header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(restuarant_request))
                .build();
        response = null;
        try
        {
            HttpClient client = HttpClient.newHttpClient();
            response = client.send(request,HttpResponse.BodyHandlers.ofString());
        }
        catch(Exception e)
        {
            
                    e.printStackTrace();
        }

        if (response.statusCode() == 410 )
        {
            System.out.println("Insufficient stock");
            request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8082/addBalance"))
                                    .header("Content-Type","application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString(wallet_request))
                                    .build();
                        
                                //HttpClient client = HttpClient.newHttpClient();
            response = null;
            try
            {
                HttpClient client = HttpClient.newHttpClient();
                response = client.send(request,HttpResponse.BodyHandlers.ofString());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            this.status=Constants.ORDER_REJECTED;
            return this;
        }        
        System.out.println("Order request accepted");
        this.status = Constants.ORDER_UNASSIGNED;
        this.deliveryRef.tell(new Delivery.OrderSuccessMessage(this.deliveryVersion, this.orderId));

        // Iterating through Hashmap
        for (Map.Entry<Long, ActorRef<Agent.AgentCommand>> entry : agentMap.entrySet()) {

            entry.getValue().tell(new Agent.RequestAgentStatusMessage(getContext().getSelf()));
        }
 

        return this;      
    }

    // Define Message and Signal Handler for Order Delivered Message
    public Behavior<FullFillOrderCommand> onOrderDeliveredMessage(OrderDeliveredMessage orderDelivered) {

        if(this.status != Constants.ORDER_ASSIGNED) {
            return this;
        }

        this.status = Constants.ORDER_DELIVERED;
        ActorRef<Agent.AgentCommand> agent = this.agentMap.get(this.agentId);
        agent.tell(new Agent.FreeAgentMessage());

        //System.out.println(orderDelivered.orderId);
        return this;
     }

    // Define Signal Handler for Order status Message
    public Behavior<FullFillOrderCommand> onOrderStatusMessage(OrderStatusMessage orderStatus) {
        
        OrderStatus statusResponse;
        if (status == Constants.ORDER_ASSIGNED) {
            statusResponse = new OrderStatus(orderId, "assigned",this.agentId);
        }
        else if (status == Constants.ORDER_UNASSIGNED) {
            statusResponse = new OrderStatus(orderId, "unassigned",this.agentId);
        }
        else if(status == Constants.ORDER_DELIVERED) { 
            statusResponse = new OrderStatus(orderId, "delivered",this.agentId);
        }
        else {
            statusResponse = new OrderStatus(orderId,"rejected",this.agentId);
        }

        orderStatus.client.tell(new ClientStatusResponse(true, statusResponse));
        return this;
     }

     // Message handler to Stop the actor
     private Behavior<FullFillOrderCommand> onPostStop(StopMessage stop) {
        getContext().getSystem().log().info("Master Control Program stopped");
        return Behaviors.stopped();
      }

    // Define Signal Handler for Request Agent Status Response Message
    public Behavior<FullFillOrderCommand> onRequestAgentStatusResponse(RequestAgentStatusResponse agentResponse) {

        if (agentResponse.agentStatus == Constants.AGENT_AVAILABLE) {
            
            if(this.status != Constants.ORDER_UNASSIGNED) {
                agentMap.get(agentResponse.agentId).tell(new Agent.AckMessage(false, getContext().getSelf()));
                return this;
            }

            this.status = Constants.ORDER_ASSIGNED;
            agentMap.get(agentResponse.agentId).tell(new Agent.AckMessage(true,getContext().getSelf()));
            this.agentId = agentResponse.agentId;
            this.deliveryRef.tell( new Delivery.GotAgentAssignedMessage(this.orderId,this.deliveryVersion));

            for (Long agentId : this.waitingNotifyAgents) {

                deliveryRef.tell(new Delivery.ReNotifyAgentMessage(agentId, this.orderId,this.deliveryVersion));

            }

            this.waitingNotifyAgents.clear();
            this.waitingagentslock = 0;
        } 
        else if (this.status!= Constants.ORDER_UNASSIGNED) {
            this.agentMap.get(agentResponse.agentId).tell(new Agent.AckMessage(false, getContext().getSelf()));
            this.waitingagentslock = 0;
            return this;
        }
        else if (!waitingNotifyAgents.isEmpty()) {
            this.agentMap.get(waitingNotifyAgents.get(0)).tell(new Agent.RequestAgentStatusMessage(getContext().getSelf()));
            this.waitingNotifyAgents.remove(0);
        }

        else {

            // Iterating through Hashmap
            this.waitingagentslock = 0;
            for (Map.Entry<Long, ActorRef<Agent.AgentCommand>> entry : this.agentMap.entrySet()) {

                if ((long) entry.getKey() <= (long) agentResponse.agentId) {
                    continue;
                }

                entry.getValue().tell(new Agent.RequestAgentStatusMessage(getContext().getSelf()));

                break;
            }


        }

        return this;

    }

    public Behavior<FullFillOrderCommand> onPingThisAgentMessage(PingthisAgentMessage pingthisAgentMessage) {
        if(this.status != Constants.ORDER_UNASSIGNED) {
            this.deliveryRef.tell(new Delivery.ReNotifyAgentMessage(pingthisAgentMessage.agentId, this.orderId,this.deliveryVersion));
        }
        if(this.waitingagentslock==0) {
            ActorRef<Agent.AgentCommand> agent = this.agentMap.get(pingthisAgentMessage.agentId);
            agent.tell(new Agent.RequestAgentStatusMessage(getContext().getSelf()));
            this.waitingagentslock = 1;
        }
        else
        {
            this.waitingNotifyAgents.add(pingthisAgentMessage.agentId);
        }
      
        return this;
    }
}
