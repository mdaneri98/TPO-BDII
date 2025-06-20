package ar.edu.itba.bd.services;

import ar.edu.itba.bd.database.MongoConnection;
import ar.edu.itba.bd.database.RedisConnection;
import ar.edu.itba.bd.models.Product;
import ar.edu.itba.bd.utils.RedisKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.*;

public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

    private final MongoCollection<Document> orderCollection;
    private final MongoCollection<Document> productCollection;

    private final Jedis redisClient = RedisConnection.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int CACHE_TTL = 30;

    public ProductService() {
        MongoDatabase db = MongoConnection.getDatabase("tp2025");
        this.orderCollection = db.getCollection("order");
        this.productCollection = db.getCollection("product");
    }


    // ----------------------------------- NEEDS ------------------------------------

    //ejercicio 8
    //ejercicio 8
    public List<Product> findAllWithAtLeastOneOrder() throws JsonProcessingException {
        String cachedResult = redisClient.get(RedisKeys.ORDERED_PRODUCTS);
        if (cachedResult != null) {
            logger.info("Cache HIT[Key: {}]", RedisKeys.ORDERED_PRODUCTS);
            return objectMapper.readValue(cachedResult, new TypeReference<>() {});
        }

        logger.info("Cache MISS[Key: {}]", RedisKeys.ORDERED_PRODUCTS);
        List<Bson> pipeline = List.of(
                Aggregates.lookup("order", "id", "orderDetails.productId", "matchedOrders"),
                Aggregates.match(Filters.expr(
                        new Document("$gt", List.of(new Document("$size", "$matchedOrders"), 0))
                ))
        );

        List<Product> products = new ArrayList<>();
        productCollection.aggregate(pipeline).forEach(doc -> {
            products.add(fromDocument(doc));
        });

        String productsJson = objectMapper.writeValueAsString(products);
        redisClient.setex(RedisKeys.ORDERED_PRODUCTS, CACHE_TTL, productsJson);

        return products;
    }

    //ejercicio 11
    public List<Product> findProductsNotOrdered() {
        Set<String> orderedProductIds = new HashSet<>();

        orderCollection.find().forEach(orderDoc -> {
            List<Document> orderDetails = orderDoc.getList("orderDetails", Document.class, Collections.emptyList());
            for (Document detail : orderDetails) {
                String productId = detail.getString("productId");
                if (productId != null) {
                    orderedProductIds.add(productId);
                }
            }
        });

        List<Product> result = new ArrayList<>();
        productCollection.find().forEach(doc -> {
            String productId = doc.getString("id");
            if (orderedProductIds.isEmpty() || !orderedProductIds.contains(productId)) {
                result.add(fromDocument(doc));
            }
        });

        return result;
    }


    // ------------------------------------ CRUD ------------------------------------

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        for (Document doc : productCollection.find()) {
            products.add(fromDocument(doc));
        }
        return products;
    }

    public Product findById(String id) {
        Document doc = productCollection.find(new Document("id", id)).first();
        return doc != null ? fromDocument(doc) : null;
    }

    public void insert(Product product) {
        Document doc = new Document()
                .append("id", product.id())
                .append("description", product.description())
                .append("brand", product.brand())
                .append("category", product.category())
                .append("price", product.price())
                .append("currentStock", product.currentStock())
                .append("futureStock", product.futureStock());
        productCollection.insertOne(doc);
    }

    public boolean update(String id, Product product) {
        Document update = new Document("$set", new Document()
                .append("id", product.id())
                .append("description", product.description())
                .append("brand", product.brand())
                .append("category", product.category())
                .append("price", product.price())
                .append("currentStock", product.currentStock())
                .append("futureStock", product.futureStock()));
        return productCollection.updateOne(new Document("id", id), update).getModifiedCount() > 0;
    }

    public boolean delete(String id) {
        return productCollection.deleteOne(new Document("id", id)).getDeletedCount() > 0;
    }

    private Product fromDocument(Document doc) {
        Number priceNum = doc.get("price", Number.class);
        double price = priceNum != null ? priceNum.doubleValue() : 0.0;
        
        return new Product(
                doc.getString("id"),
                doc.getString("description"),
                doc.getString("brand"),
                doc.getString("category"),
                price,
                doc.getInteger("currentStock"),
                doc.getInteger("futureStock")
        );
    }
} 
