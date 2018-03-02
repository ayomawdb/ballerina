package org.ballerinalang.test.taintchecking;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.annotations.Test;

public class TaintedStatusPropergationTest {

    @Test
    public void testReturnPropergation() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/returns.bal");
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 3, 5);
    }

    @Test
    public void testVariablePropergation() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/variables.bal");
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'args'", 2, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'args'", 5, 5);
    }
}
