package ar.edu.itba.bd.models;

public record Product(
    String id,
    String description,
    String brand,
    String category,
    double price,
    int currentStock,
    int futureStock
){} 