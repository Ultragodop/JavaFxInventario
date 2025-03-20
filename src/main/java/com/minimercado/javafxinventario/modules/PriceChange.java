package com.minimercado.javafxinventario.modules;

import java.util.Date;

/**
 * Represents a price change for a product
 */
public class PriceChange {
    private int id;
    private String productId;
    private Date date;
    private double price;
    private double changePercent;
    private String user;

    /**
     * Default constructor
     */
    public PriceChange() {
    }

    /**
     * Construct a price change event
     * @param productId The product ID/barcode
     * @param date Date of the change
     * @param price New price
     * @param changePercent Percentage change from previous price
     * @param user User who made the change
     */
    public PriceChange(String productId, Date date, double price, double changePercent, String user) {
        this.productId = productId;
        this.date = date;
        this.price = price;
        this.changePercent = changePercent;
        this.user = user;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return String.format("PriceChange{productId=%s, date=%s, price=%.2f, changePercent=%.2f%%}", 
            productId, date, price, changePercent);
    }
}
