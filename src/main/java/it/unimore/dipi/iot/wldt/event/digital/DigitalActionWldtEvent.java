package it.unimore.dipi.iot.wldt.event.digital;

import it.unimore.dipi.iot.wldt.event.WldtEvent;
import it.unimore.dipi.iot.wldt.exception.EventBusException;

import java.util.Map;

public class DigitalActionWldtEvent<T> extends WldtEvent<T> {

    public static final String EVENT_BASIC_TYPE = "dt.digital.event.action";

    public DigitalActionWldtEvent(String type) throws EventBusException {
        super(type);
        adaptEventType();
    }

    public DigitalActionWldtEvent(String type, T body) throws EventBusException {
        super(type, body);
        adaptEventType();
    }

    public DigitalActionWldtEvent(String type, T body, Map<String, Object> metadata) throws EventBusException {
        super(type, body, metadata);
        adaptEventType();
    }

    private void adaptEventType(){
        if(this.getType() != null)
            this.setType(buildEventType(this.getType()));
    }

    public static String buildEventType(String eventType){
        if(eventType != null)
            return String.format("%s.%s", EVENT_BASIC_TYPE, eventType);
        else
            return null;
    }

}
