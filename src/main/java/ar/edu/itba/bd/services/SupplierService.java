package ar.edu.itba.bd.services;

import ar.edu.itba.bd.database.MongoConnection;
import ar.edu.itba.bd.database.RedisConnection;
import ar.edu.itba.bd.dto.*;
import ar.edu.itba.bd.models.Phone;
import ar.edu.itba.bd.utils.RedisKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import ar.edu.itba.bd.models.Supplier;
import org.bson.conversions.Bson;
import redis.clients.jedis.Jedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mongodb.client.model.Filters.eq;


public class SupplierService {
    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

    private final MongoCollection<Document> supplierCollection;
    private final MongoCollection<Document> orderCollection;

    private final Jedis redisClient = RedisConnection.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int CACHE_TTL = 30;

    public SupplierService() {
        MongoDatabase db = MongoConnection.getDatabase("tp2025");
        this.supplierCollection = db.getCollection("supplier");
        this.orderCollection = db.getCollection("order");
    }


    // ----------------------------------- NEEDS ------------------------------------

    // ejercicio 6
    public List<SupplierWithOrderSummaryDTO> findAllSuppliersWithOrderSummary() {
        List<SupplierWithOrderSummaryDTO> suppliersDTO = new ArrayList<>();

        for (Document supplierDoc : supplierCollection.find()) {
            String supplierId = supplierDoc.getString("id");

            double totalWithoutTax = 0.0;
            double totalWithTax = 0.0;

            for (Document orderDoc : orderCollection.find(eq("supplierId", supplierId))) {
                Double orderTotal = orderDoc.getDouble("totalWithoutTax");
                Double orderTax = orderDoc.getDouble("tax");

                double total = orderTotal != null ? orderTotal : 0.0;
                double tax = orderTax != null ? orderTax : 0.0;

                totalWithoutTax += total;
                totalWithTax += total * (1 - (tax/100));
            }

            SupplierWithOrderSummaryDTO dto = new SupplierWithOrderSummaryDTO.Builder()
                    .supplierName(supplierDoc.getString("companyName"))
                    .id(supplierDoc.getString("id"))
                    .taxId(supplierDoc.getString("taxId"))
                    .companyName(supplierDoc.getString("companyName"))
                    .companyType(supplierDoc.getString("companyType"))
                    .address(supplierDoc.getString("address"))
                    .active(supplierDoc.getBoolean("active"))
                    .authorized(supplierDoc.getBoolean("authorized"))
                    .totalWithoutTax(totalWithoutTax)
                    .totalWithTax(totalWithTax)
                    .build();

            suppliersDTO.add(dto);
        }

        return suppliersDTO;
    }

    //ejercicio 1
    public List<Supplier> findAllActive() throws JsonProcessingException {
        String cachedResult = redisClient.get(RedisKeys.SUPPLIERS_ACTIVE);
        if (cachedResult != null) {
            logger.info("Cache HIT[Key: {}]", RedisKeys.SUPPLIERS_ACTIVE);
            return objectMapper.readValue(cachedResult, new TypeReference<>() {});
        }

        logger.info("Cache MISS[Key: {}]", RedisKeys.SUPPLIERS_ACTIVE);
        List<Supplier> suppliers = new ArrayList<>();
        for (Document doc : supplierCollection.find(eq("active", true))) {
            suppliers.add(fromDocument(doc));
        }

        String suppliersJson = objectMapper.writeValueAsString(suppliers);
        redisClient.setex(RedisKeys.SUPPLIERS_ACTIVE, CACHE_TTL, suppliersJson);

        return suppliers;
    }

    //ejercicio 2
    public List<SupplierTechWithPhones> findAllPhonesFromTech() {
        List<SupplierTechWithPhones> suppliers = new ArrayList<>();
        for (Document doc : supplierCollection.find()) {
            String companyName = doc.getString("companyName");
            if (companyName != null && companyName.contains("Tecno")) {
                String id = doc.getString("id");
                List<Phone> phones = findPhones(id, doc.getList("phones", Document.class));
                suppliers.add(new SupplierTechWithPhones(id, phones));
            }
        }
        return suppliers;
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

        for (Document doc : supplierCollection.find()) {
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
    public List<Supplier> findSuppliersWithOrders() {
        List<String> supplierIdsWithOrders = orderCollection
                .distinct("supplierId", String.class)
                .into(new ArrayList<>());

        List<Supplier> result = new ArrayList<>();
        supplierCollection
                .find(Filters.in("id", supplierIdsWithOrders))
                .forEach(doc -> result.add(fromDocument(doc)));

        return result;
    }

    // ejercicio 5
    public List<Supplier> findSuppliersWithoutOrders() {
        List<String> suppliersWithOrders = orderCollection.distinct("supplierId", String.class)
                .into(new ArrayList<>());

        Map<String, Supplier> uniqueSuppliers = new LinkedHashMap<>();
        for (Document doc : supplierCollection.find(new Document("id", new Document("$nin", suppliersWithOrders)))) {

            Supplier s = fromDocument(doc);
            uniqueSuppliers.putIfAbsent(s.id(), s);
        }

        return new ArrayList<>(uniqueSuppliers.values());
    }

    //ejercicio 12
    public List<Supplier> findActiveButDisabledSuppliers() {
        List<Supplier> result = new ArrayList<>();

        supplierCollection.find().forEach(doc -> {
            boolean isActive = doc.getBoolean("active");
            boolean isAuthorized = doc.getBoolean("authorized");
            if (isActive && !isAuthorized) {
                result.add(fromDocument(doc));
            }
        });

        return result;
    }

    // ------------------------------------ CRUD ------------------------------------

    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        for (Document doc : supplierCollection.find()) {
            suppliers.add(fromDocument(doc));
        }
        return suppliers;
    }

    public Supplier findById(String id) {
        Document doc = supplierCollection.find(new Document("id", id)).first();
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
        supplierCollection.insertOne(doc);
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
        return supplierCollection.updateOne(new Document("id", id), update).getModifiedCount() > 0;
    }

    public boolean delete(String id) {
        return supplierCollection.deleteOne(new Document("id", id)).getDeletedCount() > 0;
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

