package ar.edu.itba.bd;

import ar.edu.itba.bd.controllers.OrderController;
import ar.edu.itba.bd.controllers.ProductController;
import ar.edu.itba.bd.controllers.PingController;
import ar.edu.itba.bd.controllers.SupplierController;
import io.javalin.Javalin;
import ar.edu.itba.bd.utils.CSVLoader;

public class Main {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);

        // Ping endpoint
        app.get("/ping", PingController::ping);
        app.get("/load-data", (ctx) -> {
            new CSVLoader().loadAllData();
            ctx.status(200);
        });

        // ----------------- Supplier routes -----------------
        // Rutas específicas primero
        app.get("/suppliers/active/phones", SupplierController::getAllActive);
        app.get("/suppliers/tech/phones", SupplierController::getAllFromTech);
        app.get("/suppliers/phones", SupplierController::getSupplierAndEachPhone);
        app.get("/suppliers/with-orders", SupplierController::getSuppliersWithOrders);
        app.get("/suppliers/with-orders-summary", SupplierController::getAllSuppliersWithOrderSummary);
        app.get("/suppliers/without-orders", SupplierController::getSuppliersWithoutOrders);
        app.get("/suppliers/active-unauthorized", SupplierController::getSuppliersActiveAndUnauthorizedSuppliers);

        // CRUD
        app.get("/suppliers", SupplierController::getAllSuppliers);
        app.get("/suppliers/{id}", SupplierController::getSupplierById);
        app.post("/suppliers", SupplierController::createSupplier);
        app.put("/suppliers/{id}", SupplierController::updateSupplier);
        app.delete("/suppliers/{id}", SupplierController::deleteSupplier);

        // ----------------- Order routes -----------------

        // Rutas específicas primero
        app.get("/orders/by-supplier-tax-id", OrderController::getOrdersBySupplierTaxId);
        app.get("/orders/with-coto-products", OrderController::getOrdersWithCotoProducts);
        // CRUD
        app.get("/orders", OrderController::getAllOrders);
        app.get("/orders/coto", OrderController::getOrdersWithCotoProducts);
        app.get("/orders/summary", OrderController::getOrderSummaries);
        app.get("/orders/{id}", OrderController::getOrderById);
        app.post("/orders", OrderController::createOrder);
        app.put("/orders/{id}", OrderController::updateOrder);
        app.delete("/orders/{id}", OrderController::deleteOrder);

        // ----------------- Product routes -----------------

        // Rutas específicas primero
        app.get("/products/with-orders", ProductController::getAllWithAtLeastOneOrder);
        app.get("/products/without-orders", ProductController::getAllProductsNotOrdered);

        // CRUD
        app.get("/products", ProductController::getAllProducts);
        app.get("/products/{id}", ProductController::getProductById);
        app.post("/products", ProductController::createProduct);
        app.put("/products/{id}", ProductController::updateProduct);
        app.delete("/products/{id}", ProductController::deleteProduct);
    }
}
