package main

import (
	"bytes"
	"encoding/xml"
	"fmt"
	"io"
	"net/http"
	"strings"
)

// Estructuras para mapear la respuesta XML del servicio SOAP
type SoapResponse struct {
	XMLName xml.Name `xml:"Envelope"`
	Body    SoapBody `xml:"Body"`
}

type SoapBody struct {
	NumberToWordsResponse NumberToWordsResponse `xml:"NumberToWordsResponse"`
}

type NumberToWordsResponse struct {
	NumberToWordsResult string `xml:"NumberToWordsResult"`
}

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		numero := r.URL.Query().Get("n")
		if numero == "" {
			fmt.Fprint(w, "Por favor, proporciona un numero valido en la URL. Ejemplo: http://localhost:5000/?n=10")
			return
		}

		// Crear el cuerpo XML para la peticion SOAP
		soapEnvelope := fmt.Sprintf(`<?xml version="1.0" encoding="utf-8"?>
		<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
		  <soap:Body>
			<NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
			  <ubiNum>%s</ubiNum>
			</NumberToWords>
		  </soap:Body>
		</soap:Envelope>`, numero)

		// Hacer la peticion HTTP POST al servicio SOAP
		req, err := http.NewRequest("POST", "https://www.dataaccess.com/webservicesserver/NumberConversion.wso", bytes.NewBufferString(soapEnvelope))
		if err != nil {
			http.Error(w, "Error al crear la peticion: "+err.Error(), http.StatusInternalServerError)
			return
		}
		req.Header.Set("Content-Type", "text/xml; charset=utf-8")

		client := &http.Client{}
		resp, err := client.Do(req)
		if err != nil {
			http.Error(w, "Error al conectar con SOAP: "+err.Error(), http.StatusInternalServerError)
			return
		}
		defer resp.Body.Close()

		bodyBytes, _ := io.ReadAll(resp.Body)

		// Parsear el XML recibido
		var soapResp SoapResponse
		err = xml.Unmarshal(bodyBytes, &soapResp)
		if err != nil {
			http.Error(w, "Error al procesar XML: "+err.Error(), http.StatusInternalServerError)
			return
		}

		resultado := strings.ToLower(strings.TrimSpace(soapResp.Body.NumberToWordsResponse.NumberToWordsResult))
		fmt.Fprint(w, resultado)
	})

	fmt.Println("Servidor corriendo en http://localhost:5000")
	http.ListenAndServe(":5000", nil)
}