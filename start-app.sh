#!/bin/bash

echo "🚀 Iniciando TPO-BDII..."

# Levantar servicios Docker
docker compose up -d

# Compilar y ejecutar
mvn clean compile
mvn exec:java -Dexec.mainClass="ar.edu.itba.bd.Main"