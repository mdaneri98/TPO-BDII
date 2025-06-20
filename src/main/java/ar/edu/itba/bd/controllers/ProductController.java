package ar.edu.itba.bd.controllers;

import ar.edu.itba.bd.dto.ApiResponse;
import ar.edu.itba.bd.dto.Product;
import ar.edu.itba.bd.services.ProductService;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class ProductController {

    private static final ProductService productService = new ProductService();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void getAllProducts(Context ctx) {
        try {
            List<Product> products = productService.findAll();
            ctx.json(new ApiResponse("Productos obtenidos exitosamente"));
            ctx.json(products);
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener productos: " + e.getMessage()));
        }
    }

    public static void getProductById(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Product product = productService.findById(id);
            
            if (product != null) {
                ctx.json(new ApiResponse("Producto encontrado"));
                ctx.json(product);
            } else {
                ctx.status(404);
                ctx.json(new ApiResponse("Producto no encontrado"));
            }
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener producto: " + e.getMessage()));
        }
    }

    public static void createProduct(Context ctx) {
        try {
            Product product = objectMapper.readValue(ctx.body(), Product.class);
            productService.insert(product);
            ctx.status(201);
            ctx.json(new ApiResponse("Producto creado exitosamente"));
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(new ApiResponse("Error al crear producto: " + e.getMessage()));
        }
    }

    public static void updateProduct(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Product product = objectMapper.readValue(ctx.body(), Product.class);
            
            boolean updated = productService.update(id, product);
            if (updated) {
                ctx.json(new ApiResponse("Producto actualizado exitosamente"));
            } else {
                ctx.status(404);
                ctx.json(new ApiResponse("Producto no encontrado"));
            }
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(new ApiResponse("Error al actualizar producto: " + e.getMessage()));
        }
    }

    public static void deleteProduct(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            boolean deleted = productService.delete(id);
            
            if (deleted) {
                ctx.json(new ApiResponse("Producto eliminado exitosamente"));
            } else {
                ctx.status(404);
                ctx.json(new ApiResponse("Producto no encontrado"));
            }
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al eliminar producto: " + e.getMessage()));
        }
    }
} 