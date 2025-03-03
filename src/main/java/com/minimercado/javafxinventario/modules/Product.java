package com.minimercado.javafxinventario.modules;

public class Product {
    private String id;
    private String name;
    private int stock;
    private int threshold;
    private double price;
    
    public Product(String id, String name, int stock, int threshold, double price) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.threshold = threshold;
        this.price = price;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getStock() { return stock; }

    public void setStock(int stock) { this.stock = stock; }
    public int getThreshold() { return threshold; }
    public void setThreshold(int threshold) { this.threshold = threshold; }
    public double getPrice() { return price; }  
    public void setPrice(double price) { this.price = price; }

}
