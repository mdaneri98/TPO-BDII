package ar.edu.itba.bd.dto;

import java.time.LocalDate;

public record Order(
    String id,
    String supplierId,
    LocalDate date,
    double totalWithoutTax,
    double tax
){} 