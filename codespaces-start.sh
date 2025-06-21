#!/bin/bash

# Auto-fix permisos
chmod +x "$0" 2>/dev/null

echo "🚀 Iniciando TPO-BDII en GitHub Codespaces..."

# Verificar Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no disponible. Esperando inicialización..."
    sleep 10
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker aún no disponible. Reinicia el codespace."
        exit 1
    fi
fi

# Verificar servicio Docker
if ! docker info &> /dev/null; then
    echo "⏳ Iniciando servicio Docker..."
    sudo service docker start 2>/dev/null || true
    sleep 5
fi

# Limpiar contenedores anteriores
docker compose down 2>/dev/null || true

# Levantar servicios
echo "🐳 Iniciando MongoDB y Redis..."
docker compose up -d

# Esperar servicios
echo "⏳ Esperando servicios..."
sleep 15

# Compilar
echo "🔨 Compilando..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "❌ Error en compilación"
    exit 1
fi

# Ejecutar
echo "🚀 Ejecutando aplicación..."
echo "📡 API disponible en puerto 7000"
mvn exec:java -Dexec.mainClass="ar.edu.itba.bd.Main" 