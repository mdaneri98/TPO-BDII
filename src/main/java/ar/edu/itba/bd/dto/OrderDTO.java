package ar.edu.itba.bd.dto;

import java.time.LocalDate;

public class OrderDTO {
    private final String id;
    private final String supplierId;
    private final String date;
    private final double totalWithoutTax;
    private final double tax;

    private OrderDTO(Builder builder) {
        this.id = builder.id;
        this.supplierId = builder.supplierId;
        this.date = builder.date;
        this.totalWithoutTax = builder.totalWithoutTax;
        this.tax = builder.tax;
    }

    public String getId() {
        return id;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getDate() {
        return date;
    }

    public double getTotalWithoutTax() {
        return totalWithoutTax;
    }

    public double getTax() {
        return tax;
    }

    public static class Builder {
        private String id;
        private String supplierId;
        private String date;
        private double totalWithoutTax;
        private double tax;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder supplierId(String supplierId) {
            this.supplierId = supplierId;
            return this;
        }

        public Builder date(String date) {
            this.date = date;
            return this;
        }

        public Builder totalWithoutTax(double totalWithoutTax) {
            this.totalWithoutTax = totalWithoutTax;
            return this;
        }

        public Builder tax(double tax) {
            this.tax = tax;
            return this;
        }

        public OrderDTO build() {
            return new OrderDTO(this);
        }
    }
}
