package ar.edu.utn.dds.k3003.componentes;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.config.TraceIdInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DonadoresYEntidadesClient {


    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public DonadoresYEntidadesClient(@Value("${DONADORESYENTIDADES_URL}") String baseUrl) {

        log.info("DONADORESYENTIDADES URL = {}", baseUrl);

        this.baseUrl = baseUrl;

        // Reenvía el traceId del request actual en el header X-Trace-Id en cada llamada saliente
        this.restTemplate.getInterceptors().add(new TraceIdInterceptor());
    }

    public QuejaDTO agregarQueja(QuejaDTO quejaDTO) {
        try {
            // La ruta es /donadores/{id}/quejas según tu DonadorController
            String url = baseUrl + "/donadores/" + quejaDTO.donadorID() + "/quejas";

            // Armamos el body tal cual lo espera el endpoint
            Map<String, String> requestBody = Map.of(
                    "donacionID", quejaDTO.donacionID(),
                    "descripcion", quejaDTO.descripcion()
            );

            log.info("Llamando a DonadoresYEntidades: POST {} (donacionID={})", url, quejaDTO.donacionID());

            // Hacemos el POST y mapeamos la respuesta al DTO
            ResponseEntity<QuejaDTO> response = restTemplate.postForEntity(url, requestBody, QuejaDTO.class);

            log.info("DonadoresYEntidades respondió status={} al agregar queja", response.getStatusCode());

            return response.getBody();

        } catch (HttpServerErrorException e) {
            log.error("Error en DonadoresYEntidades al agregar queja: status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error en el microservicio de Donadores: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Error de comunicación con DonadoresYEntidades al agregar queja", e);
            throw new RuntimeException("Error de comunicación al intentar agregar la queja", e);
        }
    }

    public Boolean puedeDonar(String donadorID) {

        try {
            String url = baseUrl + "/donadores/" + donadorID + "/puede-donar";

            log.info("Llamando a DonadoresYEntidades: GET {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            // Extraemos el valor como un Object genérico
            Object valorPuedeDonar = body.get("puedeDonar");

            // Convertimos a String de forma segura y luego lo parseamos a Boolean
            // Esto maneja perfecto casos donde venga "true" (String) o true (Boolean)
            Boolean puedeDonar = Boolean.parseBoolean(String.valueOf(valorPuedeDonar));

            log.info("DonadoresYEntidades respondió: donador {} puedeDonar={}", donadorID, puedeDonar);

            return puedeDonar;

        } catch (HttpServerErrorException e) {
            log.warn("DonadoresYEntidades devolvió error al consultar donador {}: status={} body={}",
                    donadorID, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    e.getResponseBodyAsString(),
                    e
            );

        } catch (Exception e) {
            log.error("Error de comunicación con DonadoresYEntidades al consultar donador {}", donadorID, e);
            throw new RuntimeException(
                    "Error al consultar si el donador puede donar",
                    e
            );
        }
    }

}
