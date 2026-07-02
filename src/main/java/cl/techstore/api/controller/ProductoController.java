package cl.techstore.api.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import cl.techstore.api.dto.ProductoDTO;
import cl.techstore.api.model.Producto;
import cl.techstore.api.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // Inyectamos el cliente SQS para AWS
    @Autowired
    private SqsClient sqsClient;


    private final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/994995081324/techstore-audit-queue";

    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) {
        Producto producto = productoService.getProductoById(id);
        if (producto == null) {
            return ResponseEntity.notFound().build(); 
        }
        return ResponseEntity.ok(producto); 
    }
    
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody ProductoDTO dto) {
        // 1. Crear el producto en la BD
        Producto producto = productoService.crear(dto);
        
        // 2. Enviar evento de auditoría asíncrono a SQS
        enviarAuditoria("CREAR", producto.getId(), producto.getNombre());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> modificar(@PathVariable Long id, @RequestBody ProductoDTO dto) {
        // 1. Modificar producto en la BD
        Producto producto = productoService.modificar(id, dto);
        
        // 2. Enviar evento de auditoría asíncrono a SQS
        enviarAuditoria("MODIFICAR", producto.getId(), producto.getNombre());
        
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        // Obtenemos el producto antes de eliminarlo para extraer sus datos para la auditoría
        Producto producto = productoService.getProductoById(id);
        
        if (producto != null) {
            // 1. Borrado lógico en la BD
            productoService.eliminar(id);
            
            // 2. Enviar evento de auditoría asíncrono a SQS
            enviarAuditoria("ELIMINAR", producto.getId(), producto.getNombre());
            
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Método auxiliar para extraer al usuario autenticado, 
     * construir el JSON y enviarlo a AWS SQS.
     */
    private void enviarAuditoria(String accion, Long productoId, String nombreProducto) {
        try {
            // Obtener el correo (username) del usuario autenticado desde el JWT
            String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // Generar timestamp ISO 8601
            String fecha = Instant.now().toString();

            // Construir el JSON manualmente
            String mensajeJson = String.format(
                "{\"accion\": \"%s\", \"productoId\": %d, \"nombre\": \"%s\", \"usuario\": \"%s\", \"fecha\": \"%s\"}",
                accion, productoId, nombreProducto, usuario, fecha
            );

            // Preparar y enviar mensaje a Amazon SQS
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(QUEUE_URL)
                .messageBody(mensajeJson)
                .build();
                
            sqsClient.sendMessage(sendMsgRequest);
            System.out.println("[AUDITORIA] Mensaje enviado a SQS exitosamente: " + mensajeJson);

        } catch (Exception e) {
            // Un bloque try-catch asegura que si AWS SQS falla temporalmente, 
            // no se caiga la API completa y el usuario reciba la respuesta HTTP 200/201.
            System.err.println("[ERROR AWS SQS] No se pudo enviar el mensaje: " + e.getMessage());
        }
    }
}