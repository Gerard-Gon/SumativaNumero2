Comandos en terminal raiz del proyecto para probar en POSTMAN:

Generar el archivo ejecutable (.JAR): .\mvnw clean package -DskipTests
Construir imágenes y levantar contenedores: docker compose up --build
Si ya se levantaron imagenes y contenedores solo el siguiente comando : docker compose up
