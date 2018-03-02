public function main (string[] args) {
    untaintedReplacedByTainedValue(args);
    untaintedReplacedByTainedValueReplacedByUntaintedValue(args);
    concatUntaintedValues(args);
    concatUntainedValueWithTaintedValue(args);
}

public function secureFunction (@sensitive{} string secureIn) {
    string data = secureIn;
}

function untaintedReplacedByTainedValue(string[] args) {
    string data = "sample";
    data = data + args[0];
    secureFunction(data);
}

function untaintedReplacedByTainedValueReplacedByUntaintedValue(string[] args) {
    string data = "sample";
    data = data + args[0];
    data = "replace";
    secureFunction(data);
}

function concatUntaintedValues(string[] args) {
    string data = testConcatHelper("sample", "sample");
    secureFunction(data);
}

function concatUntainedValueWithTaintedValue(string[] args) {
    string data = testConcatHelper("sample", args[0]);
    secureFunction(data);
}

function testConcatHelper(string s1, string s2) (string) {
    return s1 + s2;
}
