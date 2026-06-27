use actix_web::{web, App, HttpServer, Responder, HttpResponse};
use serde::Deserialize;

#[derive(Deserialize)]
struct QueryParams {
    n: Option<String>,
}

// Función nativa local y limpia para convertir números a letras en español
fn numero_a_letras(mut n: i64) -> String {
    if n == 0 {
        return "cero".to_string();
    }

    let unidades = ["", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"];
    let decenas = ["", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"];
    let especiales = ["diez", "once", "doce", "trece", "catorce", "quince", "dieciseis", "diecisiete", "dieciocho", "diecinueve"];
    let veintes = ["veinte", "veintiuno", "veintidos", "veintitres", "veinticuatro", "veinticinco", "veintiseis", "veintisiete", "veintiocho", "veintinueve"];
    let centenas = ["", "cien", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"];

    let mut resultado = Vec::new();

    // Centenas
    if n >= 100 {
        let c = (n / 100) as usize;
        if c == 1 && n % 100 != 0 {
            resultado.push("ciento".to_string());
        } else {
            resultado.push(centenas[c].to_string());
        }
        n %= 100;
    }

    // Decenas y Unidades
    if n >= 10 && n < 20 {
        resultado.push(especiales[(n - 10) as usize].to_string());
    } else if n >= 20 && n < 30 {
        resultado.push(veintes[(n - 20) as usize].to_string());
    } else if n >= 30 {
        let d = (n / 10) as usize;
        let u = (n % 10) as usize;
        if u > 0 {
            resultado.push(format!("{} y {}", decenas[d], unidades[u]));
        } else {
            resultado.push(decenas[d].to_string());
        }
    } else if n > 0 {
        resultado.push(unidades[n as usize].to_string());
    }

    resultado.join(" ")
}

async fn index(query: web::Query<QueryParams>) -> impl Responder {
    let numero_str = match &query.n {
        Some(val) => val,
        None => return HttpResponse::Ok().body("Por favor, proporciona un numero valido en la URL. Ejemplo: http://localhost:5000/?n=10"),
    };

    let numero: i64 = match numero_str.parse() {
        Ok(num) => num,
        Err(_) => return HttpResponse::Ok().body("Por favor, ingresa un numero entero valido."),
    };

    // Restringimos el rango para el ejemplo rápido local
    if numero < 0 || numero > 999 {
        return HttpResponse::Ok().body("Por favor, ingresa un numero entre 0 y 999.");
    }

    let resultado = numero_a_letras(numero);
    HttpResponse::Ok().body(resultado)
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