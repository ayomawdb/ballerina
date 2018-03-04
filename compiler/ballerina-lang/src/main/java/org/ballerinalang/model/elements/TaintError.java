package org.ballerinalang.model.elements;

import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Error.
 */
public class TaintError {
    public DiagnosticPos pos;
    public String paramName;
    public DiagnosticCode diagnosticCode;

    public TaintError(DiagnosticPos pos, String paramName, DiagnosticCode diagnosticCode) {
        this.pos = pos;
        this.paramName = paramName;
        this.diagnosticCode = diagnosticCode;
    }
}
