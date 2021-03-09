package it.unimore.dipi.iot.wldt.worker.mqtt;

import it.unimore.dipi.iot.wldt.worker.WldtWorkerConfiguration;
import java.util.List;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 25/05/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class Mqtt2MqttConfiguration implements WldtWorkerConfiguration {

    private boolean outgoingPublishingEnabled = true;

    private int outgoingClientQoS = 0;

    //TODO Check if it is correctly used or can be improved
    private boolean outgoingClientRetainedMessages = false;

    private String destinationBrokerAddress;

    private int destinationBrokerPort;

    private String destinationBrokerBaseTopic;

    private String deviceId = null;

    private String brokerAddress = "127.0.0.1";

    private int brokerPort = 1883;

    private boolean brokerLocal = true;

    private List<MqttTopicDescriptor> topicList;

    @Deprecated
    private List<String> resourceIdList = null;

    @Deprecated
    private String deviceTelemetryTopic = null;

    @Deprecated
    private String resourceTelemetryTopic = null;

    @Deprecated
    private String eventTopic = null;

    @Deprecated
    private String commandRequestTopic = null;

    @Deprecated
    private String commandResponseTopic = null;

    @Deprecated
    private List<String> deviceTelemetryProcessingStepList = null;

    @Deprecated
    private List<String> resourceTelemetryProcessingStepList = null;

    @Deprecated
    private List<String> eventProcessingStepList = null;

    @Deprecated
    private List<String> commandRequestProcessingStepList = null;

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

    public String getDestinationBrokerBaseTopic() {
        return destinationBrokerBaseTopic;
    }

    public void setDestinationBrokerBaseTopic(String destinationBrokerBaseTopic) {
        this.destinationBrokerBaseTopic = destinationBrokerBaseTopic;
    }

    public int getOutgoingClientQoS() {
        return outgoingClientQoS;
    }

    public void setOutgoingClientQoS(int outgoingClientQoS) {
        this.outgoingClientQoS = outgoingClientQoS;
    }

    public boolean getOutgoingClientRetainedMessages() {
        return outgoingClientRetainedMessages;
    }

    public void setOutgoingClientRetainedMessages(boolean outgoingClientRetainedMessages) {
        this.outgoingClientRetainedMessages = outgoingClientRetainedMessages;
    }

    public boolean getOutgoingPublishingEnabled() {
        return outgoingPublishingEnabled;
    }

    public void setOutgoingPublishingEnabled(boolean outgoingPublishingEnabled) {
        this.outgoingPublishingEnabled = outgoingPublishingEnabled;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<String> getResourceIdList() {
        return resourceIdList;
    }

    public void setResourceIdList(List<String> resourceIdList) {
        this.resourceIdList = resourceIdList;
    }

    public String getDeviceTelemetryTopic() {
        return deviceTelemetryTopic;
    }

    public void setDeviceTelemetryTopic(String deviceTelemetryTopic) {
        this.deviceTelemetryTopic = deviceTelemetryTopic;
    }

    public String getResourceTelemetryTopic() {
        return resourceTelemetryTopic;
    }

    public void setResourceTelemetryTopic(String resourceTelemetryTopic) {
        this.resourceTelemetryTopic = resourceTelemetryTopic;
    }

    public String getEventTopic() {
        return eventTopic;
    }

    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
    }

    public String getCommandRequestTopic() {
        return commandRequestTopic;
    }

    public void setCommandRequestTopic(String commandRequestTopic) {
        this.commandRequestTopic = commandRequestTopic;
    }

    public String getCommandResponseTopic() {
        return commandResponseTopic;
    }

    public void setCommandResponseTopic(String commandResponseTopic) {
        this.commandResponseTopic = commandResponseTopic;
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

    @Deprecated
    public List<String> getDeviceTelemetryProcessingStepList() {
        return deviceTelemetryProcessingStepList;
    }

    @Deprecated
    public void setDeviceTelemetryProcessingStepList(List<String> deviceTelemetryProcessingStepList) {
        this.deviceTelemetryProcessingStepList = deviceTelemetryProcessingStepList;
    }

    @Deprecated
    public List<String> getResourceTelemetryProcessingStepList() {
        return resourceTelemetryProcessingStepList;
    }

    @Deprecated
    public void setResourceTelemetryProcessingStepList(List<String> resourceTelemetryProcessingStepList) {
        this.resourceTelemetryProcessingStepList = resourceTelemetryProcessingStepList;
    }

    @Deprecated
    public List<String> getEventProcessingStepList() {
        return eventProcessingStepList;
    }

    @Deprecated
    public void setEventProcessingStepList(List<String> eventProcessingStepList) {
        this.eventProcessingStepList = eventProcessingStepList;
    }

    @Deprecated
    public List<String> getCommandRequestProcessingStepList() {
        return commandRequestProcessingStepList;
    }

    @Deprecated
    public void setCommandRequestProcessingStepList(List<String> commandRequestProcessingStepList) {
        this.commandRequestProcessingStepList = commandRequestProcessingStepList;
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
        sb.append("outgoingPublishingEnabled=").append(outgoingPublishingEnabled);
        sb.append(", outgoingClientQoS=").append(outgoingClientQoS);
        sb.append(", outgoingClientRetainedMessages=").append(outgoingClientRetainedMessages);
        sb.append(", destinationBrokerAddress='").append(destinationBrokerAddress).append('\'');
        sb.append(", destinationBrokerPort=").append(destinationBrokerPort);
        sb.append(", destinationBrokerBaseTopic='").append(destinationBrokerBaseTopic).append('\'');
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", brokerAddress='").append(brokerAddress).append('\'');
        sb.append(", brokerPort=").append(brokerPort);
        sb.append(", brokerLocal=").append(brokerLocal);
        sb.append(", topicList=").append(topicList);
        sb.append(", resourceIdList=").append(resourceIdList);
        sb.append(", deviceTelemetryTopic='").append(deviceTelemetryTopic).append('\'');
        sb.append(", resourceTelemetryTopic='").append(resourceTelemetryTopic).append('\'');
        sb.append(", eventTopic='").append(eventTopic).append('\'');
        sb.append(", commandRequestTopic='").append(commandRequestTopic).append('\'');
        sb.append(", commandResponseTopic='").append(commandResponseTopic).append('\'');
        sb.append(", deviceTelemetryProcessingStepList=").append(deviceTelemetryProcessingStepList);
        sb.append(", resourceTelemetryProcessingStepList=").append(resourceTelemetryProcessingStepList);
        sb.append(", eventProcessingStepList=").append(eventProcessingStepList);
        sb.append(", commandRequestProcessingStepList=").append(commandRequestProcessingStepList);
        sb.append('}');
        return sb.toString();
    }
}
