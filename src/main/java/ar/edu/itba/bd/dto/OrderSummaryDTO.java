package ar.edu.itba.bd.dto;

public class OrderSummaryDTO {
    private final String orderId;
    private final String date;
    private final String companyName;
    private final double totalWithoutTax;
    private final double totalWithTax;

    public OrderSummaryDTO(String orderId, String date, String companyName, double totalWithoutTax, double totalWithTax) {
        this.orderId = orderId;
        this.date = date;
        this.companyName = companyName;
        this.totalWithoutTax = totalWithoutTax;
        this.totalWithTax = totalWithTax;
    }

    public String getOrderId() { return orderId; }
    public String getDate() { return date; }
    public String getCompanyName() { return companyName; }
    public double getTotalWithoutTax() { return totalWithoutTax; }
    public double getTotalWithTax() { return totalWithTax; }
}
