import ballerina/auth;
import ballerina/time;

function testIssueJwt (string keyStorePath) returns (string)|error {
    auth:JwtHeader header = {};
    header.alg = "RS256";
    header.typ = "JWT";

    auth:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    auth:JWTIssuerConfig config = {};
    config.keyAlias = "ballerina";
    config.keyPassword = "ballerina";
    config.keyStoreFilePath = keyStorePath;
    config.keyStorePassword = "ballerina";
    match auth:issue(header, payload, config) {
        string jwtString => return jwtString;
        error err => return err;
    }
}

function testValidateJwt (string jwtToken, string trustStorePath) returns boolean|error {
    auth:JWTValidatorConfig config = {};
    config.issuer = "wso2";
    config.certificateAlias = "ballerina";
    config.audience = "ballerinaSamples";
    config.clockSkew = 60;
    config.trustStoreFilePath = trustStorePath;
    config.trustStorePassword = "ballerina";

    var value = auth:validate(jwtToken, config);
    match value {
        auth:JwtPayload result => return true;
        error err => return err;
    }
}
