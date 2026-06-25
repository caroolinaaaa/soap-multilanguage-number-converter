require 'sinatra'
require 'savon'

# Configurar el puerto 5000 como en los otros lenguajes
set :port, 5000

get '/' do
  numero = params['n']
  
  if numero.nil? || numero.empty?
    return "Por favor, proporciona un número válido en la URL. Ejemplo: http://localhost:5000/?n=10"
  end

  begin
    # Configurar el cliente SOAP con el WSDL de DataAccess
    client = Savon.client(wsdl: "https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL")
    
    # Hacer la llamada al método NumberToWords
    response = client.call(:number_to_words, message: { ubiNum: numero })
    
    # Obtener el resultado del cuerpo de la respuesta
    resultado = response.body[:number_to_words_response][:number_to_words_result]
    
    resultado.to_s.strip.downcase
  rescue => e
    "Error al conectar con el servicio SOAP: #{e.message}"
  end
end