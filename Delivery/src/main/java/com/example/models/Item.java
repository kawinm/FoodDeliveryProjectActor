package com.example.models;

public class Item {

    public Long restId, itemId;

    public Item(Long restId, Long itemId) {
        this.restId = restId;
        this.itemId = itemId;
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
    
}