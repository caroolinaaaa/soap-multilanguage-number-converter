#include <iostream>
#include <cstdlib>
#include <string>
#include <vector>

// Implementación nativa en el propio lenguaje (C++) para convertir a letras en español
std::string conversion_nativa_espanol(int n) {
    if (n == 0) return "cero";
    
    std::vector<std::string> u = {"", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"};
    std::vector<std::string> d = {"", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
    std::vector<std::string> c = {"", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"};
    
    if (n == 100) return "cien";
    
    std::string res = "";
    
    // Centenas
    if (n >= 100) { 
        res += c[n / 100]; 
        n %= 100; 
        if (n > 0) res += " "; 
    }
    
    // Decenas y unidades
    if (n >= 10 && n <= 19) {
        std::vector<std::string> t = {"diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve"};
        res += t[n - 10];
        return res;
    } else if (n >= 20) {
        if (n == 20) return res + "veinte";
        if (n > 20 && n < 30) { res += "veinti" + u[n % 10]; return res; }
        res += d[n / 10]; 
        n %= 10; 
        if (n > 0) res += " y ";
    }
    
    if (n > 0) res += u[n];
    return res;
}

int main() {
    // Cabecera HTTP obligatoria para aplicaciones Web CGI
    std::cout << "Content-Type: text/plain; charset=utf-8\r\n\r\n";

    // Leer el número proporcionado en la URL (?n=165)
    std::string numero_str = "1";
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

    // Convertir el número usando la lógica base del propio C++
    try {
        int numero_int = std::stoi(numero_str);
        if (numero_int < 0 || numero_int > 999) {
            std::cout << "Por favor, introduce un numero valido entre 0 y 999." << std::endl;
        } else {
            // Imprime directamente la conversión en español nativo
            std::cout << conversion_nativa_espanol(numero_int) << std::endl;
        }
    } catch(...) {
        std::cout << "Entrada invalida. Asegurate de pasar un numero entero en ?n=" << std::endl;
    }

    return 0;
}