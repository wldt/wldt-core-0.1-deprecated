package it.unimore.dipi.iot.wldt.event;

import java.util.Optional;

public interface EventListener {

    public void onSubscribe();

    public void onUnSubscribe();

    public void onEvent(Optional<EventMessage<?>> eventMessage);

}
