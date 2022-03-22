package com.example;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.ActorRef;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.net.URI;
import java.io.*;

import com.example.models.Item;
import com.example.models.Order;


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
    public static class SampleMessage implements FullFillOrderCommand { 
        String message;
        public SampleMessage(String message) {
            this.message = message;
        }
    }

    // Define Order Delivered
    public static class OrderDeliveredMessage implements FullFillOrderCommand { 

        Long orderId;

        public OrderDeliveredMessage(Long orderId) {
            this.orderId = orderId;
        }
    }
    
    public static class InitiateOrder implements FullFillOrderCommand{

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
       .onMessage(SampleMessage.class, this::onSampleMessage)
       .onMessage(OrderDeliveredMessage.class, this::onOrderDeliveredMessage)
       .onMessage(InitiateOrder.class, this::onInitiateOrder)
       .build();
    }

    // Define Message and Signal Handlers
    public Behavior<FullFillOrderCommand> onSampleMessage(SampleMessage sampleMessage) {

       System.out.println(sampleMessage.message);
       return this;
    }
    
    public Behavior<FullFillOrderCommand> onInitiateOrder(InitiateOrder initiateOrder)
    {
        Long  price = 0l;
        
        Item item = new Item(this.order.restId,this.order.itemId);
        price = this.itemMap.get(item);
        
        String wallet_request_payload = "{ \"custId\" : " + this.order.getCustId() 
                                        + ", \"amount\" : " + price + " }";
        String restuarant_request = "{ \"restId\" : " + this.order.getRestId() 
                                    + ", \"itemId\" : " + this.order.getItemId() 
                                    + ", \"qty\" : " + this.order.getItemId() + "}";
        System.out.println(price);
        /*HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8082/deductBalance"))
                                    .header("Content-Type","application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString(wallet_request_payload))
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
                            
                        
        System.out.println(response.statusCode());
        System.out.println(response.body());*/
        return this;
    }

    // Define Message and Signal Handler for Order Delivered Message
    public Behavior<FullFillOrderCommand> onOrderDeliveredMessage(OrderDeliveredMessage orderDelivered) {

        System.out.println(orderDelivered.orderId);
        return this;
     }

}
