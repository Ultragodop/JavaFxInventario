package com.minimercado.javafxinventario.modules;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an accounting entry in the general ledger.
 * Contains information about debits and credits to different accounts.
 */
public class AccountingEntry {
    private String id;
    private LocalDate entryDate;
    private String referenceNumber;
    private String description;
    private boolean isPosted;
    private String createdBy;
    private List<LineItem> lineItems;
    private String reversalReason;
    private boolean isReversal;
    private String originalEntryId;
    
    /**
     * Constructor for a new accounting entry.
     * 
     * @param entryDate The date of the entry
     * @param referenceNumber Reference number (e.g., transaction ID)
     * @param description Description of the entry
     */
    public AccountingEntry(LocalDate entryDate, String referenceNumber, String description) {
        this.id = UUID.randomUUID().toString();
        this.entryDate = entryDate;
        this.referenceNumber = referenceNumber;
        this.description = description;
        this.isPosted = false;
        this.lineItems = new ArrayList<>();
        this.isReversal = false;
    }
    
    /**
     * Constructor for a reversal entry.
     * 
     * @param originalEntry The original entry being reversed
     * @param reversalReason Reason for the reversal
     */
    public AccountingEntry(AccountingEntry originalEntry, String reversalReason) {
        this.id = UUID.randomUUID().toString();
        this.entryDate = LocalDate.now();
        this.referenceNumber = "REV-" + originalEntry.getReferenceNumber();
        this.description = "Reversal of " + originalEntry.getDescription() + ": " + reversalReason;
        this.isPosted = false;
        this.lineItems = new ArrayList<>();
        this.isReversal = true;
        this.originalEntryId = originalEntry.getId();
        this.reversalReason = reversalReason;
        
        // Create reversed line items
        for (LineItem originalItem : originalEntry.getLineItems()) {
            // Swap debits and credits in the reversal
            this.addLineItem(
                originalItem.getAccountCode(),
                "Reversal: " + originalItem.getDescription(),
                originalItem.getCreditAmount(), 
                originalItem.getDebitAmount()
            );
        }
    }
    
    /**
     * Adds a line item (debit or credit) to this entry.
     * 
     * @param accountCode Account code
     * @param description Description of the line item
     * @param debitAmount Debit amount
     * @param creditAmount Credit amount
     */
    public void addLineItem(String accountCode, String description, double debitAmount, double creditAmount) {
        lineItems.add(new LineItem(accountCode, description, debitAmount, creditAmount));
    }
    
    /**
     * Validates that the entry balances (sum of debits equals sum of credits).
     * 
     * @return true if the entry balances, false otherwise
     */
    public boolean isBalanced() {
        double totalDebits = 0;
        double totalCredits = 0;
        
        for (LineItem item : lineItems) {
            totalDebits += item.getDebitAmount();
            totalCredits += item.getCreditAmount();
        }
        
        // Compare with a small epsilon to handle floating-point errors
        return Math.abs(totalDebits - totalCredits) < 0.001;
    }
    
    /**
     * Posts the entry to the general ledger.
     * An entry can only be posted if it balances.
     * 
     * @return true if posting was successful, false otherwise
     */
    public boolean post() {
        if (!isBalanced()) {
            return false;
        }
        
        this.isPosted = true;
        return true;
    }
    
    /**
     * Gets the total debit amount for this entry.
     * 
     * @return Total debit amount
     */
    public double getTotalDebitAmount() {
        double total = 0;
        for (LineItem item : lineItems) {
            total += item.getDebitAmount();
        }
        return total;
    }
    
    /**
     * Gets the total credit amount for this entry.
     * 
     * @return Total credit amount
     */
    public double getTotalCreditAmount() {
        double total = 0;
        for (LineItem item : lineItems) {
            total += item.getCreditAmount();
        }
        return total;
    }
    
    // Inner class for line items
    public static class LineItem {
        private String accountCode;
        private String description;
        private double debitAmount;
        private double creditAmount;
        
        public LineItem(String accountCode, String description, double debitAmount, double creditAmount) {
            this.accountCode = accountCode;
            this.description = description;
            this.debitAmount = debitAmount;
            this.creditAmount = creditAmount;
        }
        
        // Getters
        public String getAccountCode() { return accountCode; }
        public String getDescription() { return description; }
        public double getDebitAmount() { return debitAmount; }
        public double getCreditAmount() { return creditAmount; }
    }
    
    // Getters and setters
    public String getId() { return id; }
    public LocalDate getEntryDate() { return entryDate; }
    public String getReferenceNumber() { return referenceNumber; }
    public String getDescription() { return description; }
    public boolean isPosted() { return isPosted; }
    public String getCreatedBy() { return createdBy; }
    public List<LineItem> getLineItems() { return lineItems; }
    public boolean isReversal() { return isReversal; }
    public String getOriginalEntryId() { return originalEntryId; }
    public String getReversalReason() { return reversalReason; }
    
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setReversalReason(String reversalReason) { this.reversalReason = reversalReason; }
    
    @Override
    public String toString() {
        return "AccountingEntry{" +
                "id='" + id + '\'' +
                ", date=" + entryDate +
                ", reference='" + referenceNumber + '\'' +
                ", description='" + description + '\'' +
                ", posted=" + isPosted +
                ", amount=" + getTotalDebitAmount() +
                ", isReversal=" + isReversal +
                '}';
    }
}
