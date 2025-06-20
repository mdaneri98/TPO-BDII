package ar.edu.itba.bd.dto;

import ar.edu.itba.bd.models.Phone;

public record SupplierWithPhoneDTO(
        String id,
        String taxId,
        String companyName,
        String companyType,
        String address,
        boolean active,
        boolean authorized,
        String areaCode,
        String phoneNumber,
        String phoneType
) {}
