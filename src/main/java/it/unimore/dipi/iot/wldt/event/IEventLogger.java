package it.unimore.dipi.iot.wldt.event;

public interface IEventLogger {

    public void logEventPublish(String publisherId, EventMessage<?> eventMessage);

    public void logEventForwarded(String publisherId, String subscriberId, EventMessage<?> eventMessage);

    public void logClientSubscription(String subscriberId);

    public void logClientUnSubscription(String subscriberId);

}
