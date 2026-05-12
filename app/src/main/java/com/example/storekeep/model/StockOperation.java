package com.example.storekeep.model;

public class StockOperation {
    public long id;
    public long productId;
    public String productName;
    public String type;
    public int amount;
    public long dateMillis;

    public StockOperation(long id, long productId, String productName, String type,
                          int amount, long dateMillis) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.amount = amount;
        this.dateMillis = dateMillis;
    }
}
