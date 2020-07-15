package it.unimore.dipi.iot.wldt.worker.mqtt;

import it.unimore.dipi.iot.wldt.processing.PipelineData;

public class MqttPipelineData implements PipelineData {

    private String topic;
    private byte[] payload;

    public MqttPipelineData() {
    }

    public MqttPipelineData(String topic, byte[] payload) {
        this.topic = topic;
        this.payload = payload;
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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MqttProcessingInfo{");
        sb.append("topic='").append(topic).append('\'');
        sb.append(", payload=");
        if (payload == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < payload.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(payload[i]);
            sb.append(']');
        }
        sb.append('}');
        return sb.toString();
    }
}
