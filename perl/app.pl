use Mojolicious::Lite -signatures;
use HTTP::Tiny;

get '/' => sub ($c) {
    # 1. Obtener el parámetro 'n' de la URL
    my $numero = $c->param('n');

    if (!$numero) {
        return $c->render(text => "Por favor, proporciona un numero valido en la URL. Ejemplo: http://localhost:5000/?n=10");
    }

    # 2. Crear el sobre XML para SOAP
    my $soap_envelope = <<"XML";
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
      <ubiNum>$numero</ubiNum>
    </NumberToWords>
  </soap:Body>
</soap:Envelope>
XML

    # 3. Hacer la petición HTTP POST real (Raw Content)
    my $url = 'https://www.dataaccess.com/webservicesserver/NumberConversion.wso';
    my $http = HTTP::Tiny->new;
    
    my $response = $http->request('POST', $url, {
        headers => { 'Content-Type' => 'text/xml; charset=utf-8' },
        content => $soap_envelope
    });

    if (!$response->{success}) {
        return $c->render(text => "Error al conectar con el servicio SOAP: " . $response->{reason});
    }

    my $response_str = $response->{content};

    # 4. Expresión regular flexible para soportar el prefijo "m:" del XML de SOAP
    if ($response_str =~ /<m:?NumberToWordsResult>(.*?)<\/m:?NumberToWordsResult>/) {
        my $resultado_ingles = $1;
        $resultado_ingles =~ s/^\s+|\s+$//g; # Limpiar espacios en blanco
        return $c->render(text => lc($resultado_ingles));
    }

    return $c->render(text => "No se pudo parsear la respuesta SOAP.");
};

# Configurar el puerto 5000 de forma explícita
app->start('daemon', '-l', 'http://127.0.0.1:5000');