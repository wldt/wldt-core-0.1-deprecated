package it.unimore.dipi.iot.wldt.worker.coap;

import it.unimore.dipi.iot.wldt.processing.PipelineData;

public class CoapPipelineData implements PipelineData {

    private String resourceUri;

    private byte[] payload;

    private int contentFormat;

    private WldtCoapResourceDescriptor wldtCoapResourceDescriptor;

    public CoapPipelineData() {
    }

    public CoapPipelineData(String resourceUri, byte[] payload, int contentFormat) {
        this.resourceUri = resourceUri;
        this.payload = payload;
        this.contentFormat = contentFormat;
    }

    public CoapPipelineData(String resourceUri, byte[] payload, int contentFormat, WldtCoapResourceDescriptor wldtCoapResourceDescriptor) {
        this.resourceUri = resourceUri;
        this.payload = payload;
        this.contentFormat = contentFormat;
        this.wldtCoapResourceDescriptor = wldtCoapResourceDescriptor;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getContentFormat() {
        return contentFormat;
    }

    public void setContentFormat(int contentFormat) {
        this.contentFormat = contentFormat;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public WldtCoapResourceDescriptor getWldtCoapResourceDescriptor() {
        return wldtCoapResourceDescriptor;
    }

    public void setWldtCoapResourceDescriptor(WldtCoapResourceDescriptor wldtCoapResourceDescriptor) {
        this.wldtCoapResourceDescriptor = wldtCoapResourceDescriptor;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CoapPipelineData{");
        sb.append("resourceUri='").append(resourceUri).append('\'');
        sb.append(", payload=");
        if (payload == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < payload.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(payload[i]);
            sb.append(']');
        }
        sb.append(", contentFormat=").append(contentFormat);
        sb.append(", wldtCoapResourceDescriptor=").append(wldtCoapResourceDescriptor);
        sb.append('}');
        return sb.toString();
    }
}
