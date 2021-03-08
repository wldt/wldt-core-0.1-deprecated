package it.unimore.dipi.iot.wldt.process;

import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.worker.MirroringListener;
import it.unimore.dipi.iot.wldt.worker.mqtt.Mqtt2MqttConfiguration;
import it.unimore.dipi.iot.wldt.worker.mqtt.Mqtt2MqttWorker;
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
            mqtt2MqttWorker.addMirroringListener(new MirroringListener() {

                @Override
                public void onPhysicalDeviceMirrored(String deviceId, Map<String, Object> metadata) {
                    logger.info("onPhysicalDeviceMirrored() callback ! DeviceId: {} -> Metadata: {}", deviceId, metadata);
                }

                @Override
                public void onPhysicalDeviceMirroringError(String deviceId, String errorMsg) {
                    logger.error("onPhysicalDeviceMirroringError() callback ! Error Mirroring Device: {} Reason: {}", deviceId, errorMsg);
                }

                @Override
                public void onPhysicalResourceMirrored(String resourceId, Map<String, Object> metadata) {
                    logger.info("onPhysicalResourceMirrored() callback ! ResourceId: {} -> Metadata: {}", resourceId, metadata);
                }

                @Override
                public void onPhysicalResourceMirroringError(String deviceId, String errorMsg) {
                    logger.error("onPhysicalResourceMirroringError() callback ! Error Mirroring Resource: {} Reason: {}", deviceId, errorMsg);
                }
            });

            wldtEngine.addNewWorker(mqtt2MqttWorker);
            wldtEngine.startWorkers();

        }catch (Exception | WldtConfigurationException e){
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
        mqtt2MqttConfiguration.setDestinationBrokerBaseTopic("wldt");
        mqtt2MqttConfiguration.setDeviceId("com:iot:dummy:dummyMqttDevice001");
        mqtt2MqttConfiguration.setResourceIdList(Arrays.asList("dummy_string_resource"));
        mqtt2MqttConfiguration.setDeviceTelemetryTopic("telemetry/{{device_id}}");
        mqtt2MqttConfiguration.setResourceTelemetryTopic("telemetry/{{device_id}}/resource/{{resource_id}}");
        mqtt2MqttConfiguration.setEventTopic("events/{{device_id}}");
        mqtt2MqttConfiguration.setCommandRequestTopic("commands/{{device_id}}/request");
        mqtt2MqttConfiguration.setCommandResponseTopic("commands/{{device_id}}/response");
        mqtt2MqttConfiguration.setBrokerAddress("127.0.0.1");
        mqtt2MqttConfiguration.setBrokerPort(1883);

        return mqtt2MqttConfiguration;
    }

}
