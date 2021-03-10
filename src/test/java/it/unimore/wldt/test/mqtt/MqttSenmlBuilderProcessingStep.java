package it.unimore.wldt.test.mqtt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.cache.PipelineCache;
import it.unimore.dipi.iot.wldt.processing.ProcessingStep;
import it.unimore.dipi.iot.wldt.processing.ProcessingStepListener;
import it.unimore.dipi.iot.wldt.worker.mqtt.MqttPipelineData;
import it.unimore.dipi.iot.wldt.utils.SenML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Named;
import java.util.Optional;

@Named("MqttSenmlBuilderProcessingStep")
public class MqttSenmlBuilderProcessingStep implements ProcessingStep {

    private static final Logger logger = LoggerFactory.getLogger(MqttSenmlBuilderProcessingStep.class);

    private ObjectMapper mapper;

    public MqttSenmlBuilderProcessingStep() {
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

            if(listener != null && data != null && data.getPayload() != null){

                String payloadBodyString = new String(data.getPayload());

                if(isNumeric(payloadBodyString)){
                    Double bodyDoubleValue = Double.parseDouble(payloadBodyString);
                    Optional<String> newPayload = buildSenmlDataPayload(data.getTopic(), bodyDoubleValue);

                    if(newPayload.isPresent())
                        listener.onStepDone(this, Optional.of(new MqttPipelineData(data.getTopic(), data.getMqttTopicDescriptor(), newPayload.get().getBytes(), data.isRetained())));
                    else
                        listener.onStepError(this, data, "Error processing the data ! Processing Error ...");
                }
                else
                    listener.onStepError(this, data, "Provided Payload is not a Number ! Skipping processing ....");

            }
            else
                logger.error("Processing Step Listener or MqttProcessingInfo Data = Null ! Skipping processing step");

        }catch (Exception e){
            logger.error("MQTT Processing Step Error: {}", e.getLocalizedMessage());

            if(listener != null)
                listener.onStepError(this, data, e.getLocalizedMessage());
        }
    }

    private Optional<String> buildSenmlDataPayload(String topic, Double doubleValue){

        try{

            SenML senmlData = new SenML();

            senmlData.setBaseName(topic);
            senmlData.setName(topic);
            senmlData.setValue(doubleValue);
            senmlData.setUpdateTime(System.currentTimeMillis());

            return Optional.of(this.mapper.writeValueAsString(senmlData));

        }catch (Exception e){
            logger.error("Error building SENML Data ! Error: {}", e.getLocalizedMessage());
            return Optional.empty();
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
