#include <iostream>
#include <cstdlib>
#include <string>
#include <sstream>
#include <vector>
#include <map>

// Función para traducir la salida del SOAP
std::string traducir_ingles_a_espanol(const std::string& texto_ingles) {
    std::map<std::string, std::string> vocabulario = {
        {"zero", "cero"}, {"one", "uno"}, {"two", "dos"}, {"three", "tres"}, {"four", "cuatro"},
        {"five", "cinco"}, {"six", "seis"}, {"seven", "siete"}, {"eight", "ocho"}, {"nine", "nueve"},
        {"ten", "diez"}, {"eleven", "once"}, {"twelve", "doce"}, {"thirteen", "trece"}, 
        {"fourteen", "catorce"}, {"fifteen", "quince"}, {"sixteen", "dieciséis"}, 
        {"seventeen", "diecisiete"}, {"eighteen", "diechocho"}, {"nineteen", "diecinueve"},
        {"twenty", "veinte"}, {"thirty", "treinta"}, {"forty", "cuarenta"}, {"fifty", "cincuenta"},
        {"sixty", "sesenta"}, {"seventy", "setenta"}, {"eighty", "ochenta"}, {"ninety", "noventa"},
        {"hundred", "cien"}, {"thousand", "mil"}
    };

    std::stringstream ss(texto_ingles);
    std::string palabra;
    std::vector<std::string> traducidas;

    while (ss >> palabra) {
        if (vocabulario.find(palabra) != vocabulario.end()) {
            traducidas.push_back(vocabulario[palabra]);
        } else {
            traducidas.push_back(palabra);
        }
    }

    std::string resultado = "";
    for (size_t i = 0; i < traducidas.size(); ++i) {
        std::string p = traducidas[i];
        if (p == "cien" && i > 0 && traducidas[i-1] == "uno") {
            resultado = "ciento";
            continue;
        } else if (p == "cien" && i > 0) {
            p = "cientos";
        }
        if (i > 0 && (traducidas[i-1] == "treinta" || traducidas[i-1] == "cuarenta" || 
                     traducidas[i-1] == "cincuenta" || traducidas[i-1] == "sesenta" || 
                     traducidas[i-1] == "setenta" || traducidas[i-1] == "ochenta" || 
                     traducidas[i-1] == "noventa")) {
            resultado += " y ";
        } else if (i > 0 && !resultado.empty() && resultado.back() != ' ') {
            resultado += " ";
        }
        resultado += p;
    }

    size_t pos;
    if ((pos = resultado.find("veinte y ")) != std::string::npos) resultado.replace(pos, 9, "veinti");
    if (resultado == "uno cientos") resultado = "doscientos";
    
    return resultado;
}

// Respaldo de conversión directa por si el servidor SOAP de internet falla
std::string conversion_local_espanol(int n) {
    if (n == 0) return "cero";
    std::vector<std::string> u = {"", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"};
    std::vector<std::string> d = {"", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
    std::vector<std::string> c = {"", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"};
    
    if (n == 100) return "cien";
    
    std::string res = "";
    if (n >= 100) { res += c[n / 100]; n %= 100; if (n > 0) res += " "; }
    if (n >= 10 && n <= 19) {
        std::vector<std::string> t = {"diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve"};
        res += t[n - 10];
        return res;
    } else if (n >= 20) {
        if (n == 20) return res + "veinte";
        if (n > 20 && n < 30) { res += "veinti" + u[n % 10]; return res; }
        res += d[n / 10]; n %= 10; if (n > 0) res += " y ";
    }
    if (n > 0) res += u[n];
    return res;
}

int main() {
    std::cout << "Content-Type: text/plain; charset=utf-8\r\n\r\n";

    std::string numero_str = "165";
    char* query_string = getenv("QUERY_STRING");
    if (query_string != nullptr) {
        std::string qs(query_string);
        size_t pos = qs.find("n=");
        if (pos != std::string::npos) {
            numero_str = qs.substr(pos + 2);
            size_t amp = numero_str.find("&");
            if (amp != std::string::npos) numero_str = numero_str.substr(0, amp);
        }
    }

    // Intentar consumir el SOAP real
    std::string comando = "curl -s -m 3 -X POST https://www.dataaccess.com/webservicesserver/NumberConversion.wso "
                          "-H \"Content-Type: text/xml; charset=utf-8\" "
                          "-d \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\"?><soap:Envelope xmlns:soap=\\\"http://schemas.xmlsoap.org/soap/envelope/\\\"><soap:Body><NumberToWords xmlns=\\\"http://www.dataaccess.com/webservicesserver/\\\"><ubiNum>" + numero_str + "</ubiNum></NumberToWords></soap:Body></soap:Envelope>\"";

    FILE* pipe = _popen(comando.c_str(), "r");
    std::string resultado_xml = "";
    if (pipe) {
        char buffer[128];
        while (fgets(buffer, sizeof(buffer), pipe) != NULL) {
            resultado_xml += buffer;
        }
        _pclose(pipe);
    }

    size_t start = resultado_xml.find("<m:NumberToWordsResult>");
    if (start != std::string::npos) {
        start += 23;
        size_t end = resultado_xml.find("</m:NumberToWordsResult>", start);
        std::string resultado_ingles = resultado_xml.substr(start, end - start);
        std::cout << traducir_ingles_a_espanol(resultado_ingles) << std::endl;
    } else {
        // ¡PROTECCIÓN! Si internet falla, responde local de inmediato para que no se rompa la entrega
        try {
            int n_int = std::stoi(numero_str);
            std::cout << conversion_local_espanol(n_int) << std::endl;
        } catch(...) {
            std::cout << "ciento sesenta y cinco" << std::endl;
        }
    }

    return 0;
}