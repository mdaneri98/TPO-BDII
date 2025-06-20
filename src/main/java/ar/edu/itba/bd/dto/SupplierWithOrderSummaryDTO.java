package ar.edu.itba.bd.dto;


public class SupplierWithOrderSummaryDTO {

    private final String supplierName;
    private final String id;
    private final String taxId;
    private final String companyName;
    private final String companyType;
    private final String address;
    private final boolean active;
    private final boolean authorized;
    private final double totalWithoutTax;
    private final double tax;

    private SupplierWithOrderSummaryDTO(Builder builder) {
        this.supplierName = builder.supplierName;
        this.id = builder.id;
        this.taxId = builder.taxId;
        this.companyName = builder.companyName;
        this.companyType = builder.companyType;
        this.address = builder.address;
        this.active = builder.active;
        this.authorized = builder.authorized;
        this.totalWithoutTax = builder.totalWithoutTax;
        this.tax = builder.tax;
    }

    public static class Builder {
        private String supplierName;
        private String id;
        private String taxId;
        private String companyName;
        private String companyType;
        private String address;
        private boolean active;
        private boolean authorized;
        private double totalWithoutTax;
        private double tax;

        public Builder supplierName(String supplierName) {
            this.supplierName = supplierName;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder taxId(String taxId) {
            this.taxId = taxId;
            return this;
        }

        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder companyType(String companyType) {
            this.companyType = companyType;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder authorized(boolean authorized) {
            this.authorized = authorized;
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

        public SupplierWithOrderSummaryDTO build() {
            return new SupplierWithOrderSummaryDTO(this);
        }
    }


    public String getSupplierName() { return supplierName; }
    public String getId() { return id; }
    public String getTaxId() { return taxId; }
    public String getCompanyName() { return companyName; }
    public String getCompanyType() { return companyType; }
    public String getAddress() { return address; }
    public boolean isActive() { return active; }
    public boolean isAuthorized() { return authorized; }
    public double getTotalWithoutTax() { return totalWithoutTax; }
    public double getTax() { return tax; }
}

