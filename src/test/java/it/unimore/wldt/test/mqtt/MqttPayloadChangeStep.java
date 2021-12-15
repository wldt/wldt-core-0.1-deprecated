package it.unimore.wldt.test.mqtt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.ProcessingStep;
import it.unimore.dipi.iot.wldt.processing.ProcessingStepListener;
import it.unimore.dipi.iot.wldt.processing.cache.PipelineCache;
import it.unimore.dipi.iot.wldt.worker.mqtt.MqttPipelineData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Objects;
import java.util.Optional;

@Named("MqttAverageProcessingStep")
public class MqttPayloadChangeStep implements ProcessingStep {

    private static final Logger logger = LoggerFactory.getLogger(MqttPayloadChangeStep.class);

    private final static String PIPELINE_CACHE_VALUE_LIST = "value_list";

    private ObjectMapper mapper;

    public MqttPayloadChangeStep() {
        this.mapper = new ObjectMapper();
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void execute(PipelineCache pipelineCache, PipelineData incomingData, ProcessingStepListener listener) {

        MqttPipelineData data = null;

        if(incomingData instanceof MqttPipelineData)
            data = (MqttPipelineData)incomingData;
        else if(listener != null)
            listener.onStepError(this, incomingData, String.format("Wrong PipelineData for MqttAverageProcessingStep ! Data type: %s", incomingData.getClass()));
        else
            logger.error("Wrong PipelineData for MqttAverageProcessingStep ! Data type: {}", incomingData.getClass());

        try{

            if(listener != null && Objects.requireNonNull(data).getPayload() != null) {
                listener.onStepDone(this,
                        Optional.of(new MqttPipelineData(data.getTopic(),
                                data.getMqttTopicDescriptor(),
                                mapper.writeValueAsBytes(new DemoDataStructure(data.getPayload())), data.isRetained())));
            }
            else
                logger.error("Processing Step Listener or MqttProcessingInfo Data = Null ! Skipping processing step");

        }catch (Exception e){
            logger.error("MQTT Processing Step Error: {}", e.getLocalizedMessage());

            if(listener != null)
                listener.onStepError(this, data, e.getLocalizedMessage());
        }
    }


    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
