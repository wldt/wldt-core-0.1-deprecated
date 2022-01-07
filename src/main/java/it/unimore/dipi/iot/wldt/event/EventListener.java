package it.unimore.dipi.iot.wldt.event;

import java.util.Optional;

public interface EventListener {

    public void onSubscribe(String eventType);

    public void onUnSubscribe(String eventType);

    public void onEvent(Optional<EventMessage<?>> eventMessage);

}
