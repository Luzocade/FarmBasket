package com.smartagriculture;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * This class is for Farmer
 * - Name - String
 * - Phno - String
 * - Location - Location
 * - Chats - ArrayList(Message)
 * - Products - ArrayList(Product)
 * - Profile pic - String
 */

public class Farmer {

    private String name;
    private String phno;
    private String area;
    private String idUrl,certUrl;
    private ArrayList<Message> chats;
    private ArrayList<Product> products;
    private String profilePic;
    private ArrayList<Order> orders;

    Farmer(){
        chats = new ArrayList<>();
        products = new ArrayList<>();
        orders = new ArrayList<>();
    }

    Farmer(String name, String phno, String area){
        chats = new ArrayList<>();
        products = new ArrayList<>();
        orders = new ArrayList<>();
        this.name = name;
        this.phno = phno;
        this.area = area;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setPhno(String phno) {
        this.phno = phno;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getProfilePic(){
        return profilePic;
    }

    public void addChat(Message message){
        this.chats.add(message);
    }

    public void addProduct(Product product){
        this.products.add(product);
    }

    public ArrayList<Message> getChats() {
        return chats;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public String getName() {
        return name;
    }

    public String getPhno() {
        return phno;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void addOrder(Order order){
        orders.add(order);
    }

    public String getIdUrl() {
        return idUrl;
    }

    public void setIdUrl(String idUrl) {
        this.idUrl = idUrl;
    }

    public String getCertUrl() {
        return certUrl;
    }

    public void setCertUrl(String certUrl) {
        this.certUrl = certUrl;
    }
}
