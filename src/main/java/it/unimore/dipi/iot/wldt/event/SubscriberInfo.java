package it.unimore.dipi.iot.wldt.event;

import it.unimore.dipi.iot.wldt.exception.EventBusException;

import java.util.Objects;

public class SubscriberInfo {

    private String id;
    private EventListener eventListener;

    private SubscriberInfo(){

    }

    public SubscriberInfo(String id, EventListener eventListener) throws EventBusException {

        if(id == null || eventListener == null)
            throw new EventBusException("Error creating SubscriberInfo ! Id or Listener = null !");

        this.id = id;
        this.eventListener = eventListener;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriberInfo that = (SubscriberInfo) o;
        return id.equals(that.id) && eventListener.equals(that.eventListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventListener);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SubscriberInfo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", eventListener=").append(eventListener);
        sb.append('}');
        return sb.toString();
    }
}
