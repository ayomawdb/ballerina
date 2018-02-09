/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.logging.util;

/**
 * Utility to sanitize log lines
 *
 * @since 0.96.1
 */
public class SanitizerUtils {

    /**
     * Sanitize log message by replacing disallowed strings with a replacement character
     *
     * @param message Log message
     * @param disallowedStrings Strings that should be sanitized
     * @param replacement Replacement character to use, when a disallowed string is found (default is _)
     * @return Sanitized log message
     */
    public static String sanitize(String message, String[] disallowedStrings, String replacement) {
        if (disallowedStrings != null && message != null) {
            if (replacement == null) {
                replacement = Constants.DEFAULT_REPLACEMENT_CHARACTER;
            }
            for (String disallowedString : disallowedStrings) {
                if (message.indexOf(disallowedString) > -1) {
                    message = message.replace(disallowedString, replacement);
                }
            }
        }
        return message;
    }
}
