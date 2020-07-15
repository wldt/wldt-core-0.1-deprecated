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

    private boolean outgoingClientRetainedMessages = false;

    private String destinationBrokerAddress;

    private int destinationBrokerPort;

    private String destinationBrokerBaseTopic;

    private String deviceId = null;

    private List<String> resourceIdList = null;

    private String deviceTelemetryTopic = null;

    private String resourceTelemetryTopic = null;

    private String eventTopic = null;

    private String commandRequestTopic = null;

    private String commandResponseTopic = null;

    private String brokerAddress = "127.0.0.1";

    private int brokerPort = 1883;

    private boolean brokerLocal = true;

    private List<String> deviceTelemetryProcessingStepList = null;

    private List<String> resourceTelemetryProcessingStepList = null;

    private List<String> eventProcessingStepList = null;

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

    public List<String> getDeviceTelemetryProcessingStepList() {
        return deviceTelemetryProcessingStepList;
    }

    public void setDeviceTelemetryProcessingStepList(List<String> deviceTelemetryProcessingStepList) {
        this.deviceTelemetryProcessingStepList = deviceTelemetryProcessingStepList;
    }

    public List<String> getResourceTelemetryProcessingStepList() {
        return resourceTelemetryProcessingStepList;
    }

    public void setResourceTelemetryProcessingStepList(List<String> resourceTelemetryProcessingStepList) {
        this.resourceTelemetryProcessingStepList = resourceTelemetryProcessingStepList;
    }

    public List<String> getEventProcessingStepList() {
        return eventProcessingStepList;
    }

    public void setEventProcessingStepList(List<String> eventProcessingStepList) {
        this.eventProcessingStepList = eventProcessingStepList;
    }

    public List<String> getCommandRequestProcessingStepList() {
        return commandRequestProcessingStepList;
    }

    public void setCommandRequestProcessingStepList(List<String> commandRequestProcessingStepList) {
        this.commandRequestProcessingStepList = commandRequestProcessingStepList;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MqttProtocolConfiguration{");
        sb.append("mqttDestinationBrokerAddress='").append(destinationBrokerAddress).append('\'');
        sb.append(", mqttDestinationBrokerPort=").append(destinationBrokerPort);
        sb.append(", mqttDestinationBrokerBaseTopic='").append(destinationBrokerBaseTopic).append('\'');
        sb.append(", mqttOutgoingClientQoS=").append(outgoingClientQoS);
        sb.append(", mqttOutgoingClientRetainedMessages=").append(outgoingClientRetainedMessages);
        sb.append(", mqttOutgoingPublishingEnabled=").append(outgoingPublishingEnabled);
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", resourceList=").append(resourceIdList);
        sb.append(", telemetryDeviceTopic='").append(deviceTelemetryTopic).append('\'');
        sb.append(", telemetryResourceTopic='").append(resourceTelemetryTopic).append('\'');
        sb.append(", eventTopic='").append(eventTopic).append('\'');
        sb.append(", commandRequestTopic='").append(commandRequestTopic).append('\'');
        sb.append(", commandResponseTopic='").append(commandResponseTopic).append('\'');
        sb.append(", brokerAddress='").append(brokerAddress).append('\'');
        sb.append(", brokerPortTopic=").append(brokerPort);
        sb.append(", brokerLocal=").append(brokerLocal);
        sb.append('}');
        return sb.toString();
    }
}
