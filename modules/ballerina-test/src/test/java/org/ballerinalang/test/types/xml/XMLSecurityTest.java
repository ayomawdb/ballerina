package org.ballerinalang.test.types.xml;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class XMLSecurityTest {

    CompileResult xmlSecurity;
    CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        xmlSecurity = BCompileUtil.compile("test-src/types/xml/xml-security.bal");
        negativeResult = BCompileUtil.compile("test-src/types/xml/xml-security-negative.bal");
    }

    @Test
    public void testXmlEntityInjection() {
        BValue[] returns = BRunUtil.invoke(xmlSecurity, "testXmlEntityInjection");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);

        Assert.assertEquals(returns[0].stringValue(), "<bookId><bookId>");
    }

    @Test
    public void testSmallerXmlEntityExpansion() {
        BValue[] returns = BRunUtil.invoke(xmlSecurity, "testSmallerXmlEntityExpansion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);

        Assert.assertEquals(returns[0].stringValue(), "<bookId>testIdtestId<bookId>");
    }


}
