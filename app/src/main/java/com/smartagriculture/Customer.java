package com.smartagriculture;

import java.util.ArrayList;

/**
 * This class is for the Customer
 * - name - String
 * - phno - String
 * - area - String
 * - orders - ArrayList(Order)
 * - address - String
 */

public class Customer {

    private String name;
    private String phno;
    private String area;
    private String email;
    private String dob;
    private ArrayList<Order> orders;
    private Addr address;

    public Customer(){
        orders = new ArrayList<>();
    }

    public Customer(String name,String phno,String area){
        this.name = name;
        this.phno = phno;
        this.area = area;
    }

    public String getDob() {
        return dob;
    }

    public String getEmail() {
        return email;
    }

    public void setPhno(String phno) {
        this.phno = phno;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }

    public void addOrder(Order order){
        this.orders.add(order);
    }

    public String getArea() {
        return area;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public String getName() {
        return name;
    }

    public String getPhno() {
        return phno;
    }

    public Addr getAddress() {
        return address;
    }

    public void setAddress(Addr address) {
        this.address = address;
    }
}
