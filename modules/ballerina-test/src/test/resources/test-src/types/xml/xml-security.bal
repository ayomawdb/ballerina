function testXmlEntityInjection() (string) {
    var xmlDoc, _ = <xml>("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                          "<!DOCTYPE bookId [" +
                          "<!ELEMENT bookId ANY >" +
                          "<!ENTITY xxe SYSTEM \"file:///\" >]>" +
                          "<bookId>&xxe;</bookId>");
    return <string>xmlDoc;
}

function testSmallerXmlEntityExpansion() (string) {
    var xmlDoc, _ = <xml>("<?xml version=\"1.0\"?>\n" +
                          "<!DOCTYPE bookId [\n" +
                          "<!ENTITY testId \"testId\">\n" +
                          "<!ENTITY testId1 \"&testId;&testId\">\n" +
                          "]>\n" +
                          "<bookId>&testId1;</bookId>");
    return <string>xmlDoc;
}
