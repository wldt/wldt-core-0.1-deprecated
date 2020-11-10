package it.unimore.wldt.test.coap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.dipi.iot.wldt.exception.ProcessingPipelineException;
import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.ProcessingStep;
import it.unimore.dipi.iot.wldt.processing.ProcessingStepListener;
import it.unimore.dipi.iot.wldt.processing.cache.PipelineCache;
import it.unimore.dipi.iot.wldt.utils.SenML;
import it.unimore.dipi.iot.wldt.worker.coap.CoapPipelineData;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.util.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Named;
import java.util.Optional;

@Named("CoapTestProcessingStep")
public class TestCoapProcessingStep implements ProcessingStep {

    private static final Logger logger = LoggerFactory.getLogger(TestCoapProcessingStep.class);

    private ObjectMapper objectMapper;

    public TestCoapProcessingStep() {
        super();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void execute(PipelineCache pipelineCache, PipelineData data, ProcessingStepListener listener) {

        if(data instanceof CoapPipelineData && listener != null) {

            CoapPipelineData pipelineData = (CoapPipelineData)data;

            logger.debug("Executing TestCoapProcessingStep URI: {} -> Resource Descriptor: {} -> Payload String Data: {}", pipelineData.getResourceUri(), pipelineData.getWldtCoapResourceDescriptor(), new String(pipelineData.getPayload(), StandardCharsets.UTF_8));

            try{

                pipelineData.setPayload(genearateSenMLPayload(pipelineData));
                pipelineData.setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
                listener.onStepDone(this, Optional.of(pipelineData));

            }catch (ProcessingPipelineException e){
                String errorMessage = String.format("Processing Pipeline Error !", e.getLocalizedMessage());
                logger.error(errorMessage);
                listener.onStepError(this, pipelineData, errorMessage);
            }
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

    private byte[] genearateSenMLPayload(CoapPipelineData pipelineData) throws ProcessingPipelineException {

        try{
            String payloadString = new String(pipelineData.getPayload(), StandardCharsets.UTF_8);
            double numberValue = Double.parseDouble(payloadString);

            SenML senmlData = new SenML();
            senmlData.setBaseName(String.format("%s:%s", pipelineData.getWldtCoapResourceDescriptor().getResourceType(), pipelineData.getWldtCoapResourceDescriptor().getTitle()));
            senmlData.setBaseUnit("C");
            senmlData.setTime(System.currentTimeMillis());
            senmlData.setVersion(1);
            senmlData.setValue(numberValue);

            return objectMapper.writeValueAsString(senmlData).getBytes();

        }catch(NumberFormatException | JsonProcessingException e){
            String errorMessage = String.format("Error parsing Double Payload ! Pipeline Error: %s", e.getLocalizedMessage());
            logger.error(errorMessage);
            throw new ProcessingPipelineException(errorMessage);
        }
    }

}
