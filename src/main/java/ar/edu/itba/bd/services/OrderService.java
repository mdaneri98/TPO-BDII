package ar.edu.itba.bd.services;

import ar.edu.itba.bd.database.MongoConnection;
import ar.edu.itba.bd.dto.OrderDTO;
import ar.edu.itba.bd.dto.SupplierWithOrderSummaryDTO;
import ar.edu.itba.bd.models.Order;
import ar.edu.itba.bd.models.OrderDetail;
import ar.edu.itba.bd.models.Product;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OrderService {

    private final MongoCollection<Document> collection;
    private final ProductService productService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public OrderService() {
        MongoDatabase db = MongoConnection.getDatabase("tp2025");
        this.collection = db.getCollection("order");
        this.productService = new ProductService();
    }

    // ----------------------------------- NEEDS ------------------------------------

    //ejercicio 7
    public List<OrderDTO> getOrdersBySupplierTaxId(String taxId) {
        List<Bson> pipeline = List.of(
                Aggregates.lookup("supplier", "supplierId", "id", "suppliers"),
                Aggregates.addFields(new Field<>("suppliers", new Document("$ifNull", List.of("$suppliers", List.of())))),
                Aggregates.project(new Document()
                        .append("id", "$id")
                        .append("supplierId", "$supplierId")
                        .append("date", "$date")
                        .append("totalWithoutTax", "$totalWithoutTax")
                        .append("tax", "$tax")
                        .append("supplierTaxId", new Document("$arrayElemAt", List.of("$suppliers.taxId", 0)))
                ),
                Aggregates.match(Filters.eq("supplierTaxId", taxId))
        );

        List<OrderDTO> orders = new ArrayList<>();
        collection.aggregate(pipeline).forEach(doc -> {
            OrderDTO orderDTO = new OrderDTO.Builder()
                    .id(doc.getString("id"))
                    .supplierId(doc.getString("supplierId"))
                    .date(doc.getString("date"))
                    .totalWithoutTax(doc.getDouble("totalWithoutTax"))
                    .tax(doc.getDouble("tax"))
                    .build();

            orders.add(orderDTO);
        });

        return orders;
    }


    // ------------------------------------ CRUD ------------------------------------

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
        updateProductStock(order.orderDetails(), true);
        
        Document doc = new Document()
                .append("id", order.id())
                .append("supplierId", order.supplierId())
                .append("date", order.date())
                .append("totalWithoutTax", order.totalWithoutTax())
                .append("tax", order.tax())
                .append("orderDetails", orderDetailsToDocuments(order.orderDetails()));
        collection.insertOne(doc);
    }

    public boolean update(String id, Order order) {
        Order oldOrder = findById(id);
        if (oldOrder != null) {
            updateProductStock(oldOrder.orderDetails(), false);
        }

        updateProductStock(order.orderDetails(), true);
        
        Document update = new Document("$set", new Document()
                .append("id", order.id())
                .append("supplierId", order.supplierId())
                .append("date", order.date())
                .append("totalWithoutTax", order.totalWithoutTax())
                .append("tax", order.tax())
                .append("orderDetails", orderDetailsToDocuments(order.orderDetails())));
        return collection.updateOne(new Document("id", id), update).getModifiedCount() > 0;
    }

    public boolean delete(String id) {
        Order order = findById(id);
        if (order != null) {
            updateProductStock(order.orderDetails(), false);
        }
        
        return collection.deleteOne(new Document("id", id)).getDeletedCount() > 0;
    }

    private void updateProductStock(List<OrderDetail> orderDetails, boolean isAdding) {
        if (orderDetails != null) {
            for (OrderDetail detail : orderDetails) {
                Product product = productService.findById(detail.productId());
                if (product != null) {
                    int quantityChange = (int) detail.quantity();
                    if (!isAdding) {
                        quantityChange = -quantityChange;
                    }

                    int newCurrentStock = product.currentStock() + quantityChange;
                    if (newCurrentStock < 0) {
                        newCurrentStock = 0; // No permitimos stock negativo
                    }
                    
                    Product updatedProduct = new Product(
                        product.id(),
                        product.description(),
                        product.brand(),
                        product.category(),
                        product.price(),
                        newCurrentStock,
                        product.futureStock()
                    );
                    
                    productService.update(product.id(), updatedProduct);
                }
            }
        }
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
                doc.getString("date"),
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