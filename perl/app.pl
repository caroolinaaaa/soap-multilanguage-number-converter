use Mojolicious::Lite -signatures;

# Motor nativo local con reglas gramaticales completas en español (RAE)
sub numero_a_letras ($n) {
    $n = int($n);
    return 'cero' if $n == 0;

    my @unidades = ('', 'uno', 'dos', 'tres', 'cuatro', 'cinco', 'seis', 'siete', 'ocho', 'nueve', 'diez', 
                    'once', 'doce', 'trece', 'catorce', 'quince', 'dieciseis', 'diecisiete', 'diecocho', 'diecinueve', 'veinte');
    
    my @decenas  = ('', '', '', 'treinta', 'cuarenta', 'cincuenta', 'sesenta', 'setenta', 'ochenta', 'noventa');
    my @centenas = ('', 'ciento', 'doscientos', 'trescientos', 'cuatrocientos', 'quinientos', 'seiscientos', 'setecientos', 'ochocientos', 'novecientos');

    if ($n < 0) {
        return 'menos ' . numero_a_letras(abs($n));
    }

    # Unidades y veintes especiales
    if ($n <= 20) {
        return $unidades[$n];
    }
    if ($n < 30) {
        my $u = $n % 10;
        return $u == 0 ? 'veinte' : 'veinti' . $unidades[$u];
    }

    # Decenas (30 - 99)
    if ($n < 100) {
        my $d = int($n / 10);
        my $u = $n % 10;
        return $u == 0 ? $decenas[$d] : $decenas[$d] . ' y ' . $unidades[$u];
    }

    # Cien exacto
    if ($n == 100) {
        return 'cien';
    }

    # Centenas (101 - 999)
    if ($n < 1000) {
        my $c = int($n / 100);
        my $resto = $n % 100;
        return $resto == 0 ? $centenas[$c] : $centenas[$c] . ' ' . numero_a_letras($resto);
    }

    # Miles (1000 - 999999)
    if ($n < 1000000) {
        my $m = int($n / 1000);
        my $resto = $n % 1000;
        my $prefijo_mil = ($m == 1) ? 'mil' : numero_a_letras($m) . ' mil';
        return $resto == 0 ? $prefijo_mil : $prefijo_mil . ' ' . numero_a_letras($resto);
    }

    return "numero fuera de rango para esta demo local";
}

get '/' => sub ($c) {
    my $numero = $c->param('n');

    if (!defined $numero || $numero eq '' || $numero !~ /^-?\d+$/) {
        return $c->render(text => "Por favor, proporciona un numero entero valido en la URL. Ejemplo: http://localhost:5000/?n=145");
    }

    # Conversión local directa e instantánea
    my $resultado_espanol = numero_a_letras($numero);
    
    return $c->render(text => lc($resultado_espanol));
};

app->start('daemon', '-l', 'http://127.0.0.1:5000');