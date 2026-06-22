const soap = require("soap");

const numero = process.argv[2];

const url =
"https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL";

soap.createClient(url, (err, client) => {

    if (err) {
        console.log("Error al crear cliente:");
        console.log(err);
        return;
    }

    client.NumberToWords(
        { ubiNum: numero },
        (err, result) => {

            if (err) {
                console.log("Error al consumir SOAP:");
                console.log(err);
                return;
            }

            console.log(result.NumberToWordsResult);
        }
    );
});