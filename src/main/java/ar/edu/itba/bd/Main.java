package ar.edu.itba.bd;

import ar.edu.itba.bd.controllers.PingController;
import ar.edu.itba.bd.database.MongoService;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

    public static void main(String[] args) {

        MongoService mongoService = new MongoService();

        Javalin app = Javalin.create(config -> {
            config.router.apiBuilder(() -> {
                get("/ping", PingController::ping);
            });
        }).start(7070);

    }
}