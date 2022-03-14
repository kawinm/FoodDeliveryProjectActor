package com.project.restaurant.service;

import java.util.ArrayList;
import java.util.*;
import java.io.File;
import java.util.Scanner;

import com.project.restaurant.model.RestaurantInventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RestaurantService {

    /*
        resturantInventory : List containing items of the form  {ResturantId,ItemId,Quantity}
        for storing the count of items in each resturant.
    */
    private List<RestaurantInventory> restaurantInventory;

    public void initialData() throws Exception {


        restaurantInventory = new ArrayList<RestaurantInventory>();

        String userDirectory = new File("").getAbsolutePath();
        System.out.println(userDirectory);
        File file = new File(userDirectory+ "/initialData.txt");
        Scanner sc = new Scanner(file);
 
        while (sc.hasNextLine()) {

            String str = sc.nextLine();
            System.out.println(str);
            String[] splited = str.split("\\s+");
            if (splited[0].indexOf('*') > -1) break;
            Long restId = Long.parseLong(splited[0]);
            int restNum = Integer.parseInt(splited[1]);


            for (int i = 0; i < restNum; i++) {

                String str2 = sc.nextLine();
                System.out.println(str2);
                String[] splited2 = str2.split("\\s+");
                
                Long itemId, price, qty;

                itemId = Long.parseLong(splited2[0]);
                price  = Long.parseLong(splited2[1]);
                qty    = Long.parseLong(splited2[2]);
                
                RestaurantInventory res = new RestaurantInventory(restId, itemId, qty);
                restaurantInventory.add(res);
                
                
            }

        }
        System.out.println("Resturant Service Initialized");
        sc.close();
            
    }

    public RestaurantService() {


        try {
            initialData();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
  
    public synchronized Boolean acceptOrder(Long restId, Long itemId, Long qty) 
    {
        /* 
            Function taking the ResturantId along with the itemid and quantity
            and tries to process the order by deducting the required quantity 
            of items.
        */

        for (RestaurantInventory item: restaurantInventory) {

            if (item.getRestId().equals(restId) && item.getItemId().equals(itemId)) {
                
                /* Check if sufficient Quantity of items is available */
                if (item.getQty() >= qty) {

                    item.setQty(item.getQty() - qty);
                    System.out.println("/acceptOrder Successful Remaining ItemId = " + 
                    itemId + " ResturantId = " + restId + " Quantity = " + item.getQty()
                    );
                    return true;

                } else {
                    return false;
                }
            } 
        }
        return false;
    }

    
    public synchronized Boolean fillItem(Long restId, Long itemId, Long qty) 
    {
         /* 
            Function for refilling item having itemId at resturant whose id is restId
            by qty.
        */

        for (RestaurantInventory item: restaurantInventory) {

            if (item.getRestId().equals(restId) && item.getItemId().equals(itemId)) {
                /* Increase the amount of items by qty*/
                item.setQty(item.getQty() + qty);
                System.out.println("/fillItem Successful ItemId = " + 
                    itemId + " ResturantId = " + restId + " Quantity = " +  item.getQty()
                );
                return true;
            } 
        }
        return false;
    }

   
    public synchronized Boolean reInitialize() 
    {
        /*
            Reinitialize the catalogue of items available at resturants
        */

        try {
            initialData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;

    }
    
}
