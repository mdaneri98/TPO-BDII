package ar.edu.itba.bd.dto;

public record OrderDetail(
    String orderId,
    String productId,
    int itemNumber,
    double quantity
){} 