package it.unimore.dipi.iot.wldt.event;

import java.util.Optional;

public interface EventListener {

    public void onEventSubscribed(String eventType);

    public void onEventUnSubscribed(String eventType);

    public void onEvent(Optional<EventMessage<?>> eventMessage);

}
