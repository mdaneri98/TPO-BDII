package ar.edu.itba.bd.services;

import ar.edu.itba.bd.database.MongoConnection;
import ar.edu.itba.bd.dto.*;
import ar.edu.itba.bd.models.Order;
import ar.edu.itba.bd.models.Phone;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ar.edu.itba.bd.models.Supplier;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;



public class SupplierService {

    private final MongoCollection<Document> collection;

    public SupplierService() {
        MongoDatabase db = MongoConnection.getDatabase("tp2025");
        this.collection = db.getCollection("supplier");
    }


    // ----------------------------------- NEEDS ------------------------------------

    // ejercicio 6
    public List<SupplierWithOrderSummaryDTO> findAllSuppliersWithOrderSummary() {
        List<SupplierWithOrderSummaryDTO> suppliersDTO = new ArrayList<>();

        List<Bson> pipeline = List.of(
                lookup("order", "id", "supplierId", "orders"),
                Aggregates.addFields(new Field<>("orders", new Document("$ifNull", List.of("$orders", List.of())))),
                Aggregates.project(
                        new Document("supplierName", "$companyName")
                                .append("id", "$id")
                                .append("taxId", "$taxId")
                                .append("companyName", "$companyName")
                                .append("companyType", "$companyType")
                                .append("address", "$address")
                                .append("active", "$active")
                                .append("authorized", "$authorized")
                                .append("totalWithoutTax", new Document("$sum", "$orders.totalWithoutTax"))
                                .append("tax", new Document("$sum", "$orders.tax"))
                )
        );

        collection.aggregate(pipeline).forEach(doc -> {
            Number tax = doc.get("tax", Number.class);
            Number totalWithoutTax = doc.get("totalWithoutTax", Number.class);

            SupplierWithOrderSummaryDTO dto = new SupplierWithOrderSummaryDTO.Builder()
                    .supplierName(doc.getString("supplierName"))
                    .id(doc.getString("id"))
                    .taxId(doc.getString("taxId"))
                    .companyName(doc.getString("companyName"))
                    .companyType(doc.getString("companyType"))
                    .address(doc.getString("address"))
                    .active(doc.getBoolean("active"))
                    .authorized(doc.getBoolean("authorized"))
                    .tax(tax != null ? tax.doubleValue() : 0.0)
                    .totalWithoutTax(totalWithoutTax != null ? totalWithoutTax.doubleValue() : 0.0)
                    .build();

            suppliersDTO.add(dto);
        });

        return suppliersDTO;
    }

    //ejercicio 1
    public List<Supplier> findAllActive() {
        List<Supplier> suppliers = new ArrayList<>();
        for (Document doc : collection.find()) {
            if (doc.getBoolean("active").equals(true)) {
                suppliers.add(fromDocument(doc));
            }
        }
        return suppliers;
    }

    //ejercicio 2
    public List<SupplierTechWithPhones> findAllPhonesFromTech() {
        List<SupplierTechWithPhones> suppliers = new ArrayList<>();
        for (Document doc : collection.find()) {
            String taxId = doc.getString("taxId");
            if (taxId != null && taxId.equals("Tecnología")) {
                String id = doc.getString("id");
                List<Phone> phones = findPhones(id, doc.getList("phones", Document.class));
                suppliers.add(new SupplierTechWithPhones(id, phones));
            }
        }
        return suppliers;
        /*Bson filter = Filters.eq("taxId", "Tecnología");
        for (Document doc : collection.find(filter)) {
            String id = doc.getString("id");
            List<Phone> phones = findPhones(id, doc.getList("phones", Document.class));
            suppliers.add(new SupplierTechWithPhones(id, phones));
        }

        return suppliers;*/
    }

    private List<Phone> findPhones(String id, List<Document> phonesFromSupplier) {
        List<Phone> phones = new ArrayList<>();
        if(phonesFromSupplier != null) {
            for (Document doc : phonesFromSupplier) {
                String type = doc.getString("type");
                String phoneNumber = doc.getString("phoneNumber");
                String areaCode = doc.getString("areaCode");
                phones.add(new Phone(id, areaCode, phoneNumber, type));
            }
        }
        return phones;
    }


    //ejercicio 3
    public List<SupplierWithPhoneDTO> findSupplierAndEachPhone() {
        List<SupplierWithPhoneDTO> supplierWithPhoneDTOS = new ArrayList<>();

        for (Document doc : collection.find()) {
            String id = doc.getString("id");
            String taxId = doc.getString("taxId");
            String companyName = doc.getString("companyName");
            String companyType = doc.getString("companyType");
            String address = doc.getString("address");
            boolean active = doc.getBoolean("active", false);
            boolean authorized = doc.getBoolean("authorized", false);
            List<Document> phoneDocs = (List<Document>) doc.get("phones");
            List<Phone> phones = findPhones(id, phoneDocs);

            for (Phone phone : phones) {
                supplierWithPhoneDTOS.add(new SupplierWithPhoneDTO(id, taxId, companyName, companyType, address,
                        active, authorized, phone.areaCode(), phone.phoneNumber(), phone.type()
                ));
            }
        }
        return supplierWithPhoneDTOS;
    }

    //ejercicio 4
    public List<SuppliersWithRegisterOrderDTO> findSuppliersWithOrders() {
        List<SuppliersWithRegisterOrderDTO> suppliersWithRegisterOrderDTOS = new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
                Aggregates.lookup("orders", "id", "supplierId", "orders"),
                Aggregates.match(Filters.expr(new Document("$gt", Arrays.asList(new Document("$size", "$orders"), 0)))),
                Aggregates.project(Projections.fields(
                        Projections.include("id", "supplierName")
                ))
        );

        collection.aggregate(pipeline).forEach(doc -> {
            SuppliersWithRegisterOrderDTO supplier = new SuppliersWithRegisterOrderDTO(doc.getString("id"), doc.getString("supplierName"));
            suppliersWithRegisterOrderDTOS.add(supplier);
        });

        return suppliersWithRegisterOrderDTOS;
    }


    // ------------------------------------ CRUD ------------------------------------

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
}

