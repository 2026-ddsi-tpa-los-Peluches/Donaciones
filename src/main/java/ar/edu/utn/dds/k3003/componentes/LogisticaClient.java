package ar.edu.utn.dds.k3003.componentes;

import ar.edu.utn.dds.k3003.controllers.DonadorRequest;
import ar.edu.utn.dds.k3003.model.Donador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class LogisticaClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public LogisticaClient(@Value("${LOGISTICA_SERVICE_URL:http://localhost:8081}") String baseUrl) {

        System.out.println("LOGISTICA URL = " + baseUrl);

        this.baseUrl = baseUrl;
    }

    public void gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad) {
        try {
            String url = baseUrl + "/depositos/" + depositoID + "/donacion";

            // Creamos el objeto en una sola línea, limpio y tipado
            DonadorRequest request= new DonadorRequest(depositoID, donacionID, productoID, cantidad);

            // RestTemplate se encarga solo de transformarlo a JSON
            restTemplate.postForEntity(url, request, Void.class);

        } catch (Exception e) {
            throw new RuntimeException("Error de comunicación al gestionar la donación en Logística", e);
        }
    }
}