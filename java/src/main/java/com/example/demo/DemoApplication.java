package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ibm.icu.text.RuleBasedNumberFormat;
import java.util.Locale;

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

        try {
            // 1. Validar y parsear el número
            long numLong = Long.parseLong(numero);

            // 2. Usar el formateador de reglas basado en el Locale de español (es)
            Locale localeEspañol = new Locale("es");
            RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(localeEspañol, RuleBasedNumberFormat.SPELLOUT);
            
            // 3. Convertir a letras de forma nativa local con la librería
            String resultado = formatter.format(numLong);

            return resultado.trim().toLowerCase();

        } catch (NumberFormatException e) {
            return "Por favor, ingresa un numero entero valido.";
        } catch (Exception e) {
            return "Error en la conversion local: " + e.getMessage();
        }
    }
}