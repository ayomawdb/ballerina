package org.ballerinalang.test.taintchecking;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TaintedStatusPropergationTest {

    @Test
    public void testReturnPropergation() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/returns.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 3, 5);
    }

    @Test
    public void testVariablePropergation() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/variables.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'args'", 2, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'args'", 5, 5);
    }

    @Test
    public void testReceiverPropergation() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/receiver.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 5, 5);
    }

    @Test
    public void testStructPropergation() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/struct.bal");
        Assert.assertTrue(result.getDiagnostics().length == 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'args'", 7, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'args'", 8, 5);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'args'", 9, 5);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'args'", 10, 5);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'args'", 13, 5);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'args'", 14, 5);
        BAssertUtil.validateError(result, 6, "tainted value passed to sensitive parameter 'args'", 15, 5);
        BAssertUtil.validateError(result, 7, "tainted value passed to sensitive parameter 'args'", 16, 5);
    }

    @Test
    public void testArrayPropergation() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/array-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 4);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'args'", 3, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'args'", 4, 5);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'args'", 5, 5);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'args'", 6, 5);
    }

    @Test
    public void testJsonPropergation() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/json-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'args'", 3, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'args'", 4, 5);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'args'", 5, 5);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'args'", 6, 5);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'args'", 9, 5);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'args'", 10, 5);
        BAssertUtil.validateError(result, 6, "tainted value passed to sensitive parameter 'args'", 11, 5);
        BAssertUtil.validateError(result, 7, "tainted value passed to sensitive parameter 'args'", 12, 5);
    }

    @Test
    public void testMapPropergation() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/map-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'args'", 3, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'args'", 4, 5);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'args'", 5, 5);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'args'", 6, 5);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'args'", 9, 5);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'args'", 10, 5);
        BAssertUtil.validateError(result, 6, "tainted value passed to sensitive parameter 'args'", 11, 5);
        BAssertUtil.validateError(result, 7, "tainted value passed to sensitive parameter 'args'", 12, 5);
    }

    @Test
    public void testMapPropergation() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/string-template.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'name'", 3, 5);
    }
}
