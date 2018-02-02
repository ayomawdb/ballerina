function testExcessiveXmlEntityExpansion() (string) {
    var xmlDoc, _ = <xml>("<?xml version=\"1.0\"?>\n" +
                          "<!DOCTYPE bookId [\n" +
                          "<!ENTITY testId \"testId\">\n" +
                          "<!ENTITY testId2 \"&testId;&testId;&testId;&testId;&testId;&testId;&testId;&testId;&testId;&testId;\">\n" +
                          "<!ENTITY testId3 \"&testId2;&testId2;&testId2;&testId2;&testId2;&testId2;&testId2;&testId2;&testId2;&testId2;\">\n" +
                          "<!ENTITY testId4 \"&testId3;&testId3;&testId3;&testId3;&testId3;&testId3;&testId3;&testId3;&testId3;&testId3;\">\n" +
                          "<!ENTITY testId5 \"&testId4;&testId4;&testId4;&testId4;&testId4;&testId4;&testId4;&testId4;&testId4;&testId4;\">\n" +
                          "<!ENTITY testId6 \"&testId5;&testId5;&testId5;&testId5;&testId5;&testId5;&testId5;&testId5;&testId5;&testId5;\">\n" +
                          "]>\n" +
                          "<bookId>&testId6;</bookId>");
    return <string>xmlDoc;
}