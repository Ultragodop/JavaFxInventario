package com.minimercado.javafxinventario.modules;

import java.util.Date;

public class PriceHistory {
    private String productId;
    private Date date;
    private double price;
    private double changePercent;
    private String user;
    
    public PriceHistory() {
    }
    
    public PriceHistory(String productId, Date date, double price, double changePercent, String user) {
        this.productId = productId;
        this.date = date;
        this.price = price;
        this.changePercent = changePercent;
        this.user = user;
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
}
