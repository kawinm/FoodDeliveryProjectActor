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
import java.net.URI;
import java.io.*;

import com.example.models.Item;
import com.example.models.Order;
import com.example.models.OrderStatus;


public class FullFillOrder extends AbstractBehavior<FullFillOrder.FullFillOrderCommand> {
    
    //Define members 
    Long orderId;
    Order order;
    int status;
    HashMap<Item, Long> itemMap;
    HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap;

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
    
    //Constructor
    public FullFillOrder(ActorContext<FullFillOrderCommand> context, Long orderId, Order order, int status, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap) {
        super(context);
        this.orderId = orderId;
        this.order = order;
        this.status = status;
        this.itemMap = itemMap;
        this.agentMap = agentMap;
    }

    // Create method to spawn an actor
    public static Behavior<FullFillOrderCommand> create(Long orderId, Order order, int status, HashMap<Item, Long> itemMap, HashMap<Long, ActorRef<Agent.AgentCommand>> agentMap) {   

        return Behaviors.setup(context -> new FullFillOrder(context, orderId, order, status, itemMap, agentMap));
    }

    //Create Receive Method
    @Override
    public Receive<FullFillOrderCommand> createReceive() {
       return newReceiveBuilder()
       .onMessage(OrderDeliveredMessage.class, this::onOrderDeliveredMessage)
       .onMessage(InitiateOrder.class, this::onInitiateOrder)
       .onMessage(OrderStatusMessage.class, this::onOrderStatusMessage)
       .onMessage(StopMessage.class, this::onPostStop)
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
            return this;
        }        
        System.out.println("Order request accepted");
        this.status = Constants.ORDER_DELIVERED;  
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
        else {
            statusResponse = new OrderStatus(orderId, "delivered",-1l);
        }
        orderStatus.client.tell(new ClientStatusResponse(true, statusResponse));
        return this;
     }

     private Behavior<FullFillOrderCommand> onPostStop(StopMessage stop) {
        getContext().getSystem().log().info("Master Control Program stopped");
        return Behaviors.stopped();
      }

}
