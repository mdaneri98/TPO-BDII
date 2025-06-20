package ar.edu.itba.bd.models;

import java.time.LocalDate;
import java.util.List;

public record Order(
    String id,
    String supplierId,
    String date,
    double totalWithoutTax,
    double tax,
    List<OrderDetail> orderDetails
){} 