package com.example;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
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
    HashMap<Item, Long> itemMap;
    HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap;
    
    ActorRef<Delivery.DeliveryCommand> deliveryRef;

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
    
    public static class InitiateOrder implements FullFillOrderCommand{

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


    //Constructor
<<<<<<< HEAD
    public FullFillOrder(ActorContext<FullFillOrderCommand> context, Long version, Long orderId, Order order, int status, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap, ActorRef<Delivery.DeliveryCommand> deliveryRef) {
=======
    //Constructor
    public FullFillOrder(ActorContext<FullFillOrderCommand> context, Long version, Long orderId, Order order, int status, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap) {
>>>>>>> 25dcc700e8e3b07d76f6d73319dcfadf2f2fb3c3
        super(context);
        this.deliveryVersion = version;
        this.orderId = orderId;
        this.order = order;
        this.status = status;
        this.itemMap = itemMap;
        this.agentMap = agentMap;
        this.waitingNotifyAgents = new ArrayList<Long>();
        this.deliveryRef = deliveryRef;
    }

    // Create method to spawn an actor
<<<<<<< HEAD
    public static Behavior<FullFillOrderCommand> create(Long version,Long orderId, Order order, int status, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap, ActorRef<Delivery.DeliveryCommand> deliveryRef) {   

        return Behaviors.setup(context -> new FullFillOrder(context,version, orderId, order, status, itemMap, agentMap, deliveryRef));
=======
    public static Behavior<FullFillOrderCommand> create(Long version, Long orderId, Order order, int status, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap) {   

        return Behaviors.setup(context -> new FullFillOrder(context, version,orderId, order, status, itemMap, agentMap));
>>>>>>> 25dcc700e8e3b07d76f6d73319dcfadf2f2fb3c3
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
<<<<<<< HEAD
        this.status = Constants.ORDER_UNASSIGNED;

        // Iterating through Hashmap
        for (Map.Entry<Long, ActorRef<Agent.AgentCommand>> entry : agentMap.entrySet()) {

            entry.getValue().tell(new Agent.RequestAgentStatusMessage(getContext().getSelf()));
        }
 

=======
        this.status = Constants.ORDER_UNASSIGNED;  
>>>>>>> 25dcc700e8e3b07d76f6d73319dcfadf2f2fb3c3
        return this;      
    }

    // Define Message and Signal Handler for Order Delivered Message
    public Behavior<FullFillOrderCommand> onOrderDeliveredMessage(OrderDeliveredMessage orderDelivered) {

        System.out.println(orderDelivered.orderId);
        return this;
     }

    // Define Signal Handler for Order status Message
    public Behavior<FullFillOrderCommand> onOrderStatusMessage(OrderStatusMessage orderStatus) {
        
        OrderStatus statusResponse;
        if (status == Constants.ORDER_ASSIGNED) {
            statusResponse = new OrderStatus(orderId, "assigned",-1l);
        }
        else if (status == Constants.ORDER_UNASSIGNED) {
            statusResponse = new OrderStatus(orderId, "unassigned",-1l);
        }
        else if(status == Constants.ORDER_DELIVERED){
            statusResponse = new OrderStatus(orderId, "delivered",-1l);
        }
        else {
            statusResponse = new OrderStatus(orderId,"rejected",-1l);
        }
        orderStatus.client.tell(new ClientStatusResponse(true, statusResponse));
        return this;
     }

     private Behavior<FullFillOrderCommand> onPostStop(StopMessage stop) {
        getContext().getSystem().log().info("Master Control Program stopped");
        return Behaviors.stopped();
      }

    // Define Signal Handler for Request Agent Status Response Message
    public Behavior<FullFillOrderCommand> onRequestAgentStatusResponse(RequestAgentStatusResponse agentResponse) {

        if (agentResponse.agentStatus == Constants.AGENT_AVAILABLE) {
            this.status = Constants.ORDER_ASSIGNED;
            
            agentMap.get(agentResponse.agentId).tell(new Agent.AckMessage(getContext().getSelf()));

            for (Long agentId : waitingNotifyAgents) {

                deliveryRef.tell(new Delivery.ReNotifyAgentMessage(agentId, this.orderId));

            }
        } 

        else if (!waitingNotifyAgents.isEmpty()) {


            agentMap.get(waitingNotifyAgents.get(0)).tell(new Agent.RequestAgentStatusMessage(getContext().getSelf()));

            waitingNotifyAgents.remove(0);

        }

        else {

            // Iterating through Hashmap
            for (Map.Entry<Long, ActorRef<Agent.AgentCommand>> entry : agentMap.entrySet()) {

                if ((long) entry.getKey() <= (long) agentResponse.agentId) {
                    continue;
                }

                entry.getValue().tell(new Agent.RequestAgentStatusMessage(getContext().getSelf()));

                break;
            }


        }
        return this;

    }
}
