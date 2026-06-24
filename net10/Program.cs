using Microsoft.AspNetCore.Mvc;
using ServiceReference;
using GTranslate.Translators; // Cargamos la librería de traducción

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.MapGet("/", async (HttpContext context, [FromQuery] string? n) =>
{
    if (string.IsNullOrEmpty(n) || !ulong.TryParse(n, out ulong number))
    {
        return "Por favor, proporciona un número válido en la URL. Ejemplo: http://localhost:5000/?n=10";
    }

    try
    {
        // 1. Consumir el servicio SOAP
        var client = new NumberConversionSoapTypeClient(NumberConversionSoapTypeClient.EndpointConfiguration.NumberConversionSoap);
        var response = await client.NumberToWordsAsync(number);
        string resultadoIngles = response.Body.NumberToWordsResult.Trim();

        // 2. Traducir el resultado de Inglés a Español
        var translator = new AggregateTranslator();
        var translation = await translator.TranslateAsync(resultadoIngles, "es", "en");

        // Retornamos el resultado traducido
        return translation.Translation.ToLower();
    }
    catch (Exception ex)
    {
        return $"Error en el proceso: {ex.Message}";
    }
});

app.Run();