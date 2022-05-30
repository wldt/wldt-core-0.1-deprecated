package it.unimore.dipi.iot.wldt.event;

import it.unimore.dipi.iot.wldt.exception.EventBusException;
import java.util.Map;

public class PhysicalPropertyEventMessage<T> extends EventMessage <T>{

    public static final String PHYSICAL_EVENT_BASIC_TYPE = "dt.physical.event.property";

    private String physicalPropertyId;

    public PhysicalPropertyEventMessage(String physicalPropertyId) throws EventBusException {
        super(physicalPropertyId);
        adaptEventType();
        this.physicalPropertyId = physicalPropertyId;
    }

    public PhysicalPropertyEventMessage(String physicalPropertyId, T body) throws EventBusException {
        super(physicalPropertyId, body);
        adaptEventType();
        this.physicalPropertyId = physicalPropertyId;
    }

    public PhysicalPropertyEventMessage(String physicalPropertyId, T body, Map<String, Object> metadata) throws EventBusException {
        super(physicalPropertyId, body, metadata);
        adaptEventType();
        this.physicalPropertyId = physicalPropertyId;
    }

    private void adaptEventType(){
        if(this.getType() != null)
            this.setType(buildEventType(this.getType()));
    }

    public static String buildEventType(String eventType){
        if(eventType != null)
            return String.format("%s.%s", PHYSICAL_EVENT_BASIC_TYPE, eventType);
        else
            return null;
    }

    public String getPhysicalPropertyId() {
        return physicalPropertyId;
    }

    public void setPhysicalPropertyId(String physicalPropertyId) {
        this.physicalPropertyId = physicalPropertyId;
    }
}
