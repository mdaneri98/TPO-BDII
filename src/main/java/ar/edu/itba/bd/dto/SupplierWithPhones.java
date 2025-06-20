package ar.edu.itba.bd.dto;

import ar.edu.itba.bd.models.Phone;

import java.util.List;

public class SupplierWithPhones {
    private final String id;
    private final List<Phone> phones;

    private SupplierWithPhones(Builder builder) {
        this.id = builder.id;
        this.phones = builder.phones;
    }

    public String getId() {
        return id;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public static class Builder {
        private String id;
        private List<Phone> phones;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder phones(List<Phone> phones) {
            this.phones = phones;
            return this;
        }

        public SupplierWithPhones build() {
            return new SupplierWithPhones(this);
        }

    }
}
