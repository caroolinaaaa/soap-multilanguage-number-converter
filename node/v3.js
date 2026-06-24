const numero = parseInt(process.argv[2]);

const unidades = [
    "cero",
    "uno",
    "dos",
    "tres",
    "cuatro",
    "cinco",
    "seis",
    "siete",
    "ocho",
    "nueve",
    "diez",
    "once",
    "doce",
    "trece",
    "catorce",
    "quince",
    "dieciséis",
    "diecisiete",
    "dieciocho",
    "diecinueve",
    "veinte"
];

if (numero >= 0 && numero <= 20) {
    console.log(unidades[numero]);
} else {
    console.log("Número fuera del rango soportado");
}