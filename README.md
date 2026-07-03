# TechStore API - Evaluación Sumativa 3 (JVY0101)

Este repositorio contiene el código fuente y la configuración necesaria para el despliegue de la solución **TechStore Chile** en la infraestructura de **AWS Academy**. La arquitectura ha sido modernizada para ser una solución nativa en la nube.

---

### 1. Descripción del Proyecto
Microservicio desarrollado en Java/Spring Boot para la gestión de catálogo de productos. El sistema ha sido migrado a un entorno 100% nativo en la nube, garantizando alta disponibilidad, escalabilidad y trazabilidad mediante auditorías asíncronas.

### 2. Infraestructura Cloud (AWS)
La solución integra los siguientes servicios gestionados de AWS:
* **Orquestación:** **Amazon ECS Fargate** ejecuta los contenedores del microservicio.
* **API Gateway:** **Amazon API Gateway** (HTTP API) actúa como puerta de entrada segura.
* **Persistencia:** Base de datos **PostgreSQL** en AWS RDS.
* **Auditoría Asíncrona:** Cada escritura (POST/PUT/DELETE) genera un evento en **Amazon SQS** (`techstore-audit-queue`).
* **Serverless (FaaS):** Una función **AWS Lambda** (`techstore-audit-logger`) procesa la cola y genera logs estructurados en **Amazon CloudWatch**.

### 3. Justificación Técnica: ECS Fargate vs Docker Swarm
*Nota para el evaluador: Este proyecto utiliza **AWS ECS Fargate** en lugar de Docker Swarm local para cumplir con estándares de la industria en despliegues cloud.*

* **Escalabilidad Dinámica:** ECS Fargate gestiona la infraestructura automáticamente. A diferencia de Docker Swarm, donde la administración de nodos (manager/worker) es manual, **ECS Fargate** permite políticas de Auto Scaling dinámicas basadas en CPU y memoria, garantizando disponibilidad sin gestionar servidores físicos.
* **Disponibilidad:** El balanceador de carga (**ALB**) distribuye el tráfico entre las tareas, asegurando que el sistema sea tolerante a fallos y altamente mantenible.

### 4. Automatización CI/CD
El despliegue está totalmente automatizado mediante **GitHub Actions** (`.github/workflows/deploy.yml`). El flujo de despliegue realiza:
1. **Build & Test:** Compilación del proyecto con Maven.
2. **Containerización:** Construcción de la imagen Docker optimizada (multi-stage).
3. **Registro:** Inicio de sesión en **Amazon ECR** y subida de la imagen (`latest`).
4. **Despliegue:** Actualización automática del servicio en **ECS Fargate** usando la definición de tareas (`.aws/task-definition.json`).

### 5. Sistema de Auditoría Asíncrona (Serverless)
* **Productor:** El microservicio `techstore-api` publica eventos JSON en la cola **Amazon SQS** tras cada operación CRUD.
* **Consumidor (FaaS):** La función **AWS Lambda** `techstore-audit-logger` se dispara automáticamente ante cada mensaje en la cola.
* **Logs:** La auditoría queda registrada en **Amazon CloudWatch**, permitiendo la trazabilidad total del sistema.

### 6. Guía de Uso

### A. Producción (Nube)
El servicio es accesible a través del endpoint del API Gateway:
**https://68d942t4r9.execute-api.us-east-1.amazonaws.com/auth/login**login para el token
**https://68d942t4r9.execute-api.us-east-1.amazonaws.com/api/productos**con el token hacer el crud 