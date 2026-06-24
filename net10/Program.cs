using Microsoft.AspNetCore.Mvc;
using ServiceReference;

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
        // Creamos el cliente SOAP generado por dotnet-svcutil
        var client = new NumberConversionSoapTypeClient(NumberConversionSoapTypeClient.EndpointConfiguration.NumberConversionSoap);
        
        // Llamamos al método pasándole el número
        var response = await client.NumberToWordsAsync(number);
        
        // Retornamos el resultado (que vendrá en inglés)
        return response.Body.NumberToWordsResult.Trim();
    }
    catch (Exception ex)
    {
        return $"Error al conectar con el servicio SOAP: {ex.Message}";
    }
});

app.Run();