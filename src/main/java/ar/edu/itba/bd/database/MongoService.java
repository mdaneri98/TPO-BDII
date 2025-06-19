package ar.edu.itba.bd.database;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoService {
    private static final String connectionString = "mongodb+srv://mdaneri:tpo-bdii@cluster0.mxqnzzx.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoService() {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .codecRegistry(pojoCodecRegistry)
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("stock").withCodecRegistry(pojoCodecRegistry);

        try {
            database.runCommand(new Document("ping", 1));
            System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public MongoDatabase getDatabase() {
        return this.database;
    }

    public void close() {
        mongoClient.close();
    }
}
