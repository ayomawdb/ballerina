public function main (string[] args) {
    string[] data = ["Ballerina", "Colombo"];
    secureFunction(data[0], data[0]);
}

public function secureFunction (@sensitive{} string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}