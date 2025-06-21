#!/bin/bash

echo "🚀 Iniciando TPO-BDII en GitHub Codespaces..."

# Verificar que Docker esté disponible
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está disponible. Verificando instalación..."
    exit 1
fi

# Verificar que Docker Compose esté disponible
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose no está disponible. Verificando instalación..."
    exit 1
fi

echo "✅ Docker y Docker Compose están disponibles"

# Limpiar contenedores anteriores si existen
echo "🧹 Limpiando contenedores anteriores..."
docker compose down 2>/dev/null || true

# Levantar servicios Docker
echo "🐳 Levantando servicios Docker (MongoDB y Redis)..."
docker compose up -d

# Esperar a que los servicios estén listos
echo "⏳ Esperando a que los servicios estén listos..."
sleep 10

# Verificar que MongoDB esté disponible
echo "🔍 Verificando conexión a MongoDB..."
until docker exec mongo mongosh --eval "db.runCommand('ping')" --quiet > /dev/null 2>&1; do
    echo "⏳ MongoDB aún no está listo, esperando..."
    sleep 2
done
echo "✅ MongoDB está listo"

# Verificar que Redis esté disponible
echo "🔍 Verificando conexión a Redis..."
until docker exec redis redis-cli ping > /dev/null 2>&1; do
    echo "⏳ Redis aún no está listo, esperando..."
    sleep 2
done
echo "✅ Redis está listo"

# Compilar el proyecto
echo "🔨 Compilando el proyecto..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "❌ Error en la compilación"
    exit 1
fi

echo "✅ Compilación exitosa"

# Ejecutar la aplicación
echo "🚀 Iniciando la aplicación..."
echo "📡 La API estará disponible en:"
echo "   - Local: http://localhost:7000"
echo "   - Codespace: https://${CODESPACE_NAME}-7000.${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN}"
echo ""
echo "🔗 Endpoints principales:"
echo "   - GET /ping - Health check"
echo "   - GET /load-data - Cargar datos desde CSV"
echo "   - Ver README.md para más endpoints"
echo ""

mvn exec:java -Dexec.mainClass="ar.edu.itba.bd.Main" 