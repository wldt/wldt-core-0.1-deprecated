package it.unimore.dipi.iot.wldt.worker.dummy;

import it.unimore.dipi.iot.wldt.processing.PipelineData;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 22/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class DummyPipelineData implements PipelineData {

    private int value = 0;

    public DummyPipelineData(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DummyPipelineData{");
        sb.append("value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
