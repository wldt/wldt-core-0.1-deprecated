package it.unimore.dipi.iot.wldt.processing.step;

import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.cache.PipelineCache;
import it.unimore.dipi.iot.wldt.processing.ProcessingStep;
import it.unimore.dipi.iot.wldt.processing.ProcessingStepListener;
import it.unimore.dipi.iot.wldt.worker.mqtt.MqttPipelineData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Optional;
import javax.inject.Named;

@Named("MqttAverageProcessingStep")
public class MqttAverageProcessingStep implements ProcessingStep {

    private static final Logger logger = LoggerFactory.getLogger(MqttAverageProcessingStep.class);

    private final static String PIPELINE_CACHE_VALUE_LIST = "value_list";

    public MqttAverageProcessingStep() {
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

            if(listener != null && data.getPayload() != null){

                String payloadBodyString = new String(data.getPayload());

                if(isNumeric(payloadBodyString)){

                    Double bodyDoubleValue = Double.parseDouble(payloadBodyString);

                    //Init Pipeline Cache
                    if(pipelineCache.getData(this, PIPELINE_CACHE_VALUE_LIST) == null)
                        pipelineCache.addData(this, PIPELINE_CACHE_VALUE_LIST, new ArrayList<Double>());

                    ArrayList<Double> valueList = (ArrayList<Double>) pipelineCache.getData(this, PIPELINE_CACHE_VALUE_LIST);
                    valueList.add(bodyDoubleValue);

                    logger.info("Cached list size: {}", valueList.size());

                    if(valueList.size() == 10){

                        double sum = valueList.stream().mapToDouble(value -> value).sum();
                        double average = sum / (double)valueList.size();

                        valueList.clear();
                        pipelineCache.addData(this, PIPELINE_CACHE_VALUE_LIST, valueList);

                        listener.onStepDone(this, java.util.Optional.of(new MqttPipelineData(String.format("%s/%s", data.getTopic(), "average"), Double.toString(average).getBytes())));
                    }
                    else{
                        pipelineCache.addData(this, PIPELINE_CACHE_VALUE_LIST, valueList);
                        listener.onStepDone(this, Optional.empty());
                    }
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
