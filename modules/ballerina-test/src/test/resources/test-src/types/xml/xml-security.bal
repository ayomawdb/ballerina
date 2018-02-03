function testXmlEntityInjection() (string) {
    var xmlDoc, _ = <xml>("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                          "<!DOCTYPE bookId [\n" +
                          "<!ELEMENT bookId ANY >\n" +
                          "<!ENTITY xxe SYSTEM \"file:///\" >]>\n" +
                          "<bookId>&xxe;</bookId>");
    return <string>xmlDoc;
}

function testSmallerXmlEntityExpansion() (string) {
    var xmlDoc, _ = <xml>("<?xml version=\"1.0\"?>\n" +
                          "<!DOCTYPE bookId [\n" +
                          "<!ENTITY testId \"testId\">\n" +
                          "<!ENTITY testId1 \"&testId;&testId;\">\n" +
                          "]>\n" +
                          "<bookId>&testId1;</bookId>");
    return <string>xmlDoc;
}

function testExcessiveXmlEntityExpansion() (string) {
    var xmlDoc, _ = <xml>("<?xml version=\"1.0\"?>\n" +
                          "<!DOCTYPE lolz [\n" +
                          "<!ENTITY lol \"lol\">\n" +
                          "<!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
                          "<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n" +
                          "<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n" +
                          "<!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n" +
                          "<!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n" +
                          "<!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n" +
                          "<!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n" +
                          "<!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n" +
                          "]>\n" +
                          "<lolz>&lol6;</lolz>");
    return <string>xmlDoc;
}