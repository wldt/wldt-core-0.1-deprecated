package it.unimore.wldt.test.processing;

import it.unimore.dipi.iot.wldt.processing.PipelineData;

public class TestPipelineData implements PipelineData {

    private String value;

    public TestPipelineData(String value){
            this.value = value;
        }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}