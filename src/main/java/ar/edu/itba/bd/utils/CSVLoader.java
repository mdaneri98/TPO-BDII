package ar.edu.itba.bd.utils;

import ar.edu.itba.bd.models.Order;
import ar.edu.itba.bd.models.OrderDetail;
import ar.edu.itba.bd.models.Phone;
import ar.edu.itba.bd.models.Product;
import ar.edu.itba.bd.models.Supplier;
import ar.edu.itba.bd.services.OrderService;
import ar.edu.itba.bd.services.ProductService;
import ar.edu.itba.bd.services.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVLoader {
    private static final Logger logger = LoggerFactory.getLogger(CSVLoader.class);
    
    private static final String DATA_PATH = "data";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");
    
    private final SupplierService supplierService;
    private final ProductService productService;
    private final OrderService orderService;

    public CSVLoader() {
        this.supplierService = new SupplierService();
        this.productService = new ProductService();
        this.orderService = new OrderService();
    }

    public void loadAllData() {
        logger.info("Iniciando carga de datos CSV...");
        
        try {
            loadSuppliers();
            loadProducts();
            loadPhones();
            loadOrders();
            
            logger.info("Carga de datos completada exitosamente.");
            
        } catch (Exception e) {
            logger.error("Error durante la carga de datos: " + e.getMessage(), e);
        }
    }

    private void loadSuppliers() {
        logger.info("Cargando proveedores...");
        String filePath = DATA_PATH + "/proveedor.csv";
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] fields = line.split(";", -1);
                if (fields.length != 7) {
                    logger.warn("Línea de proveedor con formato incorrecto: " + line);
                    continue;
                }

                try {
                    Supplier supplier = new Supplier(
                        fields[0].trim(),
                        fields[1].trim(),
                        fields[2].trim(),
                        fields[3].trim(),
                        fields[4].trim().isEmpty() ? null : fields[4].trim(),
                        fields[5].trim().equals("1"),
                        fields[6].trim().equals("1"),
                        new ArrayList<>() // Teléfonos vacíos inicialmente
                    );

                    supplierService.insert(supplier);
                    count++;
                } catch (Exception e) {
                    logger.error("Error al procesar proveedor: " + line, e);
                }
            }
            logger.info("Proveedores cargados: " + count);
            
        } catch (IOException e) {
            logger.error("Error al leer archivo de proveedores: " + e.getMessage(), e);
        }
    }

    private void loadProducts() {
        logger.info("Cargando productos...");
        String filePath = DATA_PATH + "/producto.csv";
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] fields = line.split(";", -1);
                if (fields.length != 7) {
                    logger.warn("Línea de producto con formato incorrecto: " + line);
                    continue;
                }

                try {
                    Product product = new Product(
                        fields[0].trim(),
                        fields[1].trim(),
                        fields[2].trim(),
                        fields[3].trim(),
                        Double.parseDouble(fields[4].trim()),
                        Integer.parseInt(fields[5].trim()),
                        Integer.parseInt(fields[6].trim())
                    );

                    productService.insert(product);
                    count++;
                } catch (Exception e) {
                    logger.error("Error al procesar producto: " + line, e);
                }
            }
            logger.info("Productos cargados: " + count);
            
        } catch (IOException e) {
            logger.error("Error al leer archivo de productos: " + e.getMessage(), e);
        }
    }

    private void loadPhones() {
        logger.info("Cargando teléfonos...");
        String filePath = DATA_PATH + "/telefono.csv";

        Map<String, List<Phone>> phonesBySupplier = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] fields = line.split(";", -1);
                if (fields.length != 4) {
                    logger.warn("Línea de teléfono con formato incorrecto: " + line);
                    continue;
                }

                try {
                    String supplierId = fields[0].trim();
                    Phone phone = new Phone(
                        supplierId,
                        fields[1].trim(),
                        fields[2].trim(),
                        fields[3].trim()
                    );

                    phonesBySupplier.computeIfAbsent(supplierId, k -> new ArrayList<>()).add(phone);
                } catch (Exception e) {
                    logger.error("Error al procesar teléfono: " + line, e);
                }
            }

            int count = 0;
            for (Map.Entry<String, List<Phone>> entry : phonesBySupplier.entrySet()) {
                try {
                    Supplier supplier = supplierService.findById(entry.getKey());
                    if (supplier != null) {
                        Supplier updatedSupplier = new Supplier(
                            supplier.id(),
                            supplier.taxId(),
                            supplier.companyName(),
                            supplier.companyType(),
                            supplier.address(),
                            supplier.active(),
                            supplier.authorized(),
                            entry.getValue()
                        );
                        supplierService.update(entry.getKey(), updatedSupplier);
                        count++;
                    }
                } catch (Exception e) {
                    logger.error("Error al actualizar proveedor con teléfonos: " + entry.getKey(), e);
                }
            }
            logger.info("Proveedores actualizados con teléfonos: " + count);
            
        } catch (IOException e) {
            logger.error("Error al leer archivo de teléfonos: " + e.getMessage(), e);
        }
    }

    private void loadOrders() {
        logger.info("Cargando órdenes...");
        
        // Primero cargar los detalles de órdenes
        Map<String, List<OrderDetail>> orderDetailsMap = loadOrderDetails();
        
        // Luego cargar las órdenes con sus detalles embebido
        String filePath = DATA_PATH + "/op.csv";
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] fields = line.split(";", -1);
                if (fields.length != 5) {
                    logger.warn("Línea de orden con formato incorrecto: " + line);
                    continue;
                }

                try {
                    String orderId = fields[0].trim();
                    String date = fields[2].trim();
                    
                    // Obtener los detalles de esta orden
                    List<OrderDetail> orderDetails = orderDetailsMap.getOrDefault(orderId, new ArrayList<>());
                    
                    Order order = new Order(
                        orderId,
                        fields[1].trim(),
                        date,
                        Double.parseDouble(fields[3].trim()),
                        Double.parseDouble(fields[4].trim()),
                        orderDetails
                    );

                    orderService.insert(order);
                    count++;
                } catch (Exception e) {
                    logger.error("Error al procesar orden: " + line, e);
                }
            }
            logger.info("Órdenes cargadas: " + count);
            
        } catch (IOException e) {
            logger.error("Error al leer archivo de órdenes: " + e.getMessage(), e);
        }
    }

    private Map<String, List<OrderDetail>> loadOrderDetails() {
        logger.info("Cargando detalles de órdenes...");
        String filePath = DATA_PATH + "/detalle_op.csv";
        Map<String, List<OrderDetail>> orderDetailsMap = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] fields = line.split(";", -1);
                if (fields.length != 4) {
                    logger.warn("Línea de detalle de orden con formato incorrecto: " + line);
                    continue;
                }

                try {
                    String orderId = fields[0].trim();
                    OrderDetail orderDetail = new OrderDetail(
                        orderId,
                        fields[1].trim(),
                        Integer.parseInt(fields[2].trim()),
                        Double.parseDouble(fields[3].trim())
                    );

                    orderDetailsMap.computeIfAbsent(orderId, k -> new ArrayList<>()).add(orderDetail);
                    count++;
                } catch (Exception e) {
                    logger.error("Error al procesar detalle de orden: " + line, e);
                }
            }
            logger.info("Detalles de órdenes cargados: " + count);
            
        } catch (IOException e) {
            logger.error("Error al leer archivo de detalles de órdenes: " + e.getMessage(), e);
        }
        
        return orderDetailsMap;
    }

    public static void main(String[] args) {
        CSVLoader loader = new CSVLoader();
        loader.loadAllData();
    }
} 