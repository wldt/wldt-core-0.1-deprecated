package it.unimore.dipi.iot.wldt.worker.mqtt;

import java.util.Objects;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 09/03/2021 - 15:48
 */
public class MqttTopicDescriptor {

    public static String MQTT_TOPIC_TYPE_DEVICE_OUTGOING = "device_outgoing";

    public static String MQTT_TOPIC_TYPE_DEVICE_INCOMING = "device_incoming";

    private String id;

    private String resourceId;

    private String topic;

    private String type;

    public MqttTopicDescriptor() {
    }

    public MqttTopicDescriptor(String id, String resourceId, String topic, String type) {
        this.id = id;
        this.resourceId = resourceId;
        this.topic = topic;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MqttTopicDescriptor{");
        sb.append("id='").append(id).append('\'');
        sb.append(", resourceId='").append(resourceId).append('\'');
        sb.append(", topic='").append(topic).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MqttTopicDescriptor that = (MqttTopicDescriptor) o;
        return Objects.equals(id, that.id) && Objects.equals(resourceId, that.resourceId) && Objects.equals(topic, that.topic) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, resourceId, topic, type);
    }
}
