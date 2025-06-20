package ar.edu.itba.bd.controllers;

import ar.edu.itba.bd.dto.OrderDTO;
import ar.edu.itba.bd.models.ApiResponse;
import ar.edu.itba.bd.models.Order;
import ar.edu.itba.bd.services.OrderService;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class OrderController {

    private static final OrderService orderService = new OrderService();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ----------------------------------- NEEDS ------------------------------------

    public static void getOrdersBySupplierTaxId(Context ctx) {
        try {
            String taxId = "30660608175";
            List<OrderDTO> orders = orderService.findOrdersBySupplierTaxId(taxId);
            ctx.json(new ApiResponse("Órdenes obtenidas exitosamente", orders));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener órdenes: " + e.getMessage()));
        }
    }



    // ------------------------------------ CRUD ------------------------------------

    public static void getAllOrders(Context ctx) {
        try {
            List<Order> orders = orderService.findAll();
            ctx.json(new ApiResponse("Órdenes obtenidas exitosamente", orders));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener órdenes: " + e.getMessage()));
        }
    }

    public static void getOrderById(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Order order = orderService.findById(id);
            
            if (order != null) {
                ctx.json(new ApiResponse("Orden encontrada", order));
            } else {
                ctx.status(404);
                ctx.json(new ApiResponse("Orden no encontrada"));
            }
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener orden: " + e.getMessage()));
        }
    }

    public static void createOrder(Context ctx) {
        try {
            Order order = objectMapper.readValue(ctx.body(), Order.class);
            orderService.insert(order);
            ctx.status(201);
            ctx.json(new ApiResponse("Orden creada exitosamente"));
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(new ApiResponse("Error al crear orden: " + e.getMessage()));
        }
    }

    // FIXME: Aca hay que tener en cuenta que el order embebe al orderDetail.
    public static void updateOrder(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Order order = objectMapper.readValue(ctx.body(), Order.class);
            
            boolean updated = orderService.update(id, order);
            if (updated) {
                ctx.json(new ApiResponse("Orden actualizada exitosamente"));
            } else {
                ctx.status(404);
                ctx.json(new ApiResponse("Orden no encontrada"));
            }
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(new ApiResponse("Error al actualizar orden: " + e.getMessage()));
        }
    }

    public static void deleteOrder(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            boolean deleted = orderService.delete(id);
            
            if (deleted) {
                ctx.json(new ApiResponse("Orden eliminada exitosamente"));
            } else {
                ctx.status(404);
                ctx.json(new ApiResponse("Orden no encontrada"));
            }
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al eliminar orden: " + e.getMessage()));
        }
    }
    public static void getOrdersWithCotoProducts(Context ctx) {
        try {
            List<Order> orders = orderService.findOrdersWithCotoProducts();
            ctx.json(new ApiResponse("Órdenes con productos de la marca COTO obtenidas exitosamente"));
            ctx.json(orders);
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener órdenes: " + e.getMessage()));
        }
    }

} 