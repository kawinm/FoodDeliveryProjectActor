package com.project.restaurant.controller;

import com.project.restaurant.model.RestaurantInventory;
import com.project.restaurant.service.RestaurantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
class RestaurantController {
    
    @Autowired
    RestaurantService restaurantService;

    
    // Accepts Order invoked by the Delivery service if the required quantity of delivery item is present.
    @PostMapping(path="/acceptOrder",consumes="application/json")
    public ResponseEntity<String> acceptOrder(@RequestBody RestaurantInventory request)
    {
        if (restaurantService.acceptOrder(request.restId, request.itemId, request.qty)) 
            return new ResponseEntity<String>(HttpStatus.CREATED);
        else  
            return new ResponseEntity<String>(HttpStatus.GONE);
    }


    // Refills the item x at the inventory by quantity y.
    @PostMapping(path="/refillItem",consumes="application/json")
    public ResponseEntity<String> refillItem(@RequestBody RestaurantInventory request)
    {
        restaurantService.fillItem(request.restId, request.itemId, request.qty);
        return new ResponseEntity<String>(HttpStatus.CREATED);
       
    }

    // Reinitializes the inventory
    @PostMapping(path="/reInitialize")
    public ResponseEntity<String> reInitialize()
    {
        restaurantService.reInitialize();
        return new ResponseEntity<String>(HttpStatus.CREATED);
   }

}