package it.unimore.dipi.iot.wldt.event;

import it.unimore.dipi.iot.wldt.exception.EventBusException;

import java.util.Map;

public class PhysicalActionEventMessage<T> extends EventMessage <T>{

    public static final String EVENT_BASIC_TYPE = "dt.physical.event.action";

    public PhysicalActionEventMessage(String type) throws EventBusException {
        super(type);
        adaptEventType();
    }

    public PhysicalActionEventMessage(String type, T body) throws EventBusException {
        super(type, body);
        adaptEventType();
    }

    public PhysicalActionEventMessage(String type, T body, Map<String, Object> metadata) throws EventBusException {
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