package com.example;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.example.UserRegistry.User;
import com.example.models.AgentStatus;
import com.example.models.Order;
import com.example.models.OrderStatus;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.dispatch.OnSuccess;
import akka.http.javadsl.marshallers.jackson.Jackson;

import static akka.http.javadsl.server.Directives.*;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes can be defined in separated classes like shown in here
 */
//#user-routes-class
public class DeliveryRoutes {
  //#user-routes-class
  private final static Logger log = LoggerFactory.getLogger(DeliveryRoutes.class);
  private final ActorRef<UserRegistry.Command> userRegistryActor;
  private final ActorRef<Delivery.DeliveryCommand> deliveryActor;
  private final Duration askTimeout;
  private final Scheduler scheduler;

  public DeliveryRoutes(ActorSystem<?> system, ActorRef<UserRegistry.Command> userRegistryActor, ActorRef<Delivery.DeliveryCommand> deliveryActor) {
    this.userRegistryActor = userRegistryActor;
    this.deliveryActor = deliveryActor;
    scheduler = system.scheduler();
    askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");
  }

  private CompletionStage<UserRegistry.GetUserResponse> getUser(String name) {
    return AskPattern.ask(userRegistryActor, ref -> new UserRegistry.GetUser(name, ref), askTimeout, scheduler);
  }

  private CompletionStage<UserRegistry.ActionPerformed> deleteUser(String name) {
    return AskPattern.ask(userRegistryActor, ref -> new UserRegistry.DeleteUser(name, ref), askTimeout, scheduler);
  }

  private CompletionStage<UserRegistry.Users> getUsers() {
    return AskPattern.ask(userRegistryActor, UserRegistry.GetUsers::new, askTimeout, scheduler);
  }

  private CompletionStage<UserRegistry.ActionPerformed> createUser(User user) {
    return AskPattern.ask(userRegistryActor, ref -> new UserRegistry.CreateUser(user, ref), askTimeout, scheduler);
  }

  private CompletionStage<Delivery.ClientResponse> requestOrder(Order order) {
    return AskPattern.ask(deliveryActor, ref -> new Delivery.RequestOrderMessage(order, ref), askTimeout, scheduler);
  }

  private CompletionStage<Delivery.ClientResponse> agentSignIn(Long agentId) {
    return AskPattern.ask(deliveryActor, ref -> new Delivery.AgentSignInMessage(agentId, ref), askTimeout, scheduler);
  }
  
  private CompletionStage<Delivery.ClientResponse> agentSignOut(Long agentId) {
    return AskPattern.ask(deliveryActor, ref -> new Delivery.AgentSignOutMessage(agentId, ref), askTimeout, scheduler);
  }
  
  private CompletionStage<Delivery.ClientResponse> orderDelivered(Long orderId) {
    return AskPattern.ask(deliveryActor, ref -> new Delivery.OrderDeliveredMessage(orderId, ref), askTimeout, scheduler);
  }
  private CompletionStage<Delivery.ClientBooleanResponse> orderStatus(Long orderId)
  {
    return AskPattern.ask(deliveryActor,ref -> new Delivery.OrderStatusMessage(orderId, ref),askTimeout,scheduler);
  }

  /**
   * This method creates one route (of possibly many more that will be part of your Web App)
   */
  //#all-routes
  public Route userRoutes() {
    return 
    concat ( 
      /* TO BE DELETED */
      pathPrefix("users", () ->
        concat(
            //#users-get-delete
            pathEnd(() ->
                concat(
                    get(() ->
                        onSuccess(getUsers(),
                            users -> complete(StatusCodes.OK, users, Jackson.marshaller())
                        )
                    ),
                    post(() ->
                        entity(
                            Jackson.unmarshaller(User.class),
                            user ->
                                onSuccess(createUser(user), performed -> {
                                  log.info("Create result: {}", performed.description);
                                  return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                                })
                        )
                    )
                )
            ),
            //#users-get-delete
            //#users-get-post
            path(PathMatchers.segment(), (String name) ->
                concat(
                    get(() ->
                            //#retrieve-user-info
                            rejectEmptyResponse(() ->
                                onSuccess(getUser(name), performed ->
					  complete(StatusCodes.OK,
						   performed.maybeUser.isPresent() ? performed.maybeUser.get() : "NONE", Jackson.marshaller())
                                )
                            )
                        //#retrieve-user-info
                    ),
                    delete(() ->
                            //#users-delete-logic
                            onSuccess(deleteUser(name), performed -> {
                                  log.info("Delete result: {}", performed.description);
                                  return complete(StatusCodes.OK, performed, Jackson.marshaller());
                                }
                            )
                        //#users-delete-logic
                    )
                )
            )
            //#users-get-post
        )
      ),
      /* Above block to be deleted*/
      path("requestOrder",() -> 
        post(()->  entity(
          Jackson.unmarshaller(Order.class),
          order ->
              onSuccess(requestOrder(order), response -> {
                log.info("Create result: {}", response.response);
                return complete(StatusCodes.CREATED);
              })
            )
        )
      ),
      path("agentSignIn",() ->
        post(() -> entity(
          Jackson.unmarshaller(AgentStatus.class),
          agentstatus ->
              onSuccess(orderDelivered(agentstatus.getAgentId()), response -> {
                log.info("Create result: {}", response.response);
                return complete(StatusCodes.CREATED);
              })
            )
        //onSuccess(agentSignIn(201l), response -> {
        //  System.out.println(response.response);
        //  return complete(StatusCodes.OK);
        //})
        )
      ),
      path("agentSignOut",() -> 
        post(()-> entity(
          Jackson.unmarshaller(AgentStatus.class),
          agentstatus ->
              onSuccess(orderDelivered(agentstatus.getAgentId()), response -> {
                log.info("Create result: {}", response.response);
                return complete(StatusCodes.CREATED);
              })
            )
        
          //onSuccess(agentSignOut(201l), response -> {
          //System.out.println(response.response);
          //return complete(StatusCodes.OK);
          //})
        )
      ),
      path("orderDelivered",()->
        post(()-> entity(
          Jackson.unmarshaller(OrderStatus.class),
          orderstatus ->
              onSuccess(orderDelivered(orderstatus.getOrderId()), response -> {
                log.info("Create result: {}", response.response);
                return complete(StatusCodes.CREATED);
              })
            )//onSuccess(orderDelivered(1000l), response -> {
          //System.out.println(response.response);
          //return complete(StatusCodes.OK);
        //})
        )          
      ),
      path("reInitialize",()->
        post(() -> complete(StatusCodes.ACCEPTED))
      ),
      path(PathMatchers.segment("agent")
        .slash(PathMatchers.integerSegment()), userId -> 
        {
          return complete("Hello user " + userId);
        }
      ),
      path(PathMatchers.segment("order")
        .slash(PathMatchers.longSegment()), orderId -> 
        {
          //return complete("Hello order " + orderId);
          return onSuccess( orderStatus(orderId), response -> {
            if(response.response==true)
            {
              return complete(StatusCodes.NOT_FOUND);
            }
            else
            {
              return complete(StatusCodes.NOT_FOUND);
            }
          });
        }
      )
                                
    );
  }
  //#all-routes

}
