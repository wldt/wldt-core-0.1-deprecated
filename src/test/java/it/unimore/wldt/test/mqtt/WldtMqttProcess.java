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
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin - Java Framework
 */
public class WldtMqttProcess {

    private static final String TAG = "[WLDT-Process]";

    private static final Logger logger = LoggerFactory.getLogger(WldtMqttProcess.class);

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
            mqtt2MqttWorker.addTopicProcessingPipeline("DummyStringResource",
                    new ProcessingPipeline(
                        new IdentityProcessingStep(),
                        new MqttPayloadChangeStep(),
                        new MqttTopicChangeStep()
                    )
            );

            mqtt2MqttWorker.addTopicProcessingPipeline("CommandChannel",
                    new ProcessingPipeline(
                            new IdentityProcessingStep(),
                            new MqttPayloadChangeStep()
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

        mqtt2MqttConfiguration.setOutgoingClientQoS(0);
        mqtt2MqttConfiguration.setDestinationBrokerAddress("127.0.0.1");
        mqtt2MqttConfiguration.setDestinationBrokerPort(1884);
        mqtt2MqttConfiguration.setDtTopicPrefix("wldt");
        mqtt2MqttConfiguration.setDeviceId("com:iot:dummy:dummyMqttDevice001");
        mqtt2MqttConfiguration.setBrokerAddress("127.0.0.1");
        mqtt2MqttConfiguration.setBrokerPort(1883);

        //Specify Topic List Configuration
        mqtt2MqttConfiguration.setTopicList(
                Arrays.asList(
                        new MqttTopicDescriptor("DummyStringResource",
                                "dummy_string_resource",
                                "telemetry/{{device_id}}/resource/{{resource_id}}",
                                MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING),
                        new MqttTopicDescriptor("CommandChannel",
                                "default_command_channel",
                                "command/{{device_id}}",
                                MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_INCOMING)
                )
        );

        //mqtt2MqttConfiguration.setResourceIdList(Arrays.asList("dummy_string_resource"));
        //mqtt2MqttConfiguration.setDeviceTelemetryTopic("telemetry/{{device_id}}");
        //mqtt2MqttConfiguration.setResourceTelemetryTopic("telemetry/{{device_id}}/resource/{{resource_id}}");
        //mqtt2MqttConfiguration.setEventTopic("events/{{device_id}}");
        //mqtt2MqttConfiguration.setCommandRequestTopic("commands/{{device_id}}/request");
        //mqtt2MqttConfiguration.setCommandResponseTopic("commands/{{device_id}}/response");

        return mqtt2MqttConfiguration;
    }

}
