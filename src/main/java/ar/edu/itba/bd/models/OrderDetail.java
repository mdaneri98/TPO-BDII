package ar.edu.itba.bd.models;

public record OrderDetail(
    String orderId,
    String productId,
    int itemNumber,
    double quantity
){} 