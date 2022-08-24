package it.unimore.dipi.iot.wldt.event;

public interface WldtEventListener {

    public void onEventSubscribed(String eventType);

    public void onEventUnSubscribed(String eventType);

    public void onEvent(WldtEvent<?> wldtEvent);

}
