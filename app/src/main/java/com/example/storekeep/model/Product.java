package com.example.storekeep.model;

public class Product {
    public long id;
    public String name;
    public double price;
    public int quantity;
    public String category;

    public Product() {}

    public Product(long id, String name, double price, int quantity, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category == null ? "" : category;
    }
}
