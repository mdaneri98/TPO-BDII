package ar.edu.itba.bd.controllers;

import ar.edu.itba.bd.dto.*;
import ar.edu.itba.bd.models.ApiResponse;
import ar.edu.itba.bd.models.Supplier;
import ar.edu.itba.bd.services.SupplierService;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);
    private static final SupplierService supplierService = new SupplierService();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ----------------------------------- NEEDS ------------------------------------

    public static void getAllActive(Context ctx) {
        try {
            List<Supplier> suppliers = supplierService.findAllActive();
            ctx.json(new ApiResponse("Proveedores obtenidos exitosamente", suppliers));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener proveedores activos: " + e.getMessage()));
        }
    }

    public static void getAllFromTech(Context ctx) {
        try {
            List<SupplierTechWithPhones> suppliers = supplierService.findAllPhonesFromTech();
            ctx.json(new ApiResponse("Proveedores obtenidos exitosamente", suppliers));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener proveedores activos: " + e.getMessage()));
        }
    }

    public static void getSupplierAndEachPhone(Context ctx) {
        try {
            List<SupplierWithPhoneDTO> suppliers = supplierService.findSupplierAndEachPhone();
            ctx.json(new ApiResponse("Proveedores obtenidos exitosamente", suppliers));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener proveedores activos: " + e.getMessage()));
        }
    }

    public static void getAllSuppliersWithOrderSummary(Context ctx) {
        try {
            List<SupplierWithOrderSummaryDTO> suppliers = supplierService.findAllSuppliersWithOrderSummary();
            ctx.json(new ApiResponse("Proveedores obtenidos exitosamente", suppliers));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener proveedores: " + e.getMessage()));
        }
    }
    public static void getSuppliersWithOrders(Context ctx) {
        try {
            List<Supplier> suppliers = supplierService.findSuppliersWithOrders();
            ctx.json(new ApiResponse("Proveedores obtenidos exitosamente", suppliers));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener proveedores activos: " + e.getMessage()));
        }
    }

    public static void getSuppliersActiveButDisabled(Context ctx) {
        try {
            List<Supplier> suppliers = supplierService.findActiveButDisabledSuppliers();
            ctx.json(new ApiResponse("Proveedores obtenidos exitosamente", suppliers));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener proveedores activos: " + e.getMessage()));
        }
    }

    // ------------------------------------ CRUD ------------------------------------

    public static void getAllSuppliers(Context ctx) {
        try {
            List<Supplier> suppliers = supplierService.findAll();
            ctx.json(new ApiResponse("Proveedores obtenidos exitosamente", suppliers));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener proveedores: " + e.getMessage()));
        }
    }

    public static void getSupplierById(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Supplier supplier = supplierService.findById(id);
            
            if (supplier != null) {
                ctx.json(new ApiResponse("Proveedor encontrado", supplier));
            } else {
                ctx.status(404);
                ctx.json(new ApiResponse("Proveedor no encontrado"));
            }
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener proveedor: " + e.getMessage()));
        }
    }

    public static void createSupplier(Context ctx) {
        try {
            Supplier supplier = objectMapper.readValue(ctx.body(), Supplier.class);
            supplierService.insert(supplier);
            ctx.status(201);
            ctx.json(new ApiResponse("Proveedor creado exitosamente"));
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(new ApiResponse("Error al crear proveedor: " + e.getMessage()));
        }
    }

    // FIXME: Aca hay que tener en cuenta que el supplier embebe al telefono.
    public static void updateSupplier(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Supplier supplier = objectMapper.readValue(ctx.body(), Supplier.class);
            
            boolean updated = supplierService.update(id, supplier);
            if (updated) {
                ctx.json(new ApiResponse("Proveedor actualizado exitosamente"));
            } else {
                ctx.status(404);
                ctx.json(new ApiResponse("Proveedor no encontrado"));
            }
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(new ApiResponse("Error al actualizar proveedor: " + e.getMessage()));
        }
    }

    public static void deleteSupplier(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            boolean deleted = supplierService.delete(id);
            
            if (deleted) {
                ctx.json(new ApiResponse("Proveedor eliminado exitosamente"));
            } else {
                ctx.status(404);
                ctx.json(new ApiResponse("Proveedor no encontrado"));
            }
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al eliminar proveedor: " + e.getMessage()));
        }
    }
    public static void getSuppliersWithoutOrders(Context ctx) {
        logger.info("getSuppliersWithoutOrders called");
        try {
            List<Supplier> suppliers = supplierService.findSuppliersWithoutOrders();
            ctx.json(new ApiResponse("Proveedores sin órdenes obtenidos exitosamente"));
            ctx.json(suppliers);
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ApiResponse("Error al obtener proveedores sin órdenes: " + e.getMessage()));
        }
    }


} 
