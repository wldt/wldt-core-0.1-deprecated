package it.unimore.wldt.test.mqtt;

import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipeline;
import it.unimore.dipi.iot.wldt.processing.step.IdentityProcessingStep;
import it.unimore.dipi.iot.wldt.worker.MirroringListener;
import it.unimore.dipi.iot.wldt.worker.mqtt.Mqtt2MqttConfiguration;
import it.unimore.dipi.iot.wldt.worker.mqtt.Mqtt2MqttWorker;
import it.unimore.dipi.iot.wldt.worker.mqtt.MqttTopicDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

/**
 * Demo of a WLDT enabled Digital Twin that mirrors an MQTT IoT Device
 * received telemetry data from the physical asset and command from
 * external applications according to the following schema:
 *
 * Telemetry:
 *
 * DEVICE ---- [msg] ----> BROKER-A ----> (DT) ---- [msg] ---- BROKER-B ----> CONSUMER(s)
 *
 * Commands:
 *
 * DEVICE <---- [msg] ---- BROKER-A <---- (DT) <---- [msg] ---- BROKER-B <---- APP(s)
 *
 * @author : Marco Picone, Ph.D. (marco.picone@unimore.it)
 * @created: 21/05/2021
 * @project: WLDT - MQTT Example
 */
public class WldtExampleMqttProcess {

    private static final String TAG = "[WLDT-Process]";

    private static final Logger logger = LoggerFactory.getLogger(WldtExampleMqttProcess.class);

    private static final String DEMO_TEMPERATURE_TOPIC_ID = "temperature_topic";
    private static final String DEMO_TEMPERATURE_RESOURCE_ID = "temperature";

    private static final String DEMO_COMMAND_TOPIC_ID = "command_topic";
    private static final String DEMO_COMMAND_RESOURCE_ID = "default_command_channel";

    private static final String SOURCE_BROKER_ADDRESS = "127.0.0.1";
    private static final int SOURCE_BROKER_PORT = 1883;

    private static final String DESTINATION_BROKER_ADDRESS = "127.0.0.1";
    private static final int DESTINATION_BROKER_PORT = 1884;

    private static final String DT_PREFIX = "wldt";

    private static final String DEVICE_ID = "com:iot:dummy:dummyMqttDevice001";

    public static void main(String[] args)  {

        try{

            logger.info("{} Initializing WLDT-Engine ... ", TAG);

            //Example loading everything from the configuration file
            //WldtEngine wldtEngine = new WldtEngine();
            //wldtEngine.startWorkers();

            //Manual creation of the WldtConfiguration
            WldtConfiguration wldtConfiguration = new WldtConfiguration();
            wldtConfiguration.setDeviceNameSpace("it.unimore.dipi.things");
            wldtConfiguration.setWldtBaseIdentifier("wldt");
            wldtConfiguration.setWldtStartupTimeSeconds(10);
            wldtConfiguration.setApplicationMetricsEnabled(false);

            WldtEngine wldtEngine = new WldtEngine(wldtConfiguration);

            Mqtt2MqttWorker mqtt2MqttWorker = new Mqtt2MqttWorker(wldtEngine.getWldtId(), getMqttComplexProtocolConfiguration());

            //Add Processing Pipeline for target topics
            mqtt2MqttWorker.addTopicProcessingPipeline(DEMO_TEMPERATURE_TOPIC_ID,
                    new ProcessingPipeline(
                            new IdentityProcessingStep(),
                            new MqttAverageProcessingStep(),
                            new MqttTopicChangeStep()
                    )
            );

            mqtt2MqttWorker.addTopicProcessingPipeline(DEMO_COMMAND_TOPIC_ID,
                    new ProcessingPipeline(
                            new IdentityProcessingStep(),
                            new MqttPayloadChangeStep(),
                            new MqttCommandTopicChangeStep()
                    )
            );

            //Add Mirroring Listener
            mqtt2MqttWorker.addMirroringListener(new MirroringListener() {

                @Override
                public void onDeviceMirrored(String deviceId, Map<String, Object> metadata) {
                    logger.info("onDeviceMirrored() callback ! DeviceId: {} -> Metadata: {}", deviceId, metadata);
                }

                @Override
                public void onDeviceMirroringError(String deviceId, String errorMsg) {
                    logger.info("onDeviceMirroringError() callback ! DeviceId: {} -> ErrorMsg: {}", deviceId, errorMsg);
                }

                @Override
                public void onResourceMirrored(String resourceId, Map<String, Object> metadata) {
                    logger.info("onResourceMirrored() callback ! ResourceId: {} -> Metadata: {}", resourceId, metadata);
                }

                @Override
                public void onResourceMirroringError(String resourceId, String errorMsg) {
                    logger.info("onResourceMirroringError() callback ! ResourceId: {} -> ErrorMsg: {}", resourceId, errorMsg);
                }

            });

            wldtEngine.addNewWorker(mqtt2MqttWorker);
            wldtEngine.startWorkers();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static Mqtt2MqttConfiguration getMqttComplexProtocolConfiguration(){

        //Configuration associated to the MQTT experimental use-case available in the dedicated project
        //Demo Telemetry topic -> telemetry/com:iot:dummy:dummyMqttDevice001/resource/dummy_string_resource

        Mqtt2MqttConfiguration mqtt2MqttConfiguration = new Mqtt2MqttConfiguration();

        mqtt2MqttConfiguration.setDtPublishingQoS(0);
        mqtt2MqttConfiguration.setBrokerAddress(SOURCE_BROKER_ADDRESS);
        mqtt2MqttConfiguration.setBrokerPort(SOURCE_BROKER_PORT);
        mqtt2MqttConfiguration.setDestinationBrokerAddress(DESTINATION_BROKER_ADDRESS);
        mqtt2MqttConfiguration.setDestinationBrokerPort(DESTINATION_BROKER_PORT);
        mqtt2MqttConfiguration.setDeviceId(DEVICE_ID);

        //If Required Specify the ClientId
        //mqtt2MqttConfiguration.setBrokerClientId("physicalBrokerTestClientId");
        //mqtt2MqttConfiguration.setDestinationBrokerClientId("digitalBrokerTestClientId");

        //Specify Topic List Configuration
        mqtt2MqttConfiguration.setTopicList(
                Arrays.asList(
                        new MqttTopicDescriptor(DEMO_TEMPERATURE_TOPIC_ID,
                                DEMO_TEMPERATURE_RESOURCE_ID,
                                "telemetry/{{device_id}}/resource/{{resource_id}}",
                                MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING),
                        new MqttTopicDescriptor(DEMO_COMMAND_TOPIC_ID,
                                DEMO_COMMAND_RESOURCE_ID,
                                "command/{{device_id}}",
                                MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_INCOMING)
                )
        );

        return mqtt2MqttConfiguration;
    }

}

