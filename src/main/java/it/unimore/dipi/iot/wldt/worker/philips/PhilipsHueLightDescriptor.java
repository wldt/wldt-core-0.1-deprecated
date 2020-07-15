package it.unimore.dipi.iot.wldt.worker.philips;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 12/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class PhilipsHueLightDescriptor {

    private String id = null;

    private boolean isOn = false;

    private int brightness = 0;

    private int hue = 0;

    private int saturation = 0;

    private String effect = null;

    private String alert = null;

    private String type = null;

    private String name = null;

    private String modelId = null;

    private String manufacturerName = null;

    private String productName = null;

    private String uniqueId = null;

    private String softwareVersion = null;

    private String softwareConfigId = null;

    private String productId = null;

    private boolean reachable = false;

    public PhilipsHueLightDescriptor() {
    }

    public PhilipsHueLightDescriptor(String id, boolean isOn, int brightness, int hue, int saturation, String effect, String alert, String type, String name, String modelId, String manufacturerName, String productName, String uniqueId, String softwareVersion, String softwareConfigId, String productId, boolean reachable) {
        this.id = id;
        this.isOn = isOn;
        this.brightness = brightness;
        this.hue = hue;
        this.saturation = saturation;
        this.effect = effect;
        this.alert = alert;
        this.type = type;
        this.name = name;
        this.modelId = modelId;
        this.manufacturerName = manufacturerName;
        this.productName = productName;
        this.uniqueId = uniqueId;
        this.softwareVersion = softwareVersion;
        this.softwareConfigId = softwareConfigId;
        this.productId = productId;
        this.reachable = reachable;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getSoftwareConfigId() {
        return softwareConfigId;
    }

    public void setSoftwareConfigId(String softwareConfigId) {
        this.softwareConfigId = softwareConfigId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PhilipsHueLightDescriptor{");
        sb.append("id='").append(id).append('\'');
        sb.append(", isOn=").append(isOn);
        sb.append(", brightness=").append(brightness);
        sb.append(", hue=").append(hue);
        sb.append(", saturation=").append(saturation);
        sb.append(", effect='").append(effect).append('\'');
        sb.append(", alert='").append(alert).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", modelId='").append(modelId).append('\'');
        sb.append(", manufacturerName='").append(manufacturerName).append('\'');
        sb.append(", productName='").append(productName).append('\'');
        sb.append(", uniqueId='").append(uniqueId).append('\'');
        sb.append(", softwareVersion='").append(softwareVersion).append('\'');
        sb.append(", softwareConfigId='").append(softwareConfigId).append('\'');
        sb.append(", productId='").append(productId).append('\'');
        sb.append(", reachable=").append(reachable);
        sb.append('}');
        return sb.toString();
    }
}
