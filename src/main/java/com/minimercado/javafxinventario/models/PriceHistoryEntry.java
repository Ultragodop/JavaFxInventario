package com.minimercado.javafxinventario.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.*;

public class PriceHistoryEntry {
    private final IntegerProperty productId;
    private final StringProperty productName;
    private final DoubleProperty previousPrice;
    private final DoubleProperty currentPrice;
    private final ObjectProperty<LocalDateTime> changeDate;
    private final DoubleProperty percentageChange;
    private final StringProperty user;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public PriceHistoryEntry(int productId, String productName, double previousPrice, 
                             double currentPrice, LocalDateTime changeDate, String user) {
        this.productId = new SimpleIntegerProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.previousPrice = new SimpleDoubleProperty(previousPrice);
        this.currentPrice = new SimpleDoubleProperty(currentPrice);
        this.changeDate = new SimpleObjectProperty<>(changeDate);
        
        // Calculate percentage change
        double change = 0.0;
        if (previousPrice > 0) {
            change = ((currentPrice - previousPrice) / previousPrice) * 100.0;
        }
        this.percentageChange = new SimpleDoubleProperty(change);
        
        this.user = new SimpleStringProperty(user);
    }
    
    // Getters and setters for all properties
    
    public int getProductId() {
        return productId.get();
    }
    
    public IntegerProperty productIdProperty() {
        return productId;
    }
    
    public String getProductName() {
        return productName.get();
    }
    
    public StringProperty productNameProperty() {
        return productName;
    }
    
    public double getPreviousPrice() {
        return previousPrice.get();
    }
    
    public DoubleProperty previousPriceProperty() {
        return previousPrice;
    }
    
    public double getCurrentPrice() {
        return currentPrice.get();
    }
    
    public DoubleProperty currentPriceProperty() {
        return currentPrice;
    }
    
    public LocalDateTime getChangeDate() {
        return changeDate.get();
    }
    
    public ObjectProperty<LocalDateTime> changeDateProperty() {
        return changeDate;
    }
    
    public String getFormattedChangeDate() {
        return changeDate.get().format(formatter);
    }
    
    public double getPercentageChange() {
        return percentageChange.get();
    }
    
    public DoubleProperty percentageChangeProperty() {
        return percentageChange;
    }
    
    public String getUser() {
        return user.get();
    }
    
    public StringProperty userProperty() {
        return user;
    }
}
