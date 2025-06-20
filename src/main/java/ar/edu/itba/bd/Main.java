package ar.edu.itba.bd;


import ar.edu.itba.bd.dto.Supplier;
import ar.edu.itba.bd.services.SupplierService;
import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {
        SupplierService supplierService = new SupplierService();


        Javalin app = Javalin.create().start(7000);


        app.get("/suppliers", ctx -> {
            ctx.json(supplierService.findAll());
        });

        app.get("/suppliers/{id}", ctx -> {
            Supplier supplier = supplierService.findById(ctx.pathParam("id"));
            if (supplier != null) {
                ctx.json(supplier);
            } else {
                ctx.status(404).result("Supplier not found");
            }
        });

        app.post("/suppliers", ctx -> {
            Supplier supplier = ctx.bodyAsClass(Supplier.class);
            supplierService.insert(supplier);
            ctx.status(201).json(supplier);
        });

        app.put("/suppliers/{id}", ctx -> {
            Supplier supplier = ctx.bodyAsClass(Supplier.class);
            boolean updated = supplierService.update(ctx.pathParam("id"), supplier);
            ctx.status(updated ? 200 : 404);
        });

        app.delete("/suppliers/{id}", ctx -> {
            boolean deleted = supplierService.delete(ctx.pathParam("id"));
            ctx.status(deleted ? 204 : 404);
        });

    }
}
