package it.unimore.dipi.iot.wldt.worker.coap;

import it.unimore.dipi.iot.wldt.worker.WldtWorkerConfiguration;

import java.util.List;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 25/05/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class Coap2CoapConfiguration implements WldtWorkerConfiguration {

    private boolean cacheEnabled = false;

    private boolean resourceDiscovery = false;

    private List<String> resourceList = null;

    private String deviceAddress = null;

    private int devicePort = 5683;

    private int deviceSecurePort = 5684;

    public Coap2CoapConfiguration() {
    }

    public boolean getCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean coapCacheEnabled) {
        this.cacheEnabled = coapCacheEnabled;
    }

    public boolean isResourceDiscovery() {
        return resourceDiscovery;
    }

    public void setResourceDiscovery(boolean resourceDiscovery) {
        this.resourceDiscovery = resourceDiscovery;
    }

    public boolean getResourceDiscovery() {
        return this.resourceDiscovery;
    }

    public List<String> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<String> resourceList) {
        this.resourceList = resourceList;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public int getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(int devicePort) {
        this.devicePort = devicePort;
    }

    public int getDeviceSecurePort() {
        return deviceSecurePort;
    }

    public void setDeviceSecurePort(int deviceSecurePort) {
        this.deviceSecurePort = deviceSecurePort;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CoapProtocolConfiguration{");
        sb.append("cacheEnabled=").append(cacheEnabled);
        sb.append(", resourceDiscovery=").append(resourceDiscovery);
        sb.append(", resourceList=").append(resourceList);
        sb.append(", deviceAddress='").append(deviceAddress).append('\'');
        sb.append(", devicePort=").append(devicePort);
        sb.append(", deviceSecurePort=").append(deviceSecurePort);
        sb.append('}');
        return sb.toString();
    }
}
