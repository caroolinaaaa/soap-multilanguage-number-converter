const soap = require("soap");
const translate = require("translate-google");

const numero = process.argv[2];

const url =
"https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL";

soap.createClient(url, (err, client) => {

    if (err) {
        console.log(err);
        return;
    }

    client.NumberToWords(
        { ubiNum: numero },
        async (err, result) => {

            if (err) {
                console.log(err);
                return;
            }

            const texto =
                result.NumberToWordsResult;

            const traduccion =
                await translate(texto, {
                    to: "es"
                });

            console.log(traduccion);
        }
    );
});