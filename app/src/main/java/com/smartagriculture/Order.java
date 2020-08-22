package com.smartagriculture;

import java.util.Calendar;

/**
 * This class is for the Orders
 * - product - Product
 * - quantity - String
 * - customer - Customer
 * - time - long
 * - urgent - boolean
 * - delivered - boolean
 */

public class Order {

    private Product product;
    private String quantity;
    private Customer customer;
    private long time;
    private boolean urgent;
    private boolean delivered;

    public Order(){

    }

    public Order(Product product,String quantity,boolean urgent){
        this.product = product;
        this.quantity = quantity;
        this.urgent = urgent;
        this.time = Calendar.getInstance().getTimeInMillis();
    }

    public Customer getCustomer() {
        return customer;
    }

    public Product getProduct() {
        return product;
    }

    public String getQuantity() {
        return quantity;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }
}
