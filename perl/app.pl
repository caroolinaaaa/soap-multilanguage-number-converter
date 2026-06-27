use Mojolicious::Lite -signatures;
use Mojo::JSON qw(decode_json);

get '/' => sub ($c) {
    my $numero = $c->param('n');

    if (!$numero) {
        return $c->render(text => "Por favor, proporciona un numero valido en la URL. Ejemplo: http://localhost:5000/?n=10");
    }

    # 1. Crear el sobre XML para SOAP
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

    # Guardar temporalmente el XML para que curl lo envíe de forma segura
    my $temp_file = 'soap_req.xml';
    open(my $fh, '>', $temp_file) or return $c->render(text => "Error interno al crear archivo temporal");
    print $fh $soap_envelope;
    close($fh);

    # Ejecutar la petición HTTP POST usando el curl nativo de Windows (con --ssl-no-revoke por si las dudas)
    my $url_soap = 'https://www.dataaccess.com/webservicesserver/NumberConversion.wso';
    my $response_str = `curl -s -X POST -H "Content-Type: text/xml; charset=utf-8" --data-binary \@$temp_file "$url_soap" --ssl-no-revoke`;
    
    # Limpiar archivo temporal
    unlink($temp_file);

    my $resultado_ingles = '';
    if ($response_str =~ /<m:?NumberToWordsResult>(.*?)<\/m:?NumberToWordsResult>/) {
        $resultado_ingles = $1;
        $resultado_ingles =~ s/^\s+|\s+$//g; 
    }

    if (!$resultado_ingles) {
        return $c->render(text => "Error al conectar o parsear la respuesta del servicio SOAP.");
    }

    # 2. Consumir el servicio de traducción MyMemory usando curl y decodificando el JSON nativo
    my $url_traduccion = "https://api.mymemory.translated.net/get?q=" . Mojo::Util::url_escape($resultado_ingles) . "&langpair=en|es";
    my $json_raw = `curl -s "$url_traduccion" --ssl-no-revoke`;
    
    # Deserializar la estructura del JSON con la librería incorporada de Mojo
    my $json_data = decode_json($json_raw);
    
    if ($json_data && $json_data->{responseData} && $json_data->{responseData}->{translatedText}) {
        my $resultado_espanol = $json_data->{responseData}->{translatedText};
        return $c->render(text => lc($resultado_espanol));
    }

    return $c->render(text => "No se pudo deserializar la estructura del JSON de traduccion.");
};

app->start('daemon', '-l', 'http://127.0.0.1:5000');