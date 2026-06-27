use actix_web::{web, App, HttpServer, Responder, HttpResponse};
use serde::Deserialize;

#[derive(Deserialize)]
struct QueryParams {
    n: Option<String>,
}

async fn index(query: web::Query<QueryParams>) -> impl Responder {
    let numero = match &query.n {
        Some(val) => val,
        None => return HttpResponse::Ok().body("Por favor, proporciona un numero valido en la URL. Ejemplo: http://localhost:5000/?n=10"),
    };

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

    match res {
        Ok(response) => {
            if let Ok(body_text) = response.text().await {
                // Buscamos de forma flexible ignorando posibles prefijos como 'm:' o 'soap:'
                if let Some(start_idx) = body_text.find("NumberToWordsResult>") {
                    let cut_start = &body_text[start_idx + 20..];
                    if let Some(end_idx) = cut_start.find("</") {
                        let resultado = &cut_start[..end_idx];
                        return HttpResponse::Ok().body(resultado.trim().to_lowercase());
                    }
                }
                HttpResponse::Ok().body("No se pudo parsear la respuesta SOAP.")
            } else {
                HttpResponse::InternalServerError().body("Error al leer el cuerpo de la respuesta SOAP.")
            }
        },
        Err(e) => HttpResponse::InternalServerError().body(format!("Error SOAP: {}", e)),
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