package it.unimore.dipi.iot.wldt.worker.coap;

import it.unimore.dipi.iot.wldt.exception.WldtCoapResourceDiscoveryException;
import org.eclipse.californium.core.WebLink;

import java.util.Objects;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtCoapResourceDescriptor {

    private static final String RT_CORE_LINK_FORMAT_ATTRIBUTE = "rt";
    private static final String IF_CORE_LINK_FORMAT_ATTRIBUTE = "if";
    private static final String TITLE_CORE_LINK_FORMAT_ATTRIBUTE = "title";
    private static final String OBS_CORE_LINK_FORMAT_ATTRIBUTE = "obs";

    private String deviceAddress;
    private int devicePort;
    private String id;
    private String uri;
    private Boolean isObservable;
    private String title;
    private String resourceType;
    private String coreInterface;

    public WldtCoapResourceDescriptor() {
    }

    public WldtCoapResourceDescriptor(String deviceAddress, int devicePort) {
        this.deviceAddress = deviceAddress;
        this.devicePort = devicePort;
    }

    public WldtCoapResourceDescriptor(WebLink link, String deviceAddress, int devicePort) throws WldtCoapResourceDiscoveryException {

        if(link != null){

            this.setId(link.getURI().replace("/",""));
            this.setUri(link.getURI());
            this.setDeviceAddress(deviceAddress);
            this.setDevicePort(devicePort);

            if(link.getAttributes() != null){
                //TODO Check for multiple values
                setCoreInterface(link.getAttributes().containsAttribute(IF_CORE_LINK_FORMAT_ATTRIBUTE) ? link.getAttributes().getAttributeValues(IF_CORE_LINK_FORMAT_ATTRIBUTE).get(0) : null);
                setTitle(link.getAttributes().containsAttribute(TITLE_CORE_LINK_FORMAT_ATTRIBUTE) ? link.getAttributes().getAttributeValues(TITLE_CORE_LINK_FORMAT_ATTRIBUTE).get(0) : null);
                setResourceType(link.getAttributes().containsAttribute(RT_CORE_LINK_FORMAT_ATTRIBUTE) ? link.getAttributes().getAttributeValues(RT_CORE_LINK_FORMAT_ATTRIBUTE).get(0) : null);
                setObservable(link.getAttributes().containsAttribute(OBS_CORE_LINK_FORMAT_ATTRIBUTE));
            }
        }
        else
            throw new WldtCoapResourceDiscoveryException("Provided WebLink Object = null !");
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
        WldtCoapResourceDescriptor that = (WldtCoapResourceDescriptor) o;
        return deviceAddress.equals(that.deviceAddress) &&
                devicePort == that.devicePort &&
                id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceAddress, devicePort, id);
    }
}
