package ar.edu.itba.bd.services;

import ar.edu.itba.bd.database.MongoConnection;
import ar.edu.itba.bd.dto.Product;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    private final MongoCollection<Document> collection;

    public ProductService() {
        MongoDatabase db = MongoConnection.getDatabase("tp2025");
        this.collection = db.getCollection("product");
    }

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        for (Document doc : collection.find()) {
            products.add(fromDocument(doc));
        }
        return products;
    }

    public Product findById(String id) {
        Document doc = collection.find(new Document("id", id)).first();
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
        collection.insertOne(doc);
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
        return collection.updateOne(new Document("id", id), update).getModifiedCount() > 0;
    }

    public boolean delete(String id) {
        return collection.deleteOne(new Document("id", id)).getDeletedCount() > 0;
    }

    private Product fromDocument(Document doc) {
        return new Product(
                doc.getString("id"),
                doc.getString("description"),
                doc.getString("brand"),
                doc.getString("category"),
                doc.getDouble("price"),
                doc.getInteger("currentStock"),
                doc.getInteger("futureStock")
        );
    }
} 