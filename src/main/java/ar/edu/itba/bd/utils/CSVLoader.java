package ar.edu.itba.bd.utils;

import ar.edu.itba.bd.models.Order;
import ar.edu.itba.bd.models.OrderDetail;
import ar.edu.itba.bd.models.Phone;
import ar.edu.itba.bd.models.Product;
import ar.edu.itba.bd.models.Supplier;
import ar.edu.itba.bd.services.OrderService;
import ar.edu.itba.bd.services.ProductService;
import ar.edu.itba.bd.services.SupplierService;

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
        System.out.println("Iniciando carga de datos CSV...");
        
        try {
            loadSuppliers();
            loadProducts();
            loadPhones();
            loadOrders();
            
            System.out.println("Carga de datos completada exitosamente.");
            
        } catch (Exception e) {
            System.err.println("Error durante la carga de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSuppliers() {
        System.out.println("Cargando proveedores...");
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
                    System.err.println("Línea de proveedor con formato incorrecto: " + line);
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
                    System.err.println("Error al procesar proveedor: " + line);
                    e.printStackTrace();
                }
            }
            System.out.println("Proveedores cargados: " + count);
            
        } catch (IOException e) {
            System.err.println("Error al leer archivo de proveedores: " + e.getMessage());
        }
    }

    private void loadProducts() {
        System.out.println("Cargando productos...");
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
                    System.err.println("Línea de producto con formato incorrecto: " + line);
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
                    System.err.println("Error al procesar producto: " + line);
                    e.printStackTrace();
                }
            }
            System.out.println("Productos cargados: " + count);
            
        } catch (IOException e) {
            System.err.println("Error al leer archivo de productos: " + e.getMessage());
        }
    }

    private void loadPhones() {
        System.out.println("Cargando teléfonos...");
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
                    System.err.println("Línea de teléfono con formato incorrecto: " + line);
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
                    System.err.println("Error al procesar teléfono: " + line);
                    e.printStackTrace();
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
                    System.err.println("Error al actualizar proveedor con teléfonos: " + entry.getKey());
                    e.printStackTrace();
                }
            }
            System.out.println("Proveedores actualizados con teléfonos: " + count);
            
        } catch (IOException e) {
            System.err.println("Error al leer archivo de teléfonos: " + e.getMessage());
        }
    }

    private void loadOrders() {
        System.out.println("Cargando órdenes...");
        
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
                    System.err.println("Línea de orden con formato incorrecto: " + line);
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
                    System.err.println("Error al procesar orden: " + line);
                    e.printStackTrace();
                }
            }
            System.out.println("Órdenes cargadas: " + count);
            
        } catch (IOException e) {
            System.err.println("Error al leer archivo de órdenes: " + e.getMessage());
        }
    }

    private Map<String, List<OrderDetail>> loadOrderDetails() {
        System.out.println("Cargando detalles de órdenes...");
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
                    System.err.println("Línea de detalle de orden con formato incorrecto: " + line);
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
                    System.err.println("Error al procesar detalle de orden: " + line);
                    e.printStackTrace();
                }
            }
            System.out.println("Detalles de órdenes cargados: " + count);
            
        } catch (IOException e) {
            System.err.println("Error al leer archivo de detalles de órdenes: " + e.getMessage());
        }
        
        return orderDetailsMap;
    }

    public static void main(String[] args) {
        CSVLoader loader = new CSVLoader();
        loader.loadAllData();
    }
} 