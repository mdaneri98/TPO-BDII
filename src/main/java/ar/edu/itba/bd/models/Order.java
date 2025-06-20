package ar.edu.itba.bd.models;

import java.time.LocalDate;
import java.util.List;

public record Order(
    String id,
    String supplierId,
    String date,
    Double totalWithoutTax,
    Double tax,
    List<OrderDetail> orderDetails
){} 