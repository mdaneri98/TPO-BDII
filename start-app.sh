#!/bin/bash

# Levantar servicios Docker
docker-compose up -d

# Limpiar Redis y MongoDB
docker exec redis redis-cli FLUSHALL
docker exec mongo mongosh --eval "use tp2025; db.dropDatabase();"

# Compilar y empaquetar
mvn clean package -DskipTests

# Ejecutar aplicación
java -jar target/TPO-BDII-1.0-SNAPSHOT.jar 