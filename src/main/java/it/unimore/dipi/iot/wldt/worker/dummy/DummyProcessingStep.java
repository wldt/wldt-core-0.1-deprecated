package it.unimore.dipi.iot.wldt.worker.dummy;

import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.ProcessingStep;
import it.unimore.dipi.iot.wldt.processing.ProcessingStepListener;
import it.unimore.dipi.iot.wldt.processing.cache.PipelineCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Optional;

@Named("IdentityProcessingStep")
public class DummyProcessingStep implements ProcessingStep {

    private static final Logger logger = LoggerFactory.getLogger(DummyProcessingStep.class);

    @Override
    public void execute(PipelineCache pipelineCache, PipelineData data, ProcessingStepListener listener) {
        if(data instanceof DummyPipelineData && listener != null) {
            DummyPipelineData pipelineData = (DummyPipelineData)data;

            logger.debug("Executing DummyProcessingStep Step with data: {}", data.toString());

            //Updating pipeline data
            pipelineData.setValue(pipelineData.getValue()*2);

            listener.onStepDone(this, Optional.of(pipelineData));
        }
        else {
            if(listener != null) {
                String errorMessage = "PipelineData Error !";
                logger.error(errorMessage);
                listener.onStepError(this, data, errorMessage);
            }
            else
                logger.error("Processing Step Listener = Null ! Skipping processing step");
        }
    }
}
