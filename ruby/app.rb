require 'sinatra'
require 'numbers_and_words'

set :port, 5000

# Configurar el idioma por defecto de la librería a español
I18n.enforce_available_locales = false
I18n.locale = :es

get '/' do
  numero = params['n']
  
  # Validamos que el parámetro exista y sea un número entero válido
  if numero.nil? || numero.empty? || numero !~ /\A\d+\z/
    return "Por favor, proporciona un número entero válido en la URL. Ejemplo: http://localhost:5000/?n=10"
  end

  begin
    # Convertimos la cadena a un entero y luego a letras en español
    resultado_letras = numero.to_i.to_words
    
    resultado_letras.downcase
  rescue => e
    "Error en la conversión: #{e.message}"
  end
end 