# TPO-BDII: Sistema de Gestión de Proveedores y Órdenes

## 📋 Descripción

Sistema de gestión de proveedores, productos y órdenes desarrollado como trabajo práctico para la materia Base de Datos II. La aplicación utiliza **MongoDB** como base de datos principal y **Redis** para caché, implementando una API REST con **Javalin**.

## 🏗️ Arquitectura

- **Backend**: Java 17 + Javalin (Framework web)
- **Base de Datos**: MongoDB 7.0
- **Caché**: Redis 7.2
- **Build Tool**: Maven
- **Contenedores**: Docker Compose

## 🚀 Instalación Rápida

### Prerrequisitos
- Docker y Docker Compose
- Java 17
- Maven

### Script
```bash
# Hacer ejecutable y ejecutar
chmod +x start-app.sh
./start-app.sh
```

### GitHub Codespaces
```bash
# Para Codespaces usar el script optimizado
./codespaces-start.sh
```

### Verificar funcionamiento
```bash
curl http://localhost:7000/ping
```

## 📊 Carga de Datos

### Endpoint de Carga Automática
La aplicación incluye un endpoint que carga automáticamente todos los datos desde los archivos CSV ubicados en la carpeta `data/`.

```bash
curl http://localhost:7000/load-data
```

### Archivos CSV disponibles
- `data/proveedor.csv` - Datos de proveedores
- `data/producto.csv` - Datos de productos  
- `data/op.csv` - Órdenes
- `data/detalle_op.csv` - Detalles de órdenes
- `data/telefono.csv` - Teléfonos de proveedores


## 🔗 Endpoints de la API

### Health Check
- `GET /ping` - Verificar estado del servicio

### 📦 Proveedores (Suppliers)

- `GET /suppliers/active/phones` - **Ejercicio 1**: Proveedores activos con caché Redis
- `GET /suppliers/tech/phones` - **Ejercicio 2**: Proveedores con "Tecno" en el nombre y sus teléfonos
- `GET /suppliers/phones` - **Ejercicio 3**: Proveedores con cada teléfono por separado
- `GET /suppliers/with-orders` - **Ejercicio 4**: Proveedores que tienen órdenes registradas
- `GET /suppliers/without-orders` - **Ejercicio 5**: Proveedores sin órdenes
- `GET /suppliers/with-orders-summary` - **Ejercicio 6**: Proveedores con resumen de órdenes (totales con/sin impuestos)
- `GET /suppliers/active-unauthorized` - **Ejercicio 12**: Proveedores activos pero no autorizados

#### CRUD Básico
- `GET /suppliers` - Obtener todos los proveedores
- `GET /suppliers/{id}` - Obtener proveedor por ID
- `POST /suppliers` - Crear nuevo proveedor
- `PUT /suppliers/{id}` - Actualizar proveedor
- `DELETE /suppliers/{id}` - Eliminar proveedor

### 📋 Órdenes (Orders)

- `GET /orders/by-supplier-tax-id` - **Ejercicio 7**: Órdenes por taxId del proveedor
- `GET /orders/with-coto-products` - **Ejercicio 8**: Órdenes que contienen productos de marca COTO
- `GET /orders/detailed-summary` - **Ejercicio 10**: Órdenes ordenadas por fecha (totales con/sin impuestos)

#### CRUD Básico
- `GET /orders` - Obtener todas las órdenes
- `GET /orders/{id}` - Obtener orden por ID
- `POST /orders` - Crear nueva orden
- `PUT /orders/{id}` - Actualizar orden
- `DELETE /orders/{id}` - Eliminar orden

### 🛍️ Productos (Products)

#### Endpoints Específicos
- `GET /products/with-orders` - **Ejercicio 8**: Productos con al menos una orden
- `GET /products/without-orders` - **Ejercicio 11**: Productos que nunca fueron ordenados

#### CRUD Básico
- `GET /products` - Obtener todos los productos
- `GET /products/{id}` - Obtener producto por ID
- `POST /products` - Crear nuevo producto
- `PUT /products/{id}` - Actualizar producto
- `DELETE /products/{id}` - Eliminar producto

## 💾 Estrategia de Caché con Redis

### Claves Utilizadas
```java
// Proveedores
SUPPLIERS_ACTIVE = "suppliers:active"
SUPPLIERS_INACTIVE = "suppliers:inactive" 
SUPPLIERS_AUTHORIZED = "suppliers:authorized"
SUPPLIERS_UNAUTHORIZED = "suppliers:unauthorized"

// Productos
ORDERED_PRODUCTS = "ordered:products"
```


## 📝 Ejemplos de Uso

### Crear un proveedor
```bash
curl -X POST http://localhost:7000/suppliers \
  -H "Content-Type: application/json" \
  -d '{
    "id": "1",
    "taxId": "30660608172",
    "companyName": "Arcos Plateados",
    "companyType": "SA",
    "address": "Pelliza 4234, Olivos, BA",
    "active": false,
    "authorized": false,
    "phones": []
  }'
```

### Obtener proveedores activos (con caché)
```bash
curl http://localhost:7000/suppliers/active/phones
```

### Obtener productos con al menos una orden
```bash
curl http://localhost:7000/products/with-orders
```

## 🔧 Configuración

### Variables de Entorno
- **MongoDB**: `mongodb://root:example@localhost:27017/?authSource=admin`
- **Redis**: `localhost:6379`
- **Puerto API**: `7000`

### Configuración de Caché
```java
// En SupplierService.java
private static final int CACHE_TTL = 30; // segundos
```
