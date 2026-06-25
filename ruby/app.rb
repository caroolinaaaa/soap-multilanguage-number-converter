require 'sinatra'
require 'savon'
require 'net/http'
require 'json'
require 'uri'

set :port, 5000

get '/' do
  numero = params['n']
  
  if numero.nil? || numero.empty?
    return "Por favor, proporciona un número válido en la URL. Ejemplo: http://localhost:5000/?n=10"
  end

  begin
    # 1. Consumir el servicio SOAP
    client = Savon.client(wsdl: "https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL")
    response = client.call(:number_to_words, message: { ubiNum: numero })
    resultado_ingles = response.body[:number_to_words_response][:number_to_words_result].to_s.strip

    # 2. Traducir usando la API de MyMemory de Inglés a Español
    url_texto = URI.encode_www_form_component(resultado_ingles)
    uri = URI("https://api.mymemory.translated.net/get?q=#{url_texto}&langpair=en|es")
    
    res = Net::HTTP.get(uri)
    datos = JSON.parse(res)
    resultado_espanol = datos["responseData"]["translatedText"]
    
    resultado_espanol.downcase
  rescue => e
    "Error en el proceso: #{e.message}"
  end
end