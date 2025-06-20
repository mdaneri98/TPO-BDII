package ar.edu.itba.bd.dto;

import ar.edu.itba.bd.models.Product;

import java.util.List;

public record OrderWithProductDTO(
        String id,
        String supplierId,
        String date,
        Double totalWithoutTax,
        Double tax,
        List<Product> products
) {
}
