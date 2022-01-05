package it.unimore.dipi.iot.wldt.event;

public interface EventListener {

    public void onSubscribe();

    public void onUnSubscribe();

    public void onEvent(EventMessage eventMessage);

}
