package org.ballerinalang.test.taintchecking;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UntaintTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/taintchecking/expressions/untaint-with-other-constructs.bal");
    }

    @Test
    public void testUntaint() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/expressions/untaint.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testUntaintVariable() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/expressions/untaint-variable-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 5, 5);
    }

    @Test
    public void testUntaintWithAddOperator() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithAddOperatorInReturn");
        Assert.assertEquals(returns[0].stringValue(), "input1input2input3input4");
    }

    @Test
    public void untaintWithAddOperatorWithVariable() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithAddOperatorInReturn");
        Assert.assertEquals(returns[0].stringValue(), "input1input2input3input4");
    }

    @Test
    public void untaintWithFunctionParam() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithFunctionParam");
        Assert.assertEquals(returns[0].stringValue(), "input1input2input3input4");
    }

    @Test
    public void untaintWithFunctionReturn() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithFunctionReturn");
        Assert.assertEquals(returns[0].stringValue(), "input1input2input3input4");
    }

    @Test
    public void untaintWithReceiver() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithReceiver");
        Assert.assertEquals(returns[0].stringValue(), "input1input2input3input4");
    }

    @Test
    public void untaintWithLengthOf() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithLengthOf");
        Assert.assertEquals(returns[0].stringValue(), "24");
    }
}
