#!/bin/bash

# Auto-fix permisos
chmod +x "$0" 2>/dev/null

echo "🚀 Iniciando TPO-BDII en GitHub Codespaces..."

# Verificar que estamos en codespaces
if [ -z "$CODESPACE_NAME" ]; then
    echo "⚠️  No estás en Codespaces, usa: ./start-app.sh"
    exit 1
fi

# Esperar a que Docker esté listo
echo "⏳ Esperando Docker..."
sleep 5

# Verificar Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no disponible. Reinicia el Codespace."
    exit 1
fi

# Iniciar servicios Docker
echo "🐳 Iniciando MongoDB y Redis..."
docker compose down 2>/dev/null || true
docker compose up -d

# Esperar servicios
echo "⏳ Esperando que MongoDB y Redis estén listos..."
sleep 20

# Compilar proyecto
echo "🔨 Compilando proyecto Java..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Error en compilación. Verifica el código."
    exit 1
fi

echo "✅ Compilación exitosa"

# Ejecutar aplicación
echo "🚀 Iniciando aplicación..."
echo "📡 API disponible en: https://${CODESPACE_NAME}-7000.${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN}"
echo "🔗 Endpoints: /ping, /load-data, /suppliers, /orders, /products"
echo ""

mvn exec:java -Dexec.mainClass="ar.edu.itba.bd.Main" -q 