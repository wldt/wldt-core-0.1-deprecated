package it.unimore.dipi.iot.wldt.processing;

import it.unimore.dipi.iot.wldt.processing.cache.PipelineCache;

public interface ProcessingStep {

    public void execute(PipelineCache pipelineCache, PipelineData data, ProcessingStepListener listener);

}
