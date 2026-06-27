package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/")
    public String index(@RequestParam(value = "n", required = false) String numero) {
        if (numero == null || numero.isEmpty()) {
            return "Por favor, proporciona un numero valido en la URL. Ejemplo: http://localhost:5000/?n=10";
        }

        // 1. Crear el sobre XML para SOAP
        String soapEnvelope = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "  <soap:Body>" +
            "    <NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">" +
            "      <ubiNum>" + numero + "</ubiNum>" +
            "    </NumberToWords>" +
            "  </soap:Body>" +
            "</soap:Envelope>";

        // 2. Configurar Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(soapEnvelope, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            // 3. Hacer la petición POST
            String url = "https://www.dataaccess.com/webservicesserver/NumberConversion.wso";
            String response = restTemplate.postForObject(url, entity, String.class);

            // 4. Parseo rápido por texto del resultado XML
            if (response != null && response.contains("NumberToWordsResult>")) {
                int start = response.indexOf("NumberToWordsResult>") + 20;
                int end = response.indexOf("</", start);
                return response.substring(start, end).trim().toLowerCase();
            }
            return "No se pudo parsear la respuesta SOAP.";

        } catch (Exception e) {
            return "Error al conectar con el servicio SOAP: " + e.getMessage();
        }
    }
}