package ar.edu.itba.bd.dto;

import java.time.LocalDate;
import java.util.List;

public record Order(
    String id,
    String supplierId,
    LocalDate date,
    double totalWithoutTax,
    double tax,
    List<OrderDetail> orderDetails
){} 