package main

import (
	"fmt"
	"net/http"
	"strconv"
	"strings"

	ntw "moul.io/number-to-words" // Importamos con el alias 'ntw'
)

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		numeroStr := r.URL.Query().Get("n")
		if numeroStr == "" {
			fmt.Fprint(w, "Por favor, proporciona un numero valido en la URL. Ejemplo: http://localhost:5000/?n=10")
			return
		}

		numero, err := strconv.Atoi(numeroStr)
		if err != nil {
			fmt.Fprint(w, "Por favor, ingresa un numero entero valido.")
			return
		}

		// Usamos la función exacta para español de España/Latinoamérica
		resultado := ntw.IntegerToEsEs(numero)
		
		fmt.Fprint(w, strings.ToLower(resultado))
	})

	fmt.Println("Servidor corriendo en http://localhost:5000")
	http.ListenAndServe(":5000", nil)
}
