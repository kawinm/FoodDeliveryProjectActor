package com.project.restaurant.model;

public class RestaurantInventory 
{
    /*
        Class used for deserializing the JSON payload send as a 
        part of Post Request to /refillItem , /acceptOrder
    */  

    public Long restId, itemId, qty, price;

    public RestaurantInventory(Long restId, Long itemId, Long qty) {
        this.restId = restId;
        this.itemId = itemId;
        this.qty = qty;
    }

    /*

    public RestaurantInventory(Long restId, Long itemId, Long qty, Long price) {
        this.restId = restId;
        this.itemId = itemId;
        this.qty = qty;
        this.price = price;
    }

    */
    public Long getRestId() {
        return restId;
    }

    public void setRestId(Long restId) {
        this.restId = restId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getQty() {
        return qty;
    }

    public void setQty(Long qty) {
        this.qty = qty;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

}
