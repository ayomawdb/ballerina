package org.ballerinalang.test.taintchecking;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TaintedStatusPropagationTest {

    @Test
    public void testReturn() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/returns.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testReturnNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/returns-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 3, 5);
    }

    @Test
    public void testVariable() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/variables.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testVariableNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/variables-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 5, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 9, 5);
    }

    @Test
    public void testReceiver() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/receiver.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testReceiverNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/receiver-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 5);
    }

    @Test
    public void testStruct() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/struct.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }
    
    @Test
    public void testStructNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/struct-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 8, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 12, 5);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 16, 5);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 20, 5);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 25, 5);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'secureIn'", 30, 5);
        BAssertUtil.validateError(result, 6, "tainted value passed to sensitive parameter 'secureIn'", 36, 5);
        BAssertUtil.validateError(result, 7, "tainted value passed to sensitive parameter 'secureIn'", 42, 5);
    }

    @Test
    public void testArray() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/array.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testArrayNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/array-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 6);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 8, 5);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 13, 5);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 19, 5);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 23, 5);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'secureIn'", 28, 5);
    }

    @Test
    public void testJson() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/json.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testJsonNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/json-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 8, 5);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 12, 5);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 16, 5);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 21, 5);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'secureIn'", 26, 5);
        BAssertUtil.validateError(result, 6, "tainted value passed to sensitive parameter 'secureIn'", 32, 5);
        BAssertUtil.validateError(result, 7, "tainted value passed to sensitive parameter 'secureIn'", 38, 5);
    }

    @Test
    public void testMap() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/map.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testMapNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/map-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 8, 5);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 12, 5);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 16, 5);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 21, 5);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'secureIn'", 26, 5);
        BAssertUtil.validateError(result, 6, "tainted value passed to sensitive parameter 'secureIn'", 32, 5);
        BAssertUtil.validateError(result, 7, "tainted value passed to sensitive parameter 'secureIn'", 38, 5);
    }

    @Test
    public void testXML() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/xml.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testXMLNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/xml-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 4);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 7, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 10, 5);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 13, 5);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 14, 5);
    }

    @Test
    public void testBasicWorker() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/basic-worker.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testBasicWorkerNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/basic-worker-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 9);
    }

    @Test
    public void testIfCondition() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/if-condition.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testIfConditionNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/if-condition-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 8, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 16, 5);
    }

    @Test
    public void testTernaryExpr() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/ternary.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testTernaryExprNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/ternary-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 6, 5);
    }

    @Test
    public void testLambda() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/lambda.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testLambdaNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/lambda-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 8, 5);
    }

    @Test
    public void testMultiReturn() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/multi-return.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testMultiReturnNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/multi-return-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 7, 5);
    }

    @Test
    public void testStringTemplate() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/string-template.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testStringTemplateNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/string-template-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 5);
    }

    @Test
    public void testTransformer() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/transformer.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testTransformerNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/transformer-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 3);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 24, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 25, 5);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 26, 5);
    }

    @Test
    public void testIterable() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/iterable.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testIterableNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/iterable-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 5);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 5, 43);
    }

    @Test
    public void testForEach() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/foreach.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testForEachNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/foreach-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 5, 9);
    }

    @Test
    public void testMultipleInvocationLevels() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/multiple-invocation-levels.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testMultipleInvocationLevelsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/multiple-invocation-levels-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 10, 5);
    }

    @Test
    public void testGlobalVariables() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/global-variables.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testGlobalVariablesNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/global-variables-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to global variable 'globalVariable'", 12, 5);
    }

    @Test
    public void testHttpService() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/http-service.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 12, 9);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 13, 9);
    }
}
