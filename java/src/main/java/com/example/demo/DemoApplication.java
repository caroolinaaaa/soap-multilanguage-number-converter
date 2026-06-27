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
import java.util.HashMap;
import java.util.Map;

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

        // 1. Consumir el servicio web SOAP (Igual que v1)
        String soapEnvelope = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "  <soap:Body>" +
            "    <NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">" +
            "      <ubiNum>" + numero + "</ubiNum>" +
            "    </NumberToWords>" +
            "  </soap:Body>" +
            "</soap:Envelope>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(soapEnvelope, headers);
        RestTemplate restTemplate = new RestTemplate();
        String resultadoIngles = "";

        try {
            String urlSoap = "https://www.dataaccess.com/webservicesserver/NumberConversion.wso";
            String responseSoap = restTemplate.postForObject(urlSoap, entity, String.class);

            if (responseSoap != null && responseSoap.contains("NumberToWordsResult>")) {
                int start = responseSoap.indexOf("NumberToWordsResult>") + 20;
                int end = responseSoap.indexOf("</", start);
                resultadoIngles = responseSoap.substring(start, end).trim().toLowerCase();
            }
        } catch (Exception e) {
            return "Error al conectar con el servicio SOAP: " + e.getMessage();
        }

        if (resultadoIngles.isEmpty()) {
            return "No se pudo obtener el resultado en ingles.";
        }

        // 2. Convertir/Traducir usando el motor de traducción gramatical local
        return TraductorNumerico.traducir(resultadoIngles);
    }
}

// Motor de traducción local que respeta la gramática del español
class TraductorNumerico {
    private static final Map<String, String> mapa = new HashMap<>();

    static {
        mapa.put("zero", "cero"); mapa.put("one", "uno"); mapa.put("two", "dos");
        mapa.put("three", "tres"); mapa.put("four", "cuatro"); mapa.put("five", "cinco");
        mapa.put("six", "seis"); mapa.put("seven", "siete"); mapa.put("eight", "ocho");
        mapa.put("nine", "nueve"); mapa.put("ten", "diez"); mapa.put("eleven", "once");
        mapa.put("twelve", "doce"); mapa.put("thirteen", "trece"); mapa.put("fourteen", "catorce");
        mapa.put("fifteen", "quince"); mapa.put("sixteen", "dieciséis"); mapa.put("seventeen", "diecisiete");
        mapa.put("eighteen", "dieciocho"); mapa.put("nineteen", "diecinueve"); mapa.put("twenty", "veinte");
        mapa.put("thirty", "treinta"); mapa.put("forty", "cuarenta"); mapa.put("fifty", "cincuenta");
        mapa.put("sixty", "sesenta"); mapa.put("seventy", "setenta"); mapa.put("eighty", "ochenta");
        mapa.put("ninety", "noventa"); mapa.put("thousand", "mil");
    }

    public static String traducir(String ingles) {
        if (ingles.equals("one hundred")) return "cien";
        
        // Reemplazar conectores innecesarios del inglés "and" por espacios para procesar puro valor
        ingles = ingles.replace(" and ", " ");
        String[] palabras = ingles.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < palabras.length; i++) {
            String p = palabras[i];

            // Regla para las centenas: "one hundred" -> "ciento", "two hundred" -> "doscientos"
            if (p.equals("hundred")) {
                continue; // Se procesa junto con el número anterior
            }

            if (i < palabras.length - 1 && palabras[i + 1].equals("hundred")) {
                if (p.equals("one")) {
                    sb.append("ciento ");
                } else {
                    String cent = mapa.getOrDefault(p, p);
                    if (cent.equals("two")) sb.append("doscientos ");
                    else if (cent.equals("three")) sb.append("trescientos ");
                    else if (cent.equals("four")) sb.append("cuatrocientos ");
                    else if (cent.equals("five")) sb.append("quinientos ");
                    else if (cent.equals("six")) sb.append("seiscientos ");
                    else if (cent.equals("seven")) sb.append("setecientos ");
                    else if (cent.equals("eight")) sb.append("ochocientos ");
                    else if (cent.equals("nine")) sb.append("novecientos ");
                }
                i++; // Saltamos el "hundred" ya procesado
                continue;
            }

            // Regla para las decenas compuestas: "twenty" "two" -> "veintidós", "ninety" "two" -> "noventa y dos"
            if (mapa.containsKey(p)) {
                String esp = mapa.get(p);
                
                if (i < palabras.length - 1 && mapa.containsKey(palabras[i + 1]) && !palabras[i + 1].equals("thousand")) {
                    String sigEsp = mapa.get(palabras[i + 1]);
                    if (p.equals("twenty")) {
                        // Regla especial de los veintes (veintidós, veintitrés...)
                        if (sigEsp.equals("uno")) sb.append("veintiuno ");
                        else if (sigEsp.equals("dos")) sb.append("veintidós ");
                        else if (sigEsp.equals("tres")) sb.append("veintitrés ");
                        else if (sigEsp.equals("seis")) sb.append("veintiséis ");
                        else sb.append("veinti").append(sigEsp).append(" ");
                    } else {
                        // Decenas normales: cuarenta y dos, noventa y dos
                        sb.append(esp).append(" y ").append(sigEsp).append(" ");
                    }
                    i++; // Saltamos la unidad ya combinada
                } else {
                    sb.append(esp).append(" ");
                }
            }
        }

        return sb.toString().trim();
    }
}