package it.unimore.dipi.iot.wldt.worker.mqtt;

import it.unimore.dipi.iot.wldt.processing.PipelineData;

public class MqttPipelineData implements PipelineData {

    private String topic;
    private byte[] payload;
    private MqttTopicDescriptor mqttTopicDescriptor = null;
    private boolean isRetained = false;

    public MqttPipelineData() {
    }

    public MqttPipelineData(String topic, MqttTopicDescriptor mqttTopicDescriptor, byte[] payload, boolean isRetained) {
        this.topic = topic;
        this.payload = payload;
        this.mqttTopicDescriptor = mqttTopicDescriptor;
        this.isRetained = isRetained;
    }

    public MqttTopicDescriptor getMqttTopicDescriptor() {
        return mqttTopicDescriptor;
    }

    public void setMqttTopicDescriptor(MqttTopicDescriptor mqttTopicDescriptor) {
        this.mqttTopicDescriptor = mqttTopicDescriptor;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public boolean isRetained() {
        return isRetained;
    }

    public void setRetained(boolean retained) {
        isRetained = retained;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MqttPipelineData{");
        sb.append("topic='").append(topic).append('\'');
        sb.append(", payload=");
        if (payload == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < payload.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(payload[i]);
            sb.append(']');
        }
        sb.append(", mqttTopicDescriptor=").append(mqttTopicDescriptor);
        sb.append(", isRetained=").append(isRetained);
        sb.append('}');
        return sb.toString();
    }
}
