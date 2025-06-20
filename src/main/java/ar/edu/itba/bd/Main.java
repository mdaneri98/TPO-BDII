package ar.edu.itba.bd;

import ar.edu.itba.bd.controllers.OrderController;
import ar.edu.itba.bd.controllers.ProductController;
import ar.edu.itba.bd.controllers.PingController;
import ar.edu.itba.bd.controllers.SupplierController;
import ar.edu.itba.bd.models.Order;
import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);

        // Ping endpoint
        app.get("/ping", PingController::ping);

        // ----------------- Supplier routes -----------------
        // Rutas específicas primero
        app.get("/suppliers/phonesFromActive", SupplierController::getAllActive);
        app.get("/suppliers/phonesFromTech", SupplierController::getAllFromTech);
        app.get("/suppliers/phones", SupplierController::getSupplierAndEachPhone);
        app.get("/suppliers/suppliersWithOrders", SupplierController::getSuppliersWithOrders);
        app.get("/suppliers/suppliersWithOrdersSummary", SupplierController::getAllSuppliersWithOrderSummary);
        app.get("/suppliers/without-orders", SupplierController::getSuppliersWithoutOrders);
        app.get("/suppliers/suppliersActiveButDisabled", SupplierController::getSuppliersActiveButDisabled);

        // CRUD
        app.get("/suppliers", SupplierController::getAllSuppliers);
        app.get("/suppliers/{id}", SupplierController::getSupplierById);
        app.post("/suppliers", SupplierController::createSupplier);
        app.put("/suppliers/{id}", SupplierController::updateSupplier);
        app.delete("/suppliers/{id}", SupplierController::deleteSupplier);

        // ----------------- Order routes -----------------

        // Rutas específicas primero
        app.get("/orders/byTaxId", OrderController::getOrdersBySupplierTaxId);
        app.get("/orders/coto-products", OrderController::getOrdersWithCotoProducts);
        // CRUD
        app.get("/orders", OrderController::getAllOrders);
        app.get("/orders/coto", OrderController::getOrdersWithCotoProducts);
        app.get("/orders/{id}", OrderController::getOrderById);
        app.post("/orders", OrderController::createOrder);
        app.put("/orders/{id}", OrderController::updateOrder);
        app.delete("/orders/{id}", OrderController::deleteOrder);

        // ----------------- Product routes -----------------

        // Rutas específicas primero
        app.get("/products/withatleastoneorder", ProductController::getAllWithAtLeastOneOrder);
        app.get("/products/productsNotOrdered", ProductController::getAllProductsNotOrdered);

        // CRUD
        app.get("/products", ProductController::getAllProducts);
        app.get("/products/{id}", ProductController::getProductById);
        app.post("/products", ProductController::createProduct);
        app.put("/products/{id}", ProductController::updateProduct);
        app.delete("/products/{id}", ProductController::deleteProduct);
    }
}
