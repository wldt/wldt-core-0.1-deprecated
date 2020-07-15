package it.unimore.dipi.iot.wldt.worker.coap;

import org.eclipse.californium.core.CoapResponse;

public class CoapCachedData {

    private long timestamp;
    private String uri;
    private CoapResponse coapResponse;

    public CoapCachedData() {
    }

    public CoapCachedData(long timestamp, String uri, CoapResponse coapResponse) {
        this.timestamp = timestamp;
        this.uri = uri;
        this.coapResponse = coapResponse;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public CoapResponse getCoapResponse() {
        return coapResponse;
    }

    public void setCoapResponse(CoapResponse coapResponse) {
        this.coapResponse = coapResponse;
    }

    public boolean isDataFresh(){

        if(this.coapResponse != null && this.timestamp > 0 && this.uri != null && this.coapResponse.getOptions().getMaxAge() > 0){
            long timeDiff = System.currentTimeMillis() - this.timestamp;

            if(timeDiff >= coapResponse.getOptions().getMaxAge())
                return false;
            else
                return true;
        }
        else
            return false;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CoapCachedData{");
        sb.append("timestamp=").append(timestamp);
        sb.append(", uri='").append(uri).append('\'');
        sb.append(", coapResponse=").append(coapResponse);
        sb.append('}');
        return sb.toString();
    }
}
