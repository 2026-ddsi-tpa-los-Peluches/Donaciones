package ar.edu.utn.dds.k3003.componentes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
// ¡Acordate de importar el Map y HashMap!
import java.util.HashMap;
import java.util.Map;

public class LogisticaClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public LogisticaClient(@Value("${LOGISTICA_SERVICE_URL:http://localhost:8081}") String baseUrl) {

        System.out.println("LOGISTICA URL = " + baseUrl);

        this.baseUrl = baseUrl;
    }

    public void gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad) {
        try {
            // 1. Armamos la URL igual que en el Swagger: /depositos/{id}/donacion
            String url = baseUrl + "/depositos/" + depositoID + "/donacion";

            // 2. Armamos el body con el JSON que pide la documentación
            Map<String, Object> body = new HashMap<>();
            body.put("depositoID", depositoID);
            body.put("donacionID", donacionID);
            body.put("productoID", productoID);
            body.put("cantidad", cantidad);

            // 3. Hacemos el POST directo. Usamos Void.class porque el endpoint
            // no devuelve un body que nos interese mapear (suele devolver 200 OK o 201).
            restTemplate.postForEntity(url, body, Void.class);

        } catch (Exception e) {
            throw new RuntimeException("Error de comunicación al gestionar la donación en Logística", e);
        }
    }
}