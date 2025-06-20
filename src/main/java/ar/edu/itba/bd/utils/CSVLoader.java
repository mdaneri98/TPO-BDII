package ar.edu.itba.bd.utils;


import ar.edu.itba.bd.database.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVLoader {

    private static final String DATA_PATH = "data";

    public static void main(String[] args) {
        MongoDatabase db = MongoConnection.getDatabase("tp2025");
        MongoCollection<Document> collection = db.getCollection("proveedor");

        try (BufferedReader br = new BufferedReader(new FileReader(DATA_PATH + "/proveedor.csv"))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] fields = line.split(";", -1);

                if (fields.length != 7) {
                    System.err.println("Línea con formato incorrecto: " + line);
                    continue;
                }

                try {
                    Document proveedor = new Document()
                            .append("id", fields[0].trim())
                            .append("taxId", fields[1].trim())
                            .append("companyName", fields[2].trim())
                            .append("companyType", fields[3].trim())
                            .append("address", fields[4].trim().isEmpty() ? null : fields[4].trim())
                            .append("active", fields[5].trim().equals("1"))
                            .append("authorized", fields[6].trim().equals("1"));

                    collection.insertOne(proveedor);
                } catch (Exception e) {
                    System.err.println("Error al procesar línea: " + line);
                    e.printStackTrace();
                }
            }

            System.out.println("Carga finalizada con éxito.");
        } catch (IOException e) {
            System.err.println("Error al leer el archivo CSV: " + e.getMessage());
        }
    }
}
