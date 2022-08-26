package it.unimore.dipi.iot.wldt.core.event;

import it.unimore.dipi.iot.wldt.exception.EventBusException;

import java.util.Objects;

public class WldtSubscriberInfo {

    private String id;
    private WldtEventListener wldtEventListener;

    private WldtSubscriberInfo(){

    }

    public WldtSubscriberInfo(String id, WldtEventListener wldtEventListener) throws EventBusException {

        if(id == null || wldtEventListener == null)
            throw new EventBusException("Error creating SubscriberInfo ! Id or Listener = null !");

        this.id = id;
        this.wldtEventListener = wldtEventListener;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WldtEventListener getEventListener() {
        return wldtEventListener;
    }

    public void setEventListener(WldtEventListener wldtEventListener) {
        this.wldtEventListener = wldtEventListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WldtSubscriberInfo that = (WldtSubscriberInfo) o;
        return id.equals(that.id) && wldtEventListener.equals(that.wldtEventListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, wldtEventListener);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SubscriberInfo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", eventListener=").append(wldtEventListener);
        sb.append('}');
        return sb.toString();
    }
}
