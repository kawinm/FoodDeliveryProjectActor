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

    //Compare only account numbers
    @Override
    public int hashCode(){
        String hash_string = ""+this.itemId+this.restId+"";
        return hash_string.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Item item  = (Item) obj;
        //System.out.print("hhh"+this.restId+"lll"+item.getRestId());
        if (this.restId.equals(item.getRestId()) && this.itemId.equals(item.getItemId()))
            return true;
        return false;
    }
    
}