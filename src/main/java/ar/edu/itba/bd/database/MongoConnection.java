package ar.edu.itba.bd.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {

    private static final String CONNECTION_STRING = "mongodb://root:example@localhost:27017/?authSource=admin";
    private static final MongoClient client = MongoClients.create(CONNECTION_STRING);

    private MongoConnection() {}

    public static MongoDatabase getDatabase(String dbName) {
        return client.getDatabase(dbName);
    }
}
