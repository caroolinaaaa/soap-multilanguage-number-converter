package main

import (
	"bytes"
	"encoding/json"
	"encoding/xml"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
)

// Estructuras para el XML de SOAP
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

// Estructura para el JSON de MyMemory API
type TranslationResponse struct {
	ResponseData struct {
		TranslatedText string `json:"translatedText"`
	} `json:"responseData"`
}

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		numero := r.URL.Query().Get("n")
		if numero == "" {
			fmt.Fprint(w, "Por favor, proporciona un numero valido en la URL. Ejemplo: http://localhost:5000/?n=10")
			return
		}

		// 1. Petición SOAP (Inglés)
		soapEnvelope := fmt.Sprintf(`<?xml version="1.0" encoding="utf-8"?>
		<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
		  <soap:Body>
			<NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
			  <ubiNum>%s</ubiNum>
			</NumberToWords>
		  </soap:Body>
		</soap:Envelope>`, numero)

		req, err := http.NewRequest("POST", "https://www.dataaccess.com/webservicesserver/NumberConversion.wso", bytes.NewBufferString(soapEnvelope))
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		req.Header.Set("Content-Type", "text/xml; charset=utf-8")

		client := &http.Client{}
		resp, err := client.Do(req)
		if err != nil {
			http.Error(w, "Error SOAP: "+err.Error(), http.StatusInternalServerError)
			return
		}
		defer resp.Body.Close()

		bodyBytes, _ := io.ReadAll(resp.Body)
		var soapResp SoapResponse
		xml.Unmarshal(bodyBytes, &soapResp)
		resultadoIngles := strings.TrimSpace(soapResp.Body.NumberToWordsResponse.NumberToWordsResult)

		// 2. Traducción a Español vía API HTTP NATIVA
		apiURL := fmt.Sprintf("https://api.mymemory.translated.net/get?q=%s&langpair=en|es", url.QueryEscape(resultadoIngles))
		tradResp, err := http.Get(apiURL)
		if err != nil {
			http.Error(w, "Error Traduccion: "+err.Error(), http.StatusInternalServerError)
			return
		}
		defer tradResp.Body.Close()

		tradBytes, _ := io.ReadAll(tradResp.Body)
		var jsonResp TranslationResponse
		json.Unmarshal(tradBytes, &jsonResp)

		resultadoEspanol := strings.ToLower(strings.TrimSpace(jsonResp.ResponseData.TranslatedText))
		fmt.Fprint(w, resultadoEspanol)
	})

	fmt.Println("Servidor corriendo en http://localhost:5000")
	http.ListenAndServe(":5000", nil)
}