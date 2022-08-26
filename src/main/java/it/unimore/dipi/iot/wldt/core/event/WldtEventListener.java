package it.unimore.dipi.iot.wldt.core.event;

public interface WldtEventListener {

    public void onEventSubscribed(String eventType);

    public void onEventUnSubscribed(String eventType);

    public void onEvent(WldtEvent<?> wldtEvent);

}
