package ar.edu.itba.bd;

import ar.edu.itba.bd.database.MongoConnection;
import ar.edu.itba.bd.database.MongoService;
import ar.edu.itba.bd.dto.Proveedor;
import io.javalin.Javalin;
import com.mongodb.client.*;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        MongoService mongoService = new MongoService();

        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
        });

        app.get("/ping", ctx -> {
            ctx.result("pong");
        });

        app.get("/proveedores", ctx -> {
            MongoDatabase db = MongoConnection.getDatabase("tp2025");
            MongoCollection<Document> col = db.getCollection("proveedor");
            FindIterable<Document> docs = col.find();

            List<Proveedor> proveedores = new ArrayList<>();
            for (Document doc : docs) {
                Proveedor p = new Proveedor();
                p.cuit = doc.getString("cuit");
                p.razon_social = doc.getString("razon_social");
                p.activo = doc.getBoolean("activo", false);
                p.habilitado = doc.getBoolean("habilitado", false);
                p.telefonos = (List<String>) doc.get("telefonos");
                proveedores.add(p);
            }
            ctx.json(proveedores); 
        });

        app.start(7070);
    }
}
