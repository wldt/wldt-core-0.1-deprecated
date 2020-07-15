package it.unimore.dipi.iot.wldt.worker.philips;

import java.util.Objects;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class PhilipsHueLightCoapResourceDescriptor {

    public static final String RT_CORE_LINK_FORMAT_ATTRIBUTE = "rt";
    public static final String IF_CORE_LINK_FORMAT_ATTRIBUTE = "if";
    public static final String TITLE_CORE_LINK_FORMAT_ATTRIBUTE = "title";
    public static final String OBS_CORE_LINK_FORMAT_ATTRIBUTE = "obs";

    private String deviceAddress;
    private int devicePort;
    private String id;
    private String uri;
    private Boolean isObservable;
    private String title;
    private String resourceType;
    private String coreInterface;

    public PhilipsHueLightCoapResourceDescriptor() {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Boolean getObservable() {
        return isObservable;
    }

    public void setObservable(Boolean observable) {
        isObservable = observable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getCoreInterface() {
        return coreInterface;
    }

    public void setCoreInterface(String coreInterface) {
        this.coreInterface = coreInterface;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WldtCoapResource{");
        sb.append("deviceAddress='").append(deviceAddress).append('\'');
        sb.append(", devicePort='").append(devicePort).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", uri='").append(uri).append('\'');
        sb.append(", isObservable=").append(isObservable);
        sb.append(", title='").append(title).append('\'');
        sb.append(", resourceType='").append(resourceType).append('\'');
        sb.append(", coreInterface='").append(coreInterface).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhilipsHueLightCoapResourceDescriptor that = (PhilipsHueLightCoapResourceDescriptor) o;
        return deviceAddress.equals(that.deviceAddress) &&
                devicePort == that.devicePort &&
                id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceAddress, devicePort, id);
    }
}
