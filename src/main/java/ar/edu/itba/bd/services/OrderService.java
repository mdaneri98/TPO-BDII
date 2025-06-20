package ar.edu.itba.bd.services;

import ar.edu.itba.bd.database.MongoConnection;
import ar.edu.itba.bd.dto.Order;
import ar.edu.itba.bd.dto.OrderDetail;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OrderService {

    private final MongoCollection<Document> collection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public OrderService() {
        MongoDatabase db = MongoConnection.getDatabase("tp2025");
        this.collection = db.getCollection("order");
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        for (Document doc : collection.find()) {
            orders.add(fromDocument(doc));
        }
        return orders;
    }

    public Order findById(String id) {
        Document doc = collection.find(new Document("id", id)).first();
        return doc != null ? fromDocument(doc) : null;
    }

    public void insert(Order order) {
        Document doc = new Document()
                .append("id", order.id())
                .append("supplierId", order.supplierId())
                .append("date", order.date().format(DATE_FORMATTER))
                .append("totalWithoutTax", order.totalWithoutTax())
                .append("tax", order.tax())
                .append("orderDetails", orderDetailsToDocuments(order.orderDetails()));
        collection.insertOne(doc);
    }

    public boolean update(String id, Order order) {
        Document update = new Document("$set", new Document()
                .append("id", order.id())
                .append("supplierId", order.supplierId())
                .append("date", order.date().format(DATE_FORMATTER))
                .append("totalWithoutTax", order.totalWithoutTax())
                .append("tax", order.tax())
                .append("orderDetails", orderDetailsToDocuments(order.orderDetails())));
        return collection.updateOne(new Document("id", id), update).getModifiedCount() > 0;
    }

    public boolean delete(String id) {
        return collection.deleteOne(new Document("id", id)).getDeletedCount() > 0;
    }

    private Order fromDocument(Document doc) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        List<Document> orderDetailDocs = doc.getList("orderDetails", Document.class);
        if (orderDetailDocs != null) {
            for (Document orderDetailDoc : orderDetailDocs) {
                orderDetails.add(new OrderDetail(
                    orderDetailDoc.getString("orderId"),
                    orderDetailDoc.getString("productId"),
                    orderDetailDoc.getInteger("itemNumber"),
                    orderDetailDoc.getDouble("quantity")
                ));
            }
        }
        
        return new Order(
                doc.getString("id"),
                doc.getString("supplierId"),
                LocalDate.parse(doc.getString("date"), DATE_FORMATTER),
                doc.getDouble("totalWithoutTax"),
                doc.getDouble("tax"),
                orderDetails
        );
    }

    private List<Document> orderDetailsToDocuments(List<OrderDetail> orderDetails) {
        List<Document> orderDetailDocs = new ArrayList<>();
        if (orderDetails != null) {
            for (OrderDetail orderDetail : orderDetails) {
                orderDetailDocs.add(new Document()
                    .append("orderId", orderDetail.orderId())
                    .append("productId", orderDetail.productId())
                    .append("itemNumber", orderDetail.itemNumber())
                    .append("quantity", orderDetail.quantity()));
            }
        }
        return orderDetailDocs;
    }
} 