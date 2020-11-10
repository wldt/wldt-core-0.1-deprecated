package it.unimore.dipi.iot.wldt.worker.mqtt;

import com.codahale.metrics.Timer;
import it.unimore.dipi.iot.wldt.metrics.WldtMetricsManager;
import it.unimore.dipi.iot.wldt.exception.WldtMqttModuleException;
import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipeline;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipelineListener;
import it.unimore.dipi.iot.wldt.processing.step.ProcessingStepLoader;
import it.unimore.dipi.iot.wldt.utils.TopicTemplateManager;
import it.unimore.dipi.iot.wldt.worker.coap.Coap2CoapWorker;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class Mqtt2MqttManager {

    private static final Logger logger = LoggerFactory.getLogger(Mqtt2MqttManager.class);

    private static final String TAG = "[WLDT-MQTT-Manager]";

    private String wldtId;

    private Mqtt2MqttConfiguration mqtt2MqttConfiguration;

    /**
     * MQTT Client to received data from the "physical" twin
     */
    private IMqttClient incomingMqttClient;

    /**
     * MQTT Client to send data to a destination broker
     */
    private IMqttClient outgoingMqttClient;

    /**
     * Reference to the Mqtt2MqttWorker
     */
    private Mqtt2MqttWorker mqtt2MqttWorker;

    private Mqtt2MqttManager(){

    }

    public Mqtt2MqttManager(String wldtId, Mqtt2MqttConfiguration mqtt2MqttConfiguration, Mqtt2MqttWorker mqtt2MqttWorker) throws WldtMqttModuleException, MqttException, IOException {

        if(mqtt2MqttConfiguration == null)
            throw new WldtMqttModuleException("Provided DTDPMqttProtocol = null !");

        if(mqtt2MqttConfiguration.getBrokerLocal()
                && mqtt2MqttConfiguration.getBrokerAddress() != null
                && mqtt2MqttConfiguration.getBrokerPort() > 0
                && mqtt2MqttConfiguration.getDeviceId() != null) {

            this.mqtt2MqttConfiguration = mqtt2MqttConfiguration;
            this.wldtId = wldtId;
            this.mqtt2MqttWorker = mqtt2MqttWorker;

            //TODO CHECK AND REMOVE IF NEEDED
            //initProcessingPipelines();

        }
        else
            throw new WldtMqttModuleException("DTDPMqttProtocol configuration error ! Missing Broker Info or Device Id. Remote Broker are not yet supported");

    }

    /*
    private void initProcessingPipelines() {

        ProcessingStepLoader processingStepLoader = new ProcessingStepLoader();

        //Load Device Telemetry Processing Pipeline
        if(this.mqtt2MqttConfiguration.getDeviceTelemetryProcessingStepList() != null && this.mqtt2MqttConfiguration.getDeviceTelemetryProcessingStepList().size() > 0){

            this.deviceTelemetryprocessingPipeline = new ProcessingPipeline();

            this.mqtt2MqttConfiguration.getDeviceTelemetryProcessingStepList().forEach(pipelineStepName -> {
                this.deviceTelemetryprocessingPipeline.addStep(processingStepLoader.loadAnnotated(pipelineStepName));
            });

            logger.info("Device Telemetry Processing Pipeline Initialized ! Step Size: {}", this.deviceTelemetryprocessingPipeline.getSize());
        }

        //Load Resource Telemetry Processing Pipeline
        if(this.mqtt2MqttConfiguration.getResourceTelemetryProcessingStepList() != null && this.mqtt2MqttConfiguration.getResourceTelemetryProcessingStepList().size() > 0){

            this.resourceTelemetryprocessingPipeline = new ProcessingPipeline();

            this.mqtt2MqttConfiguration.getResourceTelemetryProcessingStepList().forEach(pipelineStepName -> {
                this.resourceTelemetryprocessingPipeline.addStep(processingStepLoader.loadAnnotated(pipelineStepName));
            });

            logger.info("Resource Telemetry Processing Pipeline Initialized ! Step Size: {}", this.resourceTelemetryprocessingPipeline.getSize());
        }

        //Load Event Telemetry Processing Pipeline
        if(this.mqtt2MqttConfiguration.getEventProcessingStepList() != null && this.mqtt2MqttConfiguration.getEventProcessingStepList().size() > 0){

            this.eventTelemetryprocessingPipeline = new ProcessingPipeline();

            this.mqtt2MqttConfiguration.getEventProcessingStepList().forEach(pipelineStepName -> {
                this.eventTelemetryprocessingPipeline.addStep(processingStepLoader.loadAnnotated(pipelineStepName));
            });

            logger.info("Resource Telemetry Processing Pipeline Initialized ! Step Size: {}", this.eventTelemetryprocessingPipeline.getSize());
        }
    }
    */

    public void initCommandMessageManagement() {

    }

    public void initEventMessageManagement() throws MqttException, WldtMqttModuleException, IOException{


        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_TOPIC_REGISTRATION_TIME);

        try {

            if(this.mqtt2MqttConfiguration.getEventTopic() != null){
                String deviceEventTopic = TopicTemplateManager.getTopicForDevice(this.mqtt2MqttConfiguration.getEventTopic(), this.mqtt2MqttConfiguration.getDeviceId());
                registerToIncomingMqttTopic(deviceEventTopic, this::handleEventMessage);
                logger.info("{} Registered to Device Event Topic: {}", TAG, deviceEventTopic);
            }
            else
                logger.info("Event Topic Configuration = null ! Avoiding registering to the topic ...");

        } finally {
            if(context != null)
                context.stop();
        }
    }

    public void initTelemetryMessageManagement() throws MqttException, WldtMqttModuleException, IOException {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_TOPIC_REGISTRATION_TIME);

        try {

            if(this.mqtt2MqttConfiguration.getDeviceTelemetryTopic() != null){

                String deviceTelemetryTopic = TopicTemplateManager.getTopicForDevice(this.mqtt2MqttConfiguration.getDeviceTelemetryTopic(), this.mqtt2MqttConfiguration.getDeviceId());
                registerToIncomingMqttTopic(deviceTelemetryTopic, this::handleDeviceTelemetryMessage);

                logger.info("{} Registered to Device Telemetry Topic: {}", TAG, deviceTelemetryTopic);

                if(this.mqtt2MqttConfiguration.getResourceTelemetryTopic() != null){
                    //Handle Telemetry topic for available device's resources
                    if(this.mqtt2MqttConfiguration.getResourceIdList() != null && this.mqtt2MqttConfiguration.getResourceIdList().size() > 0){

                        this.mqtt2MqttConfiguration.getResourceIdList().forEach(resourceId -> {
                            try {
                                String resourceTelemetryTopic = TopicTemplateManager.getTopicForDeviceResource(this.mqtt2MqttConfiguration.getResourceTelemetryTopic(), this.mqtt2MqttConfiguration.getDeviceId(), resourceId);
                                registerToIncomingMqttTopic(resourceTelemetryTopic, this::handleResourceTelemetryMessage);
                                logger.info("{} Registered to ResourceId Telemetry Topic: {}", TAG, resourceTelemetryTopic);
                            } catch (WldtMqttModuleException | MqttException | IOException e) {
                                logger.error("{} Error registering for resourceId: {} telemetry topics ! Exception: {}", TAG, resourceId, e.getLocalizedMessage());
                            }
                        });
                    }
                }
                else
                    logger.info("Resource Telemetry Topic Configuration = null ! Avoiding registering to the topic ...");


            }else
                logger.info("Device Telemetry Topic Configuration = null ! Avoiding registering to the topic ...");

        } finally {
            if(context != null)
                context.stop();
        }
    }


    private void handleEventMessage(String topic, MqttMessage msg) {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_EVENT_FORWARD_TIME);

        try {
            byte[] payload = msg.getPayload();

            WldtMetricsManager.getInstance().measureMqttIncomingPayloadSizeMetric(payload.length);
            WldtMetricsManager.getInstance().measureMqttIncomingEventPayloadSizeMetric(payload.length);

            logger.debug("{} EVENT TOPIC ({}) Message Received: {}", TAG, topic, new String(payload));

            if(mqtt2MqttConfiguration.getOutgoingPublishingEnabled()){

                if(this.getMqtt2MqttWorker() != null && this.getMqtt2MqttWorker().hasProcessingPipeline(Mqtt2MqttWorker.DEFAULT_EVENT_PIPELINE)){

                    logger.info("Executing Processing Pipeline ({}) for topic: {} ...", Mqtt2MqttWorker.DEFAULT_EVENT_PIPELINE, topic);

                    this.getMqtt2MqttWorker().executeProcessingPipeline(Mqtt2MqttWorker.DEFAULT_EVENT_PIPELINE, new MqttPipelineData(topic, payload), new ProcessingPipelineListener() {

                        @Override
                        public void onPipelineDone(Optional<PipelineData> result) {

                            if(result.isPresent() && result.get() instanceof MqttPipelineData){
                                try {
                                    MqttPipelineData processingInfo = (MqttPipelineData)result.get();
                                    publishData(outgoingMqttClient, getOutgoingTopic(processingInfo.getTopic()), processingInfo.getPayload());
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            else
                                logger.warn("MQTT Processing Pipeline produced an empty result for Topic: {} ! Skipping data forwarding !", topic);
                        }

                        @Override
                        public void onPipelineError() {
                            logger.error("Error MQTT Processing Pipeline for Topic: {} ! Skipping data forwarding !", topic);
                        }

                    });

                }
                else
                    publishData(outgoingMqttClient, getOutgoingTopic(topic), payload);
            }

        }catch (Exception e){
            logger.error("{} ERROR FORWARDING MESSAGE TO TOPIC: {}", TAG, topic);
        } finally {
            if(context != null)
                context.stop();
        }
    }

    private void handleDeviceTelemetryMessage(String topic, MqttMessage msg) {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_TELEMETRY_FORWARD_TIME);

        try {

            byte[] payload = msg.getPayload();
            WldtMetricsManager.getInstance().measureMqttIncomingPayloadSizeMetric(payload.length);
            WldtMetricsManager.getInstance().measureMqttIncomingTelemetryPayload(payload.length);

            logger.debug("{} DEVICE TELEMETRY TOPIC ({}) Message Received: {}", TAG, topic, new String(payload));

            if(mqtt2MqttConfiguration.getOutgoingPublishingEnabled()){

                if(this.getMqtt2MqttWorker() != null && this.getMqtt2MqttWorker().hasProcessingPipeline(Mqtt2MqttWorker.DEFAULT_DEVICE_TELEMETRY_PROCESSING_PIPELINE)){

                    logger.info("Executing Processing Pipeline ({}) for topic: {} ...", Mqtt2MqttWorker.DEFAULT_DEVICE_TELEMETRY_PROCESSING_PIPELINE, topic);

                    this.getMqtt2MqttWorker().executeProcessingPipeline(Mqtt2MqttWorker.DEFAULT_DEVICE_TELEMETRY_PROCESSING_PIPELINE, new MqttPipelineData(topic, payload), new ProcessingPipelineListener() {

                        @Override
                        public void onPipelineDone(Optional<PipelineData> result) {

                            if(result.isPresent() && result.get() instanceof MqttPipelineData){
                                try {
                                    MqttPipelineData processingInfo = (MqttPipelineData) result.get();
                                    publishData(outgoingMqttClient, getOutgoingTopic(processingInfo.getTopic()), processingInfo.getPayload());
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            else
                                logger.warn("MQTT Processing Pipeline produced an empty result for Topic: {} ! Skipping data forwarding !", topic);
                        }

                        @Override
                        public void onPipelineError() {
                            logger.error("Error MQTT Processing Pipeline for Topic: {} ! Skipping data forwarding !", topic);
                        }

                    });
                }
                else
                    publishData(outgoingMqttClient, getOutgoingTopic(topic), payload);
            }

        }catch (Exception e){
            logger.error("{} ERROR FORWARDING TELEMETRY MESSAGE TO TOPIC: {}", TAG, topic);
        } finally {
            if(context != null)
                context.stop();
        }
    }

    private void handleResourceTelemetryMessage(String topic, MqttMessage msg) {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_TELEMETRY_FORWARD_TIME);

        try {

            byte[] payload = msg.getPayload();

            WldtMetricsManager.getInstance().measureMqttIncomingPayloadSizeMetric(payload.length);
            WldtMetricsManager.getInstance().measureMqttIncomingTelemetryPayload(payload.length);

            logger.debug("{} RESOURCE TELEMETRY TOPIC ({}) Message Received: {}", TAG, topic, new String(payload));

            if(mqtt2MqttConfiguration.getOutgoingPublishingEnabled()){

                if(this.getMqtt2MqttWorker() != null && this.getMqtt2MqttWorker().hasProcessingPipeline(Mqtt2MqttWorker.DEFAULT_RESOURCE_TELEMETRY_PROCESSING_PIPELINE)){

                    logger.info("Executing Processing Pipeline ({}) for topic: {} ...", Mqtt2MqttWorker.DEFAULT_RESOURCE_TELEMETRY_PROCESSING_PIPELINE, topic);

                    this.getMqtt2MqttWorker().executeProcessingPipeline(Mqtt2MqttWorker.DEFAULT_RESOURCE_TELEMETRY_PROCESSING_PIPELINE, new MqttPipelineData(topic, payload), new ProcessingPipelineListener() {

                        @Override
                        public void onPipelineDone(Optional<PipelineData> result) {

                            if(result.isPresent() && result.get() instanceof MqttPipelineData){
                                try {
                                    MqttPipelineData processingInfo = (MqttPipelineData)result.get();
                                    publishData(outgoingMqttClient, getOutgoingTopic(processingInfo.getTopic()), processingInfo.getPayload());
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            else
                                logger.warn("MQTT Processing Pipeline produced an empty result for Topic: {} ! Skipping data forwarding !", topic);
                        }

                        @Override
                        public void onPipelineError() {
                            logger.error("Error MQTT Processing Pipeline for Topic: {} ! Skipping data forwarding !", topic);
                        }

                    });
                }
                else
                    publishData(outgoingMqttClient, getOutgoingTopic(topic), payload);
            }

        }catch (Exception e){
            logger.error("{} ERROR FORWARDING TELEMETRY MESSAGE TO TOPIC: {}", TAG, topic);
        } finally {
            if(context != null)
                context.stop();
        }
    }

    private void registerToIncomingMqttTopic(String topic, IMqttMessageListener mqttMessageListener) throws WldtMqttModuleException, MqttException {

        if(incomingMqttClient != null && incomingMqttClient.isConnected())
            incomingMqttClient.subscribe(topic, mqttMessageListener);
        else
            throw new WldtMqttModuleException("MQTT Client = NULL or Not Connected ! Impossible to subscribe to a target topic !");
    }

    private void publishData(IMqttClient mqttClient, String topic, byte[] payload) throws MqttException {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_OUTGOING_PUBLISH_DATA_TIME);

        try {

            logger.debug("{} Forwarging to TOPIC: {} Message: {}", TAG, topic, new String(payload));

            if (mqttClient.isConnected() && topic != null && payload.length > 0) {
                MqttMessage msg = new MqttMessage(payload);
                msg.setQos(this.mqtt2MqttConfiguration.getOutgoingClientQoS());
                msg.setRetained(this.mqtt2MqttConfiguration.getOutgoingClientRetainedMessages());
                mqttClient.publish(topic,msg);
                logger.debug("Data Correctly Published !");
            }
            else{
                logger.error("{} Error: Topic Null,  Msg empty or MQTT Client is not Connected !", TAG);
            }

        } catch (MqttException e){
            throw e;
        } finally {
            if(context != null)
                context.stop();
        }
    }

    /**
     * Initialize both incoming and outgoind MQTT Clients
     */
    public void init() throws MqttException {

        if(this.mqtt2MqttConfiguration != null) {
            logger.info("{} Initializing Incoming MQTT Client ...", TAG);
            initIncomingClient();
        }
        else
            logger.info("{} DTDPMqttProtocol = null ! Impossible to init the Incoming MQTT Client", TAG);

        if(this.mqtt2MqttConfiguration.getOutgoingPublishingEnabled() &&
                this.mqtt2MqttConfiguration.getDestinationBrokerAddress() != null &&
                this.mqtt2MqttConfiguration.getDestinationBrokerPort() > 0) {
            logger.info("{} Initializing Outgoing MQTT Client ...", TAG);
            initOutgoingClient();
        }
        else
            logger.info("{} Destination Broker Configuration Error ! Impossible to init the Outgoing MQTT Client", TAG);
    }

    /**
     * Initialize the MQTT Consumer to send and receive data and command from and to the Physical Thing
     */
    public void initIncomingClient() throws MqttException {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_INCOMING_CLIENT_SETUP_TIME);

        try {

            incomingMqttClient = new MqttClient(getPhysicalThingMqttBrokerUrl(),String.format("%s%s", this.wldtId, "incoming"), new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            incomingMqttClient.connect(options);

            logger.info("{} INCOMING MQTT Client Connected to {}", TAG, getPhysicalThingMqttBrokerUrl());

        }catch (Exception e){
            throw e;
        } finally {
            if(context != null)
                context.stop();
        }
    }

    /**
     * Initialize the MQTT Client to send and receive data and command to external broker
     */
    public void initOutgoingClient() throws MqttException {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_OUTGOING_CLIENT_SETUP_TIME);

        try {

            outgoingMqttClient = new MqttClient(getDestinationMqttBrokerUrl(),String.format("%s%s", this.wldtId, "outgoing"), new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            outgoingMqttClient.connect(options);

            logger.info("{} OUTGOING MQTT Client Connected to {}", TAG, getDestinationMqttBrokerUrl());

        }catch (Exception e){
            throw e;
        } finally {
            if(context != null)
                context.stop();
        }
    }

    private String getOutgoingTopic(String originalTopic){
        if(mqtt2MqttConfiguration.getDestinationBrokerBaseTopic() == null ||
                mqtt2MqttConfiguration.getDestinationBrokerBaseTopic().equals("null") ||
                mqtt2MqttConfiguration.getDestinationBrokerBaseTopic().length() == 0)
            return originalTopic;
        else
            return String.format("%s/%s", mqtt2MqttConfiguration.getDestinationBrokerBaseTopic(), originalTopic);
    }

    private String getPhysicalThingMqttBrokerUrl(){
        return String.format("tcp://%s:%d",this.mqtt2MqttConfiguration.getBrokerAddress(), this.mqtt2MqttConfiguration.getBrokerPort());
    }

    private String getDestinationMqttBrokerUrl(){

        if(this.mqtt2MqttConfiguration.getDestinationBrokerAddress() == null)
            return null;

        return String.format("tcp://%s:%d",this.mqtt2MqttConfiguration.getDestinationBrokerAddress(), this.mqtt2MqttConfiguration.getDestinationBrokerPort());
    }

    public String getWldtId() {
        return wldtId;
    }

    public void setWldtId(String wldtId) {
        this.wldtId = wldtId;
    }

    public Mqtt2MqttConfiguration getMqtt2MqttConfiguration() {
        return mqtt2MqttConfiguration;
    }

    public void setMqtt2MqttConfiguration(Mqtt2MqttConfiguration mqtt2MqttConfiguration) {
        this.mqtt2MqttConfiguration = mqtt2MqttConfiguration;
    }

    public Mqtt2MqttWorker getMqtt2MqttWorker() {
        return mqtt2MqttWorker;
    }

    public void setMqtt2MqttWorker(Mqtt2MqttWorker mqtt2MqttWorker) {
        this.mqtt2MqttWorker = mqtt2MqttWorker;
    }
}
