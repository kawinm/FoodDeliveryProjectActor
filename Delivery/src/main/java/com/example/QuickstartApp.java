package com.example;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.ActorSystem;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

import com.example.models.Item;

//#main-class
public class QuickstartApp {
    // #start-http-server
    static void startHttpServer(Route route, ActorSystem<?> system) {
        CompletionStage<ServerBinding> futureBinding =
            Http.get(system).newServerAt("localhost", 8081).bind(route);

        futureBinding.whenComplete((binding, exception) -> {
            if (binding != null) {
                InetSocketAddress address = binding.localAddress();
                system.log().info("Server online at http://{}:{}/",
                    address.getHostString(),
                    address.getPort());
            } else {
                system.log().error("Failed to bind HTTP endpoint, terminating system", exception);
                system.terminate();
            }
        });
    }
    // #start-http-server

    public static void main(String[] args) throws Exception {

        //#server-bootstrapping
        Behavior<NotUsed> rootBehavior = Behaviors.setup(context -> {

            File file = new File(args[0]);
            Scanner sc = new Scanner(file);
            HashMap<Long,ActorRef<Agent.AgentCommand>> agentRefs = new HashMap<>();
            HashMap<Item, Long> itemMap = new HashMap<>();
            

            int count = 0;
            while (sc.hasNextLine()) {

                String str = sc.nextLine();
                System.out.println(str);
                String[] splited = str.split("\\s+");
        
                if (splited[0].indexOf('*') > -1) {
                    count += 1;
                    continue;
                }
                if(count==0)
                {
                    Long restId = Long.parseLong(splited[0]);
                    int restNum = Integer.parseInt(splited[1]);
                    for (int i = 0; i < restNum; i++) {
    
                        String str2 = sc.nextLine();
                        String[] splited2 = str2.split("\\s+");
                        
                        Long itemId, price, qty;
    
                        itemId = Long.parseLong(splited2[0]);
                        price  = Long.parseLong(splited2[1]);
                        qty    = Long.parseLong(splited2[2]);
                        
                        Item item = new Item(restId, itemId);
                        itemMap.put(item, price);  
                    }
                }
                if (count == 1) 
                {
                    Long agentId = Long.parseLong(str);
                    ActorRef<Agent.AgentCommand> agentActor = context.spawn(Agent.create(agentId, Constants.AGENT_SIGNED_OUT), "agent_"+agentId);
                    agentRefs.put(agentId,agentActor);
                }
                    
            }
            sc.close();

            ActorRef<Delivery.DeliveryCommand> deliveryActor = context.spawn(Delivery.create(itemMap, agentRefs), "delivery_main");

            // Sample message send
            agentRefs.get(201l).tell(new Agent.SampleMessage("Hello from Agent 201"));

            // To Delete $#%%
            ActorRef<UserRegistry.Command> userRegistryActor =
                context.spawn(UserRegistry.create(), "UserRegistry");

            
            DeliveryRoutes userRoutes = new DeliveryRoutes(context.getSystem(), userRegistryActor,deliveryActor);
            startHttpServer(userRoutes.userRoutes(), context.getSystem());

            return Behaviors.empty();
        });

        // boot up server using the route as defined below
        ActorSystem.create(rootBehavior, "HelloAkkaHttpServer");
        //#server-bootstrapping
    }

}
//#main-class


