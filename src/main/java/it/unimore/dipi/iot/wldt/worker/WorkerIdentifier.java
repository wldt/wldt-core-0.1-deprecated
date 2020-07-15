package it.unimore.dipi.iot.wldt.worker;

public enum WorkerIdentifier {

    MQTT_TO_MQTT_MODULE("mqtt"),
    COAP_TO_COAP_MODULE("coap");

    public final String value;

    WorkerIdentifier(String label) {
        this.value = label;
    }
}
