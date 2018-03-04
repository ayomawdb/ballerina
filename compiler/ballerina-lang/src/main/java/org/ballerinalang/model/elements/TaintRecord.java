package org.ballerinalang.model.elements;

import java.util.List;

/**
 * TaintRecord.
 */
public class TaintRecord {
    public List<Boolean> retParamTaintdStatus;
    public TaintError taintError;

    public TaintRecord(List<Boolean> retParamTaintdStatus) {
        this.retParamTaintdStatus = retParamTaintdStatus;
    }

    public TaintRecord(TaintError taintError) {
        this.taintError = taintError;
    }
}
