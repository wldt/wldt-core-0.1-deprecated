package it.unimore.dipi.iot.wldt.process;

import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.worker.mqtt.Mqtt2MqttConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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
            WldtEngine wldtEngine = new WldtEngine();
            wldtEngine.startWorkers();

            //Manual creation of the WldtConfiguration
            //WldtConfiguration wldtConfiguration = new WldtConfiguration();
            //wldtConfiguration.setDeviceNameSpace("it.unimore.dipi.things");
            //wldtConfiguration.setWldtBaseIdentifier("wldt");
            //wldtConfiguration.setWldtStartupTimeSeconds(10);
            //wldtConfiguration.setApplicationMetricsEnabled(false);

            //WldtEngine wldtEngine = new WldtEngine(wldtConfiguration);
            //wldtEngine.addNewWorker(new WldtMqttWorker(wldtEngine.getWldtId(), getMqttComplexProtocolConfiguration()));
            //wldtEngine.startWorkers();

        }catch (Exception | WldtConfigurationException e){
            e.printStackTrace();
        }
    }

    private static Mqtt2MqttConfiguration getMqttComplexProtocolConfiguration(){

        Mqtt2MqttConfiguration mqtt2MqttConfiguration = new Mqtt2MqttConfiguration();

        mqtt2MqttConfiguration.setOutgoingClientQoS(0);
        mqtt2MqttConfiguration.setDestinationBrokerAddress("127.0.0.1");
        mqtt2MqttConfiguration.setDestinationBrokerPort(1884);
        mqtt2MqttConfiguration.setDestinationBrokerBaseTopic("wldt");
        mqtt2MqttConfiguration.setDeviceId("it.unimore.dipi.things:dummy_device:20a23b54-0f03-4f07-833b-97b904ada0f9");
        mqtt2MqttConfiguration.setResourceIdList(Arrays.asList("dummy_sensor:d5800329-6306-4cfb-b036-459211ef0a56-0", "dummy_sensor:5fd5ae51-9875-4618-9ef8-34f35de2c812-1"));
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
