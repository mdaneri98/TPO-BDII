package ar.edu.itba.bd.controllers;


import ar.edu.itba.bd.models.ApiResponse;
import io.javalin.http.Context;

public class PingController {

    public static void ping(Context ctx) {



        ctx.json(new ApiResponse("pong"));
    }

}
