package org.smartregister.chw.hts.domain;

import java.io.Serializable;

public class SampleRegistrationObject implements Serializable {

    private String sampleType;
    private String iqcType;
    private String testingPoint;

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getIqcType() {
        return iqcType;
    }

    public void setIqcType(String iqcType) {
        this.iqcType = iqcType;
    }

    public String getTestingPoint() {
        return testingPoint;
    }

    public void setTestingPoint(String testingPoint) {
        this.testingPoint = testingPoint;
    }
}
