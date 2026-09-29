package ar.edu.utn.dds.k3003.componentes;

import ar.edu.utn.dds.k3003.config.TraceIdInterceptor;
import ar.edu.utn.dds.k3003.controllers.DonadorRequest;
import ar.edu.utn.dds.k3003.model.Donador;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LogisticaClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public LogisticaClient(@Value("${LOGISTICA_SERVICE_URL}") String baseUrl) {

        log.info("LOGISTICA URL = {}", baseUrl);

        this.baseUrl = baseUrl;

        // Reenvía el traceId del request actual en el header X-Trace-Id en cada llamada saliente
        this.restTemplate.getInterceptors().add(new TraceIdInterceptor());
    }

    public void gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad) {
        try {
            String url = baseUrl + "/depositos/" + depositoID + "/donacion";

            // Creamos el objeto en una sola línea, limpio y tipado
            DonadorRequest request= new DonadorRequest(depositoID, donacionID, productoID, cantidad);

            log.info("Llamando a Logística: POST {} (donacionID={}, productoID={}, cantidad={})",
                    url, donacionID, productoID, cantidad);

            // RestTemplate se encarga solo de transformarlo a JSON
            restTemplate.postForEntity(url, request, Void.class);

            log.info("Logística aceptó la donación {}", donacionID);

        } catch (Exception e) {
            log.error("Error de comunicación con Logística al gestionar la donación {}", donacionID, e);
            throw new RuntimeException("Error de comunicación al gestionar la donación en Logística", e);
        }
    }
}
