package it.unimore.wldt.test.processing;

import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.cache.PipelineCache;
import it.unimore.dipi.iot.wldt.processing.ProcessingStep;
import it.unimore.dipi.iot.wldt.processing.ProcessingStepListener;

public class TestProcessingStep implements ProcessingStep {

    @Override
    public void execute(PipelineCache pipelineCache, PipelineData data, ProcessingStepListener listener) {
        if(data instanceof TestPipelineData){
            TestPipelineData testPipelineData = (TestPipelineData)data;
            if(listener != null)
                listener.onStepDone(this, java.util.Optional.of(new TestPipelineData(String.format("%s-%s", testPipelineData.getValue(), testPipelineData.getValue()))));
        }
        else
            System.err.println("Error Pipeline Data provided !");
    }
}
