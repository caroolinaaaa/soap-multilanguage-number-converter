using Microsoft.AspNetCore.Mvc;
using Humanizer; // Cargamos la librería Humanizer
using System.Globalization;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.MapGet("/", (HttpContext context, [FromQuery] string? n) =>
{
    // Validamos que el parámetro exista y sea un número entero
    if (string.IsNullOrEmpty(n) || !long.TryParse(n, out long number))
    {
        return "Por favor, proporciona un número válido en la URL. Ejemplo: http://localhost:5000/?n=10";
    }

    try
    {
        // Forzamos la cultura a español para que Humanizer convierta al idioma correcto
        var culturaEspanol = new CultureInfo("es");
        
        // Convertimos el número a letras usando la extensión .ToWords()
        string resultadoLetras = number.ToWords(culturaEspanol);

        // Retornamos el resultado en minúsculas
        return resultadoLetras.ToLower();
    }
    catch (Exception ex)
    {
        return $"Error en la conversión: {ex.Message}";
    }
});

app.Run();