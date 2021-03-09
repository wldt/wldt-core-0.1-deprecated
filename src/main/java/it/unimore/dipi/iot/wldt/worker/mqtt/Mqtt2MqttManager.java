package it.unimore.dipi.iot.wldt.worker.mqtt;

import com.codahale.metrics.Timer;
import it.unimore.dipi.iot.wldt.exception.WldtWorkerException;
import it.unimore.dipi.iot.wldt.metrics.WldtMetricsManager;
import it.unimore.dipi.iot.wldt.exception.WldtMqttModuleException;
import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipelineListener;
import it.unimore.dipi.iot.wldt.utils.TopicTemplateManager;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private IMqttClient physicalDeviceMqttBrokerClient;

    /**
     * MQTT Client to send data to a destination broker
     */
    private IMqttClient digitalTwinMqttBrokerClient;

    /**
     * Reference to the Mqtt2MqttWorker
     */
    private Mqtt2MqttWorker mqtt2MqttWorker;

    /**
     * Variable used to detect mirroring errors. The mirroring is completed only when all the resources are correctly
     * mirrored. In the MQTT case when the WLDT is correctly registered on all the target topics
     */
    private boolean isMirroringCompleted = true;

    /**
     * Target device telemetry topic. Initialized at the setup of the worker
     * It is null if the target topic is not configured or required.
     */
    private String deviceTelemetryTopic = null;

    /**
     * Target device event topic. Initialized at the setup of the worker
     * It is null if the target topic is not configured or required.
     */
    private String deviceEventTopic = null;

    private Map<String, MqttTopicDescriptor> configuredTopicMap;

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
            this.configuredTopicMap = new HashMap<>();
        }
        else
            throw new WldtMqttModuleException("DTDPMqttProtocol configuration error ! Missing Broker Info or Device Id. Remote Broker are not yet supported");

    }

    /**
     * Initialize both incoming and outgoind MQTT Clients
     */
    public void init() throws WldtMqttModuleException {

        try{

            //Init MQTT Incoming client
            if(this.mqtt2MqttConfiguration != null) {
                logger.info("{} Initializing Incoming MQTT Client ...", TAG);
                initPhysicalDeviceMqttBrokerClient();
            }
            else
                logger.info("{} Mqtt2MqttConfiguration = null ! Impossible to init the Incoming MQTT Client", TAG);

            //Init MQTT Outgoing client
            if(this.mqtt2MqttConfiguration.getOutgoingPublishingEnabled() &&
                    this.mqtt2MqttConfiguration.getDestinationBrokerAddress() != null &&
                    this.mqtt2MqttConfiguration.getDestinationBrokerPort() > 0) {
                logger.info("{} Initializing Outgoing MQTT Client ...", TAG);
                initDigitalTwinMqttBrokerClient();
            }
            else
                logger.info("{} Destination Broker Configuration Error ! Impossible to init the Outgoing MQTT Client", TAG);

            initTopicManagement();

            //Init telemetry and event management
            //TODO this methods in the next version
            //this.initTelemetryMessageManagement();
            //this.initEventMessageManagement();

            //Notify device correctly mirrored
            this.mqtt2MqttWorker.notifyDeviceMirrored(this.mqtt2MqttConfiguration.getDeviceId(), new HashMap<String, Object>() {
                {
                    put(Mqtt2MqttWorker.DEVICE_MIRRORED_ID, mqtt2MqttConfiguration.getDeviceId());
                    put(Mqtt2MqttWorker.DEVICE_MIRRORED_TOPIC_NUMBER, configuredTopicMap.size());
                    put(Mqtt2MqttWorker.DEVICE_MIRRORED_TOPIC_LIST, configuredTopicMap.keySet().stream().map(String::valueOf).collect(Collectors.joining("-", "{", "}")));
                    put(Mqtt2MqttWorker.DEVICE_MIRRORED_BROKER_ENDPOINT_CALLBACK_METADATA, getPhysicalThingMqttBrokerUrl());
                }
            });

        }catch (Exception e){
            this.mqtt2MqttWorker.notifyDeviceMirroringError(this.mqtt2MqttConfiguration.getDeviceId(), e.getLocalizedMessage());
            throw new WldtMqttModuleException(e.getLocalizedMessage());
        }

    }

    private void initTopicManagement() {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_TOPIC_REGISTRATION_TIME);

        try {

            //Check if topics are correctly configured
            if(this.mqtt2MqttConfiguration.getTopicList() != null && this.mqtt2MqttConfiguration.getTopicList().size() > 0){

                //Analyze each configured topic
                this.mqtt2MqttConfiguration.getTopicList().forEach(mqttTopicDescriptor -> {
                    try{
                        //Check if Id and topic are correctly configured
                        if(mqttTopicDescriptor.getId() != null && mqttTopicDescriptor.getId().length() > 0 && mqttTopicDescriptor.getTopic() != null && mqttTopicDescriptor.getTopic().length() > 0) {

                            //Build final target topic in order to apply templates (if required)
                            String targetTopic = null;

                            if(mqttTopicDescriptor.getResourceId() != null && mqttTopicDescriptor.getResourceId().length() > 0)
                                targetTopic = TopicTemplateManager.getTopicForDeviceResource(mqttTopicDescriptor.getTopic(), this.mqtt2MqttConfiguration.getDeviceId(), mqttTopicDescriptor.getResourceId());
                            else
                                targetTopic = TopicTemplateManager.getTopicForDevice(mqttTopicDescriptor.getTopic(), this.mqtt2MqttConfiguration.getDeviceId());

                            IMqttClient targetMqttClient = null;

                            //Select the right mqttClient according to topic configuration
                            if (mqttTopicDescriptor.getType().equals(MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING))
                                targetMqttClient = physicalDeviceMqttBrokerClient;
                            else if (mqttTopicDescriptor.getType().equals(MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_INCOMING))
                                targetMqttClient = digitalTwinMqttBrokerClient;

                            if (targetMqttClient != null) {

                                //Register to the target MQTT topic on the right client
                                registerToMqttTopic(targetMqttClient, targetTopic, this::handleIncomingMessage);

                                //Save the configured topic and its descriptor in order to properly use the configuration when a new message is received
                                configuredTopicMap.put(targetTopic, mqttTopicDescriptor);

                                //Notify that the resource (topic in that case) has been correctly mirrored
                                String finalTargetTopic = targetTopic;
                                this.mqtt2MqttWorker.notifyResourceMirrored((mqttTopicDescriptor.getResourceId() == null ? mqttTopicDescriptor.getId() : mqttTopicDescriptor.getResourceId()), new HashMap<String, Object>() {
                                    {
                                        put(Mqtt2MqttWorker.RESOURCE_MIRRORED_TOPIC_CALLBACK_METADATA, finalTargetTopic);
                                        put(Mqtt2MqttWorker.DEVICE_MIRRORED_BROKER_ENDPOINT_CALLBACK_METADATA, getPhysicalThingMqttBrokerUrl());
                                    }
                                });

                                logger.info("{} Registered to Configured topic with id: {} and value: {}", TAG, mqttTopicDescriptor.getId(), targetTopic);
                            }
                            else
                                logger.error("Topic Configuration Error (Type not found !) ! Configured record: {}", mqttTopicDescriptor);

                        }  else
                            logger.error("Topic Configuration Error ! Configured record: {}", mqttTopicDescriptor);

                    }catch (Exception e){
                        String errorMsg = String.format("%s Error registering to target topic: %s ! Exception: %s", TAG, mqttTopicDescriptor.getTopic(), e.getLocalizedMessage());
                        logger.error(errorMsg);
                        mqtt2MqttWorker.notifyResourceMirroringError(mqtt2MqttConfiguration.getDeviceId(), errorMsg);
                        isMirroringCompleted = false;
                    }
                });

            }else
                logger.info("Topic List Configuration = null or Empty ! Avoiding registering to the topics ...");

        } finally {
            if(context != null)
                context.stop();
        }

    }

    /**
     * Initialize the MQTT Consumer to send and receive data and command from and to the Physical Thing
     */
    public void initPhysicalDeviceMqttBrokerClient() throws MqttException {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_INCOMING_CLIENT_SETUP_TIME);

        try {

            physicalDeviceMqttBrokerClient = new MqttClient(getPhysicalThingMqttBrokerUrl(),String.format("%s%s", this.wldtId, "physical"), new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            physicalDeviceMqttBrokerClient.connect(options);

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
    public void initDigitalTwinMqttBrokerClient() throws MqttException {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_OUTGOING_CLIENT_SETUP_TIME);

        try {

            digitalTwinMqttBrokerClient = new MqttClient(getDestinationMqttBrokerUrl(),String.format("%s%s", this.wldtId, "digital"), new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            digitalTwinMqttBrokerClient.connect(options);

            logger.info("{} OUTGOING MQTT Client Connected to {}", TAG, getDestinationMqttBrokerUrl());

        }catch (Exception e){
            throw e;
        } finally {
            if(context != null)
                context.stop();
        }
    }

    private void registerToMqttTopic(IMqttClient mqttClient, String topic, IMqttMessageListener mqttMessageListener) throws WldtMqttModuleException, MqttException {

        if(mqttClient != null && mqttClient.isConnected())
            mqttClient.subscribe(topic, mqttMessageListener);
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

    private String getOutgoingTopic(String originalTopic){
        if(mqtt2MqttConfiguration.getDestinationBrokerBaseTopic() == null ||
                mqtt2MqttConfiguration.getDestinationBrokerBaseTopic().equals("null") ||
                mqtt2MqttConfiguration.getDestinationBrokerBaseTopic().length() == 0)
            return originalTopic;
        else
            return String.format("%s/%s", mqtt2MqttConfiguration.getDestinationBrokerBaseTopic(), originalTopic);
    }

    private void handleIncomingMessage(String topic, MqttMessage msg) {

        Timer.Context context = WldtMetricsManager.getInstance().getMqttModuleTimerContext(WldtMetricsManager.MQTT_FORWARD_TIME);

        try {

            byte[] payload = msg.getPayload();
            WldtMetricsManager.getInstance().measureMqttIncomingPayloadSizeMetric(payload.length);
            WldtMetricsManager.getInstance().measureMqttIncomingTelemetryPayload(payload.length);

            logger.debug("{} DEVICE TOPIC ({}) Message Received: {}", TAG, topic, new String(payload));

            //TODO rename that field -> Forwarding Enabled ?
            if(mqtt2MqttConfiguration.getOutgoingPublishingEnabled()){

                MqttTopicDescriptor configuredMqttTopicDescriptor = configuredTopicMap.get(topic);

                //TODO add processing pipeline support
                if(configuredMqttTopicDescriptor != null){
                    if(configuredMqttTopicDescriptor.getType().equals(MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING))
                        publishData(digitalTwinMqttBrokerClient, getOutgoingTopic(topic), payload);
                    else if(configuredMqttTopicDescriptor.getType().equals(MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_INCOMING))
                        publishData(physicalDeviceMqttBrokerClient, getOutgoingTopic(topic), payload);

                }
                else
                    logger.error("{} MqttTopicDescriptor not found ! ERROR FORWARDING TELEMETRY MESSAGE TO TOPIC: {}", TAG, topic);

            }

        }catch (Exception e){
            logger.error("{} ERROR FORWARDING TELEMETRY MESSAGE TO TOPIC: {}", TAG, topic);
        } finally {
            if(context != null)
                context.stop();
        }
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
