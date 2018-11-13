// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Configures the SSL/TLS options to be used for client sockets.
#
# + trustStore - Configures the trust store to be used
# + trustedCertFile - A file containing a list of certificates or a single certificate that the client trusts
public type SecureClientSocket record {
    TrustStore? trustStore;
    string trustedCertFile;
    !...
};

# A record for providing trust store related configurations.
#
# + path - Path to the trust-store file
# + password - Trust-store password
# + alias - Certificate alias
public type TrustStore record {
    string path;
    string password;
    string alias;
    !...
};


# A record for providing key store related configurations.
#
# + storePath - Path to the key-store file
# + storePassword - Key-store password
# + keyAlias - Key alias
# + keyPassword - Key password
public type KeyStore record {
    string keyStorePath;
    string keyStorePassword;
    string keyAlias;
    string keyPassword;
    !...
};
