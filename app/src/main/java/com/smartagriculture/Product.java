package com.smartagriculture;

/**
 * This class is for a product
 * - name - String
 * - ownerId - String
 * - quantity - String
 * - categoryName - String
 * - price - String
 * - ownerNAme - String
 */

public class Product {

    private String name, quantity;
    private String ownerId;
    private String categoryName;
    private String price;
    private String ownerName;

    public Product() {
    }

    public Product(String categoryName, String name, String quantity, String ownerId, String price) {
        this.categoryName = categoryName;
        this.name = name;
        this.quantity = quantity;
        this.ownerId = ownerId;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
