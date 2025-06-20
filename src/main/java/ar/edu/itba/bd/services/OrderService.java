package ar.edu.itba.bd.services;

import ar.edu.itba.bd.database.MongoConnection;
import ar.edu.itba.bd.dto.OrderDTO;
import ar.edu.itba.bd.dto.OrderWithProductDTO;
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

import java.util.*;

public class OrderService {

    private final MongoCollection<Document> orderCollection;
    private final ProductService productService;
    private final MongoCollection<Document> productCollection;


    public OrderService() {
        MongoDatabase db = MongoConnection.getDatabase("tp2025");
        this.orderCollection = db.getCollection("order");
        this.productCollection = db.getCollection("product");
        this.productService = new ProductService();
    }

    // ----------------------------------- NEEDS ------------------------------------

    //ejercicio 7
    public List<OrderDTO> findOrdersBySupplierTaxId(String taxId) {
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
        orderCollection.aggregate(pipeline).forEach(doc -> {
            // Manejar valores numéricos que pueden ser Integer o Double
            Number totalWithoutTaxNum = doc.get("totalWithoutTax", Number.class);
            Number taxNum = doc.get("tax", Number.class);
            
            double totalWithoutTax = totalWithoutTaxNum != null ? totalWithoutTaxNum.doubleValue() : 0.0;
            double tax = taxNum != null ? taxNum.doubleValue() : 0.0;
            
            OrderDTO orderDTO = new OrderDTO.Builder()
                    .id(doc.getString("id"))
                    .supplierId(doc.getString("supplierId"))
                    .date(doc.getString("date"))
                    .totalWithoutTax(totalWithoutTax)
                    .tax(tax)
                    .build();

            orders.add(orderDTO);
        });

        return orders;
    }

    //ejercicio 9
    public List<OrderWithProductDTO> findOrdersWithCotoProducts() {
        Set<String> cotoProductIds = new HashSet<>();
        productCollection.find(Filters.eq("brand", "COTO"))
                .forEach(doc -> {
                    String id = doc.getString("id");
                    if (id != null) {
                        cotoProductIds.add(id);
                    }
                });

        List<OrderWithProductDTO> result = new ArrayList<>();
        orderCollection.find().forEach(orderDoc -> {
            List<Document> orderDetails = orderDoc.getList("orderDetails", Document.class, Collections.emptyList());

            boolean hasCotoProduct = orderDetails.stream()
                    .map(d -> d.getString("productId"))
                    .anyMatch(cotoProductIds::contains);

            if (hasCotoProduct) {
                List<Product> products = new ArrayList<>();
                for (Document detail : orderDetails) {
                    String pid = detail.getString("productId");
                    if (pid != null && cotoProductIds.contains(pid)) {
                        Document productDoc = productCollection.find(Filters.eq("id", pid)).first();
                        if (productDoc != null) {
                            products.add(fromProductDocument(productDoc));
                        }
                    }
                }

                OrderWithProductDTO dto = new OrderWithProductDTO(
                        orderDoc.getString("id"),
                        orderDoc.getString("supplierId"),
                        orderDoc.getString("date"),
                        orderDoc.getDouble("totalWithoutTax"),
                        orderDoc.getDouble("tax"),
                        products
                );
                result.add(dto);
            }
        });
        return result;
    }

    // ------------------------------------ CRUD ------------------------------------

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        for (Document doc : orderCollection.find()) {
            orders.add(fromDocument(doc));
        }
        return orders;
    }

    public Order findById(String id) {
        Document doc = orderCollection.find(new Document("id", id)).first();
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
        orderCollection.insertOne(doc);
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
        return orderCollection.updateOne(new Document("id", id), update).getModifiedCount() > 0;
    }

    public boolean delete(String id) {
        Order order = findById(id);
        if (order != null) {
            updateProductStock(order.orderDetails(), false);
        }
        
        return orderCollection.deleteOne(new Document("id", id)).getDeletedCount() > 0;
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
                // Manejar valores numéricos que pueden ser Integer o Double
                Number quantityNum = orderDetailDoc.get("quantity", Number.class);
                double quantity = quantityNum != null ? quantityNum.doubleValue() : 0.0;
                
                orderDetails.add(new OrderDetail(
                    orderDetailDoc.getString("orderId"),
                    orderDetailDoc.getString("productId"),
                    orderDetailDoc.getInteger("itemNumber"),
                    quantity
                ));
            }
        }
        
        // Manejar valores numéricos que pueden ser Integer o Double
        Number totalWithoutTaxNum = doc.get("totalWithoutTax", Number.class);
        Number taxNum = doc.get("tax", Number.class);
        
        double totalWithoutTax = totalWithoutTaxNum != null ? totalWithoutTaxNum.doubleValue() : 0.0;
        double tax = taxNum != null ? taxNum.doubleValue() : 0.0;
        
        return new Order(
                doc.getString("id"),
                doc.getString("supplierId"),
                doc.getString("date"),
                totalWithoutTax,
                tax,
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

    private Product fromProductDocument(Document doc) {
        return new Product(
                doc.getString("id"),
                doc.getString("description"),
                doc.getString("brand"),
                doc.getString("category"),
                doc.getDouble("price"),
                doc.getInteger("currentStock", 0),
                doc.getInteger("futureStock", 0)
        );
    }

} 
