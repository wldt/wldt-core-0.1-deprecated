package it.unimore.dipi.iot.wldt.event;

public interface IEventLogger {

    public void logEventPublished(String publisherId, EventMessage<?> eventMessage);

    public void logEventForwarded(String publisherId, String subscriberId, EventMessage<?> eventMessage);

    public void logClientSubscription(String eventType, String subscriberId);

    public void logClientUnSubscription(String eventType, String subscriberId);

}
