package it.unimore.wldt.test.mqtt;

import it.unimore.dipi.iot.wldt.worker.mqtt.Mqtt2MqttManager;
import it.unimore.dipi.iot.wldt.worker.mqtt.MqttTopicDescriptor;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 03/04/2021 - 16:39
 */
public class TopicsLookupTester {

    private static final String DEVICE_ID = "testDeviceId";

    private Map<String, MqttTopicDescriptor> getTopicMap(){

        Map<String, MqttTopicDescriptor> mqttTopicMap = new HashMap<>();

        mqttTopicMap.put("TelemetryChannel",new MqttTopicDescriptor("DummyStringResource",
                "dummy_string_resource",
                "telemetry/gps/",
                MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING)
        );

        mqttTopicMap.put("TelemetryWildcardChannel",new MqttTopicDescriptor("DummyStringResource",
                "dummy_string_resource",
                "telemetry/device/+/gps/",
                MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING)
        );

        mqttTopicMap.put("CommandChannel",new MqttTopicDescriptor("DummyStringResource",
                "dummy_command_channel",
                "command///req//#",
                MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING)
        );

        mqttTopicMap.put("batteryTopic", new MqttTopicDescriptor("gpsResource",
                "battery",
                "telemetry/tb6002f533897433c94bd84f1b59ef35d_hub/com.iot.example:VehicleTest05/battery",
                MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING)
        );

        mqttTopicMap.put("gpsTopicInternalWildcard", new MqttTopicDescriptor("gpsResource",
                "gps",
                "test/{{device_id}}/{{resource_id}}",
                MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING)
        );

        return mqttTopicMap;
    }

    @Test
    public void successTopicInternalTemplateLookupTest(){

        Map<String, MqttTopicDescriptor> mqttTopicMap = getTopicMap();

        //Looking for telemetry/gps/
        Optional<MqttTopicDescriptor> telemetryTopicResult = Mqtt2MqttManager
                .lookupTopicDescriptor(DEVICE_ID, mqttTopicMap, "test/testDeviceId/gps");

        Assert.assertNotNull(telemetryTopicResult);
        Assert.assertTrue(telemetryTopicResult.isPresent());
        Assert.assertNotNull(telemetryTopicResult.get());
        Assert.assertEquals(telemetryTopicResult.get(), mqttTopicMap.get("gpsTopicInternalWildcard"));

    }

    @Test
    public void successTelemetryGpsTopicLookupTest(){

        Map<String, MqttTopicDescriptor> mqttTopicMap = getTopicMap();

        //Looking for telemetry/gps/
        Optional<MqttTopicDescriptor> telemetryTopicResult = Mqtt2MqttManager
                .lookupTopicDescriptor(DEVICE_ID, mqttTopicMap, "telemetry/tb6002f533897433c94bd84f1b59ef35d_hub/com.iot.example:VehicleTest05/battery");

        Assert.assertNotNull(telemetryTopicResult);
        Assert.assertTrue(telemetryTopicResult.isPresent());
        Assert.assertNotNull(telemetryTopicResult.get());
        Assert.assertEquals(telemetryTopicResult.get(), mqttTopicMap.get("batteryTopic"));

    }

    @Test
    public void successTelemetryTopicLookupTest(){

        Map<String, MqttTopicDescriptor> mqttTopicMap = getTopicMap();

        //Looking for telemetry/gps/
        Optional<MqttTopicDescriptor> telemetryTopicResult = Mqtt2MqttManager
                .lookupTopicDescriptor(DEVICE_ID, mqttTopicMap, "telemetry/gps/");

        Assert.assertNotNull(telemetryTopicResult);
        Assert.assertTrue(telemetryTopicResult.isPresent());
        Assert.assertNotNull(telemetryTopicResult.get());
        Assert.assertEquals(telemetryTopicResult.get(), mqttTopicMap.get("TelemetryChannel"));

    }

    @Test
    public void errorTelemetryTopicLookupTest(){

        Map<String, MqttTopicDescriptor> mqttTopicMap = getTopicMap();

        //Looking for telemetry/gps/
        Optional<MqttTopicDescriptor> telemetryTopicResult = Mqtt2MqttManager
                .lookupTopicDescriptor(DEVICE_ID, mqttTopicMap, "telemetry/gps/test");

        Assert.assertNotNull(telemetryTopicResult);
        Assert.assertFalse(telemetryTopicResult.isPresent());
    }

    @Test
    public void successWildcardTopicLookupTest1(){

        Map<String, MqttTopicDescriptor> mqttTopicMap = getTopicMap();

        //Looking for command topic
        Optional<MqttTopicDescriptor> commandTopicResult = Mqtt2MqttManager
                .lookupTopicDescriptor(DEVICE_ID, mqttTopicMap, "command///req//modified");

        Assert.assertNotNull(commandTopicResult);
        Assert.assertTrue(commandTopicResult.isPresent());
        Assert.assertNotNull(commandTopicResult.get());
        Assert.assertEquals(commandTopicResult.get(), mqttTopicMap.get("CommandChannel"));

    }

    @Test
    public void successWildcardTopicLookupTest2(){

        Map<String, MqttTopicDescriptor> mqttTopicMap = getTopicMap();

        //Looking for command topic
        Optional<MqttTopicDescriptor> topicResult = Mqtt2MqttManager
                .lookupTopicDescriptor(DEVICE_ID, mqttTopicMap, "telemetry/device/d00001/gps/");

        Assert.assertNotNull(topicResult);
        Assert.assertTrue(topicResult.isPresent());
        Assert.assertNotNull(topicResult.get());
        Assert.assertEquals(topicResult.get(), mqttTopicMap.get("TelemetryWildcardChannel"));

    }

    @Test
    public void successWildcardTopicLookupTest3(){

        Map<String, MqttTopicDescriptor> mqttTopicMap = getTopicMap();

        //Looking for command topic
        Optional<MqttTopicDescriptor> topicResult = Mqtt2MqttManager
                .lookupTopicDescriptor(DEVICE_ID, mqttTopicMap, "telemetry/device/d00002/gps/");

        Assert.assertNotNull(topicResult);
        Assert.assertTrue(topicResult.isPresent());
        Assert.assertNotNull(topicResult.get());
        Assert.assertEquals(topicResult.get(), mqttTopicMap.get("TelemetryWildcardChannel"));

    }

}
