package com.example;

import akka.actor.typed.javadsl.ActorContext;

import java.util.ArrayList;

import com.example.models.AgentStatus;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;
import ch.qos.logback.core.joran.conditional.ElseAction;
import akka.actor.typed.javadsl.Behaviors;

//import com.example.Delivery;
//import com.example.FullFillOrder;

import java.util.*;

public class Agent extends AbstractBehavior<Agent.AgentCommand> {
    
    // Define members 
    Long agentId;
    int status;

    Long deliveryVersion = 0l;
    ActorRef<Delivery.DeliveryCommand> deliveryActor;

    int SignOutLock;

    int assignmentLock;

    List<ActorRef<FullFillOrder.FullFillOrderCommand>> waitingOrders;

    // Define the message type which 
    // actor can process
    interface AgentCommand {}

    // Agent Signin Message
    public static class AgentSignInMessage implements AgentCommand { 

        Long agentId;
        ActorRef<Delivery.DeliveryCommand> delivery;
        public AgentSignInMessage(Long agentId, ActorRef<Delivery.DeliveryCommand> dRef) {
            this.agentId = agentId;
            this.delivery = dRef;
        }
    }

    // Agent Signout Message
    public static class AgentSignOutMessage implements AgentCommand { 

        Long agentId;

        public AgentSignOutMessage(Long agentId) {
            this.agentId = agentId;
        }
    }
    
    
    // Get Agent Status Message
    public static class GetAgentStatusMessage implements AgentCommand { 
        
        ActorRef<GetAgentStatusResponse> agentStatusResponse;
        public GetAgentStatusMessage(ActorRef<GetAgentStatusResponse> agentStatusResponse) {
            this.agentStatusResponse = agentStatusResponse;
        }
    }

    /* Messages for Assigning Agent to an Order */

    // Request Agent Status Message (From FFO)
    public static class RequestAgentStatusMessage implements AgentCommand { 
        
        ActorRef<FullFillOrder.FullFillOrderCommand> order;

        public RequestAgentStatusMessage(ActorRef<FullFillOrder.FullFillOrderCommand> order) {
            this.order = order;
        }
    }

    // Acknowledge Agent Assignment Message (From FFO)
    public static class AckMessage implements AgentCommand { 
        
        boolean ack;
        ActorRef<FullFillOrder.FullFillOrderCommand> order;

        public AckMessage(boolean ack,ActorRef<FullFillOrder.FullFillOrderCommand> order) {
            this.ack = ack;
            this.order = order;
        }
    }

    /* */

    public static class GetAgentStatusResponse {
        AgentStatus agentStatus;
        
        public GetAgentStatusResponse(AgentStatus agentStatus) {
            this.agentStatus = agentStatus;
        }
    }
  
    public static class FreeAgentMessage implements AgentCommand{
        
    }

    public static class StopMessage implements AgentCommand {

        public StopMessage() {

        }
    }

    //Constructor
    public Agent(ActorContext<AgentCommand> context, Long agentId, int status,Long version) {
        super(context);
        this.agentId = agentId;
        this.status = status;
        this.deliveryVersion = version;
        this.SignOutLock = 0;
        this.waitingOrders = new ArrayList<ActorRef<FullFillOrder.FullFillOrderCommand>>();
        this.deliveryActor = null;
        System.out.println(this.deliveryVersion);
    }

    // Create method to spawn an actor
    public static Behavior<AgentCommand> create(Long agentId, int status, Long version) {   

        return Behaviors.setup(context -> new Agent(context,agentId,status,version));
    }

    //Create Receive Method
    @Override
    public Receive<AgentCommand> createReceive() {
       return newReceiveBuilder()
       .onMessage(AgentSignInMessage.class, this::onAgentSignInMessage)
       .onMessage(AgentSignOutMessage.class, this::onAgentSignOutMessage)
       .onMessage(GetAgentStatusMessage.class, this::onGetAgentStatusMessage)
       .onMessage(RequestAgentStatusMessage.class, this::onRequestAgentStatusMessage)
       .onMessage(AckMessage.class, this::onAckMessage)
       .onMessage(FreeAgentMessage.class,this::onFreeAgentMessage)
       .onMessage(StopMessage.class, this::onPostStop)
       .build();
    }

    /* Define Message and Signal Handlers */
  
    // Define Signal Handler for Agent SignIn Message
    public Behavior<AgentCommand> onAgentSignInMessage(AgentSignInMessage agentSignIn) {

        if (this.status == Constants.AGENT_SIGNED_OUT) {
            this.deliveryActor = agentSignIn.delivery;
            System.out.println(this.deliveryActor);
            this.status = Constants.AGENT_AVAILABLE;
            this.waitingOrders.clear();
            this.deliveryActor.tell(new Delivery.AgentAvailableMessage(agentSignIn.agentId,this.deliveryVersion));
        }

        return this;
    }

     // Define Signal Handler for Agent SignOut Message
    public Behavior<AgentCommand> onAgentSignOutMessage(AgentSignOutMessage agentSignOut) {

        if (this.assignmentLock == 1) {
            this.SignOutLock = 1;
            return this;
        }

        if (this.status == Constants.AGENT_AVAILABLE) {
            this.status = Constants.AGENT_SIGNED_OUT;
        }

        System.out.println(agentSignOut.agentId);
        return this;
    }

     // Define Signal Handler for Agent Status Message
    public Behavior<AgentCommand> onGetAgentStatusMessage(GetAgentStatusMessage getAgentStatus) {
        AgentStatus agentStatus = new AgentStatus(this.agentId);
        
        if(this.status == Constants.AGENT_SIGNED_OUT) {
            agentStatus.setStatus("signed-out");
        }
        else if(this.status == Constants.AGENT_AVAILABLE) {
            agentStatus.setStatus("available");
        }
        else {
            agentStatus.setStatus("unavailable");
        }

        getAgentStatus.agentStatusResponse.tell(new GetAgentStatusResponse(agentStatus));
        return this;
     }

    public Behavior<AgentCommand> onRequestAgentStatusMessage(RequestAgentStatusMessage requestAgentStatus) {

        if (this.assignmentLock == 1) {

            this.waitingOrders.add(requestAgentStatus.order);
        } 
        else if (this.status == Constants.AGENT_SIGNED_OUT || this.status == Constants.AGENT_UNAVAILABLE) {

            requestAgentStatus.order.tell(new FullFillOrder.RequestAgentStatusResponse(this.agentId, this.status));
        }
        else {
            this.assignmentLock = 1;
            requestAgentStatus.order.tell(new FullFillOrder.RequestAgentStatusResponse(this.agentId, this.status));
        }

        return this;
    }

    public Behavior<AgentCommand> onAckMessage(AckMessage ackOrder) {
        
        if (ackOrder.ack == true) {
            this.status = Constants.AGENT_UNAVAILABLE;
            this.assignmentLock = 0;
            this.SignOutLock = 0;

            for (ActorRef<FullFillOrder.FullFillOrderCommand> orders: this.waitingOrders) {
                orders.tell(new FullFillOrder.RequestAgentStatusResponse(this.agentId, this.status));
    
            }
            this.waitingOrders.clear();
        }
        else {
            if (!this.waitingOrders.isEmpty())  {
                ActorRef<FullFillOrder.FullFillOrderCommand> order = this.waitingOrders.get(0);
                this.waitingOrders.remove(0);
                order.tell(new FullFillOrder.RequestAgentStatusResponse(this.agentId, this.status));
            }
            else {
                if(this.SignOutLock == 1) {
                    this.status= Constants.AGENT_SIGNED_OUT;
                    this.SignOutLock = 0;
                }
                this.assignmentLock=0;
            }
        }
       
        return this;
    }

    public Behavior<AgentCommand> onFreeAgentMessage(FreeAgentMessage freeAgentMessage) {
        this.status = Constants.AGENT_AVAILABLE;
        this.deliveryActor.tell(new Delivery.AgentAvailableMessage(this.agentId,this.deliveryVersion));
        return this;
    }

    public Behavior<AgentCommand> onPostStop(StopMessage stopMessage) {
        getContext().getSystem().log().info("Master Control Program stopped");
        return Behaviors.stopped();
    }
}
