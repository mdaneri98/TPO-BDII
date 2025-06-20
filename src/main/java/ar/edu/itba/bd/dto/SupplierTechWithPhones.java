package ar.edu.itba.bd.dto;

import ar.edu.itba.bd.models.Phone;

import java.util.List;

public record SupplierTechWithPhones(
        String id,
        List<Phone> phones
) {
}
