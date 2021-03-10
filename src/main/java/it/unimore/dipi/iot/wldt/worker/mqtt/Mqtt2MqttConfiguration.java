package it.unimore.dipi.iot.wldt.worker.mqtt;

import it.unimore.dipi.iot.wldt.worker.WldtWorkerConfiguration;
import java.util.List;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 25/05/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class Mqtt2MqttConfiguration implements WldtWorkerConfiguration {

    private boolean dtForwardingEnabled = true;

    private int dtPublishingQoS = 0;

    private String destinationBrokerAddress;

    private int destinationBrokerPort;

    /**
     * It is applied both for outgoing and incoming topics
     */
    private String dtTopicPrefix;

    private String deviceId = null;

    private String brokerAddress = "127.0.0.1";

    private int brokerPort = 1883;

    private boolean brokerLocal = true;

    private List<MqttTopicDescriptor> topicList;

    public Mqtt2MqttConfiguration() {
    }

    public String getDestinationBrokerAddress() {
        return destinationBrokerAddress;
    }

    public void setDestinationBrokerAddress(String destinationBrokerAddress) {
        this.destinationBrokerAddress = destinationBrokerAddress;
    }

    public int getDestinationBrokerPort() {
        return destinationBrokerPort;
    }

    public void setDestinationBrokerPort(int destinationBrokerPort) {
        this.destinationBrokerPort = destinationBrokerPort;
    }

    public String getDtTopicPrefix() {
        return dtTopicPrefix;
    }

    public void setDtTopicPrefix(String dtTopicPrefix) {
        this.dtTopicPrefix = dtTopicPrefix;
    }

    public int getDtPublishingQoS() {
        return dtPublishingQoS;
    }

    public void setDtPublishingQoS(int dtPublishingQoS) {
        this.dtPublishingQoS = dtPublishingQoS;
    }

    public boolean getDtForwardingEnabled() {
        return dtForwardingEnabled;
    }

    public void setDtForwardingEnabled(boolean dtForwardingEnabled) {
        this.dtForwardingEnabled = dtForwardingEnabled;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getBrokerAddress() {
        return brokerAddress;
    }

    public void setBrokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
    }

    public int getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(int brokerPort) {
        this.brokerPort = brokerPort;
    }

    public boolean getBrokerLocal() {
        return brokerLocal;
    }

    public void setBrokerLocal(boolean brokerLocal) {
        this.brokerLocal = brokerLocal;
    }

    public List<MqttTopicDescriptor> getTopicList() {
        return topicList;
    }

    public void setTopicList(List<MqttTopicDescriptor> topicList) {
        this.topicList = topicList;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Mqtt2MqttConfiguration{");
        sb.append("dtForwardingEnabled=").append(dtForwardingEnabled);
        sb.append(", dtPublishingQoS=").append(dtPublishingQoS);
        sb.append(", destinationBrokerAddress='").append(destinationBrokerAddress).append('\'');
        sb.append(", destinationBrokerPort=").append(destinationBrokerPort);
        sb.append(", dtTopicPrefix='").append(dtTopicPrefix).append('\'');
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", brokerAddress='").append(brokerAddress).append('\'');
        sb.append(", brokerPort=").append(brokerPort);
        sb.append(", brokerLocal=").append(brokerLocal);
        sb.append(", topicList=").append(topicList);
        sb.append('}');
        return sb.toString();
    }
}
