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

    private String destinationBrokerAddress;

    private int destinationBrokerPort;

    private String destinationBrokerClientUsername;

    private String destinationBrokerClientPassword;

    private boolean destinationBrokerLocal = true;

    private boolean destinationBrokerSecureCommunicationRequired = false;

    private String destinationBrokerServerCertPath;

    private String destinationBrokerTlsContext = "TLSv1.2";

    private String destinationBrokerClientId = null;

    private String deviceId = null;

    private String brokerAddress = "127.0.0.1";

    private int brokerPort = 1883;

    private boolean brokerLocal = true;

    private String brokerClientUsername;

    private String brokerClientPassword;

    private boolean brokerSecureCommunicationRequired = false;

    private String brokerServerCertPath;

    private String brokerTlsContext = "TLSv1.2";

    private String brokerClientId = null;

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

    public String getDestinationBrokerClientUsername() {
        return destinationBrokerClientUsername;
    }

    public void setDestinationBrokerClientUsername(String destinationBrokerClientUsername) {
        this.destinationBrokerClientUsername = destinationBrokerClientUsername;
    }

    public String getDestinationBrokerClientPassword() {
        return destinationBrokerClientPassword;
    }

    public void setDestinationBrokerClientPassword(String destinationBrokerClientPassword) {
        this.destinationBrokerClientPassword = destinationBrokerClientPassword;
    }

    public boolean getDestinationBrokerLocal() {
        return destinationBrokerLocal;
    }

    public void setDestinationBrokerLocal(boolean destinationBrokerLocal) {
        this.destinationBrokerLocal = destinationBrokerLocal;
    }

    public String getBrokerClientUsername() {
        return brokerClientUsername;
    }

    public void setBrokerClientUsername(String brokerClientUsername) {
        this.brokerClientUsername = brokerClientUsername;
    }

    public String getBrokerClientPassword() {
        return brokerClientPassword;
    }

    public void setBrokerClientPassword(String brokerClientPassword) {
        this.brokerClientPassword = brokerClientPassword;
    }

    public boolean getDestinationBrokerSecureCommunicationRequired() {
        return destinationBrokerSecureCommunicationRequired;
    }

    public void setDestinationBrokerSecureCommunicationRequired(boolean destinationBrokerSecureCommunicationRequired) {
        this.destinationBrokerSecureCommunicationRequired = destinationBrokerSecureCommunicationRequired;
    }

    public String getDestinationBrokerServerCertPath() {
        return destinationBrokerServerCertPath;
    }

    public void setDestinationBrokerServerCertPath(String destinationBrokerServerCertPath) {
        this.destinationBrokerServerCertPath = destinationBrokerServerCertPath;
    }

    public boolean getBrokerSecureCommunicationRequired() {
        return brokerSecureCommunicationRequired;
    }

    public void setBrokerSecureCommunicationRequired(boolean brokerSecureCommunicationRequired) {
        this.brokerSecureCommunicationRequired = brokerSecureCommunicationRequired;
    }

    public String getBrokerServerCertPath() {
        return brokerServerCertPath;
    }

    public void setBrokerServerCertPath(String brokerServerCertPath) {
        this.brokerServerCertPath = brokerServerCertPath;
    }

    public String getDestinationBrokerTlsContext() {
        return destinationBrokerTlsContext;
    }

    public void setDestinationBrokerTlsContext(String destinationBrokerTlsContext) {
        this.destinationBrokerTlsContext = destinationBrokerTlsContext;
    }

    public String getBrokerTlsContext() {
        return brokerTlsContext;
    }

    public void setBrokerTlsContext(String brokerTlsContext) {
        this.brokerTlsContext = brokerTlsContext;
    }

    public String getBrokerClientId() {
        return brokerClientId;
    }

    public void setBrokerClientId(String brokerClientId) {
        this.brokerClientId = brokerClientId;
    }

    public String getDestinationBrokerClientId() {
        return destinationBrokerClientId;
    }

    public void setDestinationBrokerClientId(String destinationBrokerClientId) {
        this.destinationBrokerClientId = destinationBrokerClientId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Mqtt2MqttConfiguration{");
        sb.append("dtForwardingEnabled=").append(dtForwardingEnabled);
        sb.append(", destinationBrokerAddress='").append(destinationBrokerAddress).append('\'');
        sb.append(", destinationBrokerPort=").append(destinationBrokerPort);
        sb.append(", destinationBrokerClientUsername='").append(destinationBrokerClientUsername).append('\'');
        sb.append(", destinationBrokerClientPassword='").append(destinationBrokerClientPassword).append('\'');
        sb.append(", destinationBrokerLocal=").append(destinationBrokerLocal);
        sb.append(", destinationBrokerSecureCommunicationRequired=").append(destinationBrokerSecureCommunicationRequired);
        sb.append(", destinationBrokerServerCertPath='").append(destinationBrokerServerCertPath).append('\'');
        sb.append(", destinationBrokerTlsContext='").append(destinationBrokerTlsContext).append('\'');
        sb.append(", destinationBrokerClientId='").append(destinationBrokerClientId).append('\'');
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", brokerAddress='").append(brokerAddress).append('\'');
        sb.append(", brokerPort=").append(brokerPort);
        sb.append(", brokerLocal=").append(brokerLocal);
        sb.append(", brokerClientUsername='").append(brokerClientUsername).append('\'');
        sb.append(", brokerClientPassword='").append(brokerClientPassword).append('\'');
        sb.append(", brokerSecureCommunicationRequired=").append(brokerSecureCommunicationRequired);
        sb.append(", brokerServerCertPath='").append(brokerServerCertPath).append('\'');
        sb.append(", brokerTlsContext='").append(brokerTlsContext).append('\'');
        sb.append(", brokerClientId='").append(brokerClientId).append('\'');
        sb.append(", topicList=").append(topicList);
        sb.append('}');
        return sb.toString();
    }
}
