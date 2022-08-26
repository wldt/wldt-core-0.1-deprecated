package it.unimore.dipi.iot.wldt.adapter.physical.event;

import it.unimore.dipi.iot.wldt.core.event.WldtEvent;
import it.unimore.dipi.iot.wldt.exception.EventBusException;

import java.util.Map;

public class PhysicalAssetEventWldtEvent<T> extends WldtEvent<T> {

    public static final String PHYSICAL_EVENT_BASIC_TYPE = "dt.physical.event.event";

    private String physicalEventKey;

    public PhysicalAssetEventWldtEvent(String physicalEventKey, T body) throws EventBusException {
        super(physicalEventKey, body);
        adaptEventType();
        this.physicalEventKey = physicalEventKey;
    }

    public PhysicalAssetEventWldtEvent(String physicalEventKey, T body, Map<String, Object> metadata) throws EventBusException {
        super(physicalEventKey, body, metadata);
        adaptEventType();
        this.physicalEventKey = physicalEventKey;
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

    public String getPhysicalEventKey() {
        return physicalEventKey;
    }

    public void setPhysicalEventKey(String physicalEventKey) {
        this.physicalEventKey = physicalEventKey;
    }
}
