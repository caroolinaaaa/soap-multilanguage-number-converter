use actix_web::{web, App, HttpServer, Responder, HttpResponse};
use serde::Deserialize;

#[derive(Deserialize)]
struct QueryParams {
    n: Option<String>,
}

// Estructuras para deserializar la respuesta JSON de la API de traducción
#[derive(Deserialize)]
struct TranslationResponse {
    #[serde(rename = "responseData")]
    response_data: ResponseData,
}

#[derive(Deserialize)]
struct ResponseData {
    #[serde(rename = "translatedText")]
    translated_text: String,
}

async fn index(query: web::Query<QueryParams>) -> impl Responder {
    let numero = match &query.n {
        Some(val) => val,
        None => return HttpResponse::Ok().body("Por favor, proporciona un numero valido en la URL. Ejemplo: http://localhost:5000/?n=10"),
    };

    // 1. Petición SOAP para obtener el número en inglés
    let soap_envelope = format!(
        r#"<?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
              <ubiNum>{}</ubiNum>
            </NumberToWords>
          </soap:Body>
        </soap:Envelope>"#,
        numero
    );

    let client = reqwest::Client::new();
    let res = client.post("https://www.dataaccess.com/webservicesserver/NumberConversion.wso")
        .header("Content-Type", "text/xml; charset=utf-8")
        .body(soap_envelope)
        .send()
        .await;

    let mut resultado_ingles = String::new();

    match res {
        Ok(response) => {
            if let Ok(body_text) = response.text().await {
                if let Some(start_idx) = body_text.find("NumberToWordsResult>") {
                    let cut_start = &body_text[start_idx + 20..];
                    if let Some(end_idx) = cut_start.find("</") {
                        resultado_ingles = cut_start[..end_idx].trim().to_string();
                    }
                }
            }
        },
        Err(e) => return HttpResponse::InternalServerError().body(format!("Error SOAP: {}", e)),
    }

    if resultado_ingles.is_empty() {
        return HttpResponse::Ok().body("No se pudo obtener o parsear el resultado en ingles.");
    }

    // 2. Petición HTTP a la API de MyMemory para traducir de Inglés a Español
    let api_url = format!(
        "https://api.mymemory.translated.net/get?q={}&langpair=en|es",
        resultado_ingles
    );

    match client.get(&api_url).send().await {
        Ok(response) => {
            if let Ok(json_resp) = response.json::<TranslationResponse>().await {
                let resultado_espanol = json_resp.response_data.translated_text.trim().to_lowercase();
                HttpResponse::Ok().body(resultado_espanol)
            } else {
                HttpResponse::InternalServerError().body("Error al procesar el JSON de la traduccion.")
            }
        },
        Err(e) => HttpResponse::InternalServerError().body(format!("Error en el servicio de traduccion: {}", e)),
    }
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    println!("Servidor Rust corriendo en http://localhost:5000");
    HttpServer::new(|| {
        App::new().route("/", web::get().to(index))
    })
    .bind("127.0.0.1:5000")?
    .run()
    .await
}