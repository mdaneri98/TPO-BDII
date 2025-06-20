package ar.edu.itba.bd.services;

import ar.edu.itba.bd.database.MongoConnection;
import ar.edu.itba.bd.dto.Phone;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ar.edu.itba.bd.dto.Supplier;
import org.bson.types.ObjectId;


public class SupplierService {

    private final MongoCollection<Document> collection;

    public SupplierService() {
        MongoDatabase db = MongoConnection.getDatabase("tp2025");
        this.collection = db.getCollection("supplier");
    }

    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        for (Document doc : collection.find()) {
            suppliers.add(fromDocument(doc));
        }
        return suppliers;
    }

    public Supplier findById(String id) {
        Document doc = collection.find(new Document("id", id)).first();
        return doc != null ? fromDocument(doc) : null;
    }

    public void insert(Supplier s) {
        Document doc = new Document()
                .append("id", s.id())
                .append("taxId", s.taxId())
                .append("companyName", s.companyName())
                .append("companyType", s.companyType())
                .append("address", s.address())
                .append("active", s.active())
                .append("authorized", s.authorized())
                .append("phones", phonesToDocuments(s.phones()));
        collection.insertOne(doc);
    }

    public boolean update(String id, Supplier s) {
        Document update = new Document("$set", new Document()
                .append("id", s.id())
                .append("taxId", s.taxId())
                .append("companyName", s.companyName())
                .append("companyType", s.companyType())
                .append("address", s.address())
                .append("active", s.active())
                .append("authorized", s.authorized())
                .append("phones", phonesToDocuments(s.phones())));
        return collection.updateOne(new Document("id", id), update).getModifiedCount() > 0;
    }

    public boolean delete(String id) {
        return collection.deleteOne(new Document("id", id)).getDeletedCount() > 0;
    }

    private Supplier fromDocument(Document doc) {
        List<Phone> phones = new ArrayList<>();
        List<Document> phoneDocs = doc.getList("phones", Document.class);
        if (phoneDocs != null) {
            for (Document phoneDoc : phoneDocs) {
                phones.add(new Phone(
                    phoneDoc.getString("supplierId"),
                    phoneDoc.getString("areaCode"),
                    phoneDoc.getString("phoneNumber"),
                    phoneDoc.getString("type")
                ));
            }
        }
        
        return new Supplier(
                doc.getString("id"),
                doc.getString("taxId"),
                doc.getString("companyName"),
                doc.getString("companyType"),
                doc.getString("address"),
                doc.getBoolean("active", false),
                doc.getBoolean("authorized", false),
                phones
        );
    }

    private List<Document> phonesToDocuments(List<Phone> phones) {
        List<Document> phoneDocs = new ArrayList<>();
        if (phones != null) {
            for (Phone phone : phones) {
                phoneDocs.add(new Document()
                    .append("supplierId", phone.supplierId())
                    .append("areaCode", phone.areaCode())
                    .append("phoneNumber", phone.phoneNumber())
                    .append("type", phone.type()));
            }
        }
        return phoneDocs;
    }

    public List<Supplier> findSuppliersWithoutOrders() {
        MongoDatabase db = MongoConnection.getDatabase("tp2025");
        MongoCollection<Document> orders = db.getCollection("order");

        List<String> cuitWithOrders = orders.distinct("supplierId", String.class)
                .into(new ArrayList<>());

        Map<String, Supplier> uniqueSuppliers = new LinkedHashMap<>();
        for (Document doc : collection.find(new Document("id", new Document("$nin", cuitWithOrders)))) {
            Supplier s = fromDocument(doc);
            uniqueSuppliers.putIfAbsent(s.id(), s);
        }

        return new ArrayList<>(uniqueSuppliers.values());
    }

}

