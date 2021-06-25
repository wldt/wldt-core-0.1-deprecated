package it.unimore.dipi.iot.wldt.worker.mqtt;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 25/06/2021 - 17:05
 */
public enum MqttQosLevel {

    MQTT_QOS_0(0),
    MQTT_QOS_1(1),
    MQTT_QOS_2(2);

    public final int qosValue;

    MqttQosLevel(int qosValue) {
        this.qosValue = qosValue;
    }

}
