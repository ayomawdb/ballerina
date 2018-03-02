public function main (string[] args) {
    string data = "static";
    
    xml x1 = xml `<book>The Lost World</book>`;
    secureFunction(x1, x1);

    xml x2 = xml `Hello, world!`;
    secureFunction(x2, x2);

    xml x3 = xml `<!--I am a comment-->`;
    secureFunction(x3, x3);

    xml x4 = xml `<?target data?>`;
    secureFunction(x4, x4);

    xml x5 = x1 + x2 + x3 + x4;
    secureFunction(x5, x5);

    xml x6 = xml `<book>The Lost World{{data}}</book>`;
    secureFunction(x6, x6);

    xml x7 = xml `<{{data}}>The Lost World</{{data}}>`;
    secureFunction(x7, x7);


    xmlns "http://ballerina.com/updated" as ns0;
    xml x8 = xml `<ns0:book ns0:status="available" count="5"/>`;
    secureFunction(x8@[ns0:status], x8@[ns0:status]);

    xml x9 = xml `<ns0:book ns0:status="{{data}}" count="5"/>`;
    secureFunction(x9@[ns0:status], x9@[ns0:status]);
    secureFunction(x9@[ns0:count], x9@[ns0:count]);
}

public function secureFunction (@sensitive{} any secureIn, any insecureIn) {

}