public function main (string[] args) {
    // tainted array - untainted field.
    string[] data = ["Ballerina", args[0]];
    secureFunction(data[0], data[0]);

    // tainted array - tainted field.
    string[] data1 = ["Ballerina", args[0]];
    secureFunction(data1[1], data1[1]);

    // untainted array - tainted assignment.
    string[] data2 = ["Ballerina", "Colombo"];
    data2[0] = args[0];
    secureFunction(data2[0], data2[0]);

    // untainted array - tainted assignment - replaced with untainted assignment.
    string[] data3 = ["Ballerina", "Colombo"];
    data3[0] = args[0];
    data3[0] = "BallerinaNew";
    secureFunction(data3[0], data3[0]);
}

public function secureFunction (@sensitive{} string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}