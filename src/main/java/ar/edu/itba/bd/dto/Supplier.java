package ar.edu.itba.bd.dto;

import java.util.List;


public record Supplier(
     String id,
     String taxId,
     String companyName,
     String companyType,
     String address,
     boolean active,
     boolean authorized,
     List<Phone> phones
){}

