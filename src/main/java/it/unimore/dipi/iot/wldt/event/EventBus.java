package it.unimore.dipi.iot.wldt.event;

import it.unimore.dipi.iot.wldt.exception.EventBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EventBus {

    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);

    private static EventBus instance = null;
    private Map<String, List<SubscriberInfo>> subscriberMap = null;

    private IEventLogger eventLogger = null;

    private EventBus(){
        this.subscriberMap = new HashMap<>();
    }

    public static EventBus getInstance(){
        if(instance == null)
            instance = new EventBus();
        return instance;
    }

    public void setEventLogger(IEventLogger eventLogger){
        this.eventLogger = eventLogger;
    }

    public void publishEvent(String publisherId, EventMessage<?> eventMessage) throws EventBusException {
        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-publishEvent() -> Error: SubscriberMap = NULL !");

        if(eventMessage == null || eventMessage.getType() == null || (eventMessage.getType() != null && eventMessage.getType().length() == 0))
            throw new EventBusException(String.format("EventBus-publishEvent() -> Error: eventMessage = NULL or event-type (%s) is invalid !", eventMessage != null ? eventMessage.getType() : "null"));

        if(eventLogger != null)
            eventLogger.logEventPublish(publisherId, eventMessage);

        if(this.subscriberMap.containsKey(eventMessage.getType()) && this.subscriberMap.get(eventMessage.getType()).size() > 0)
            this.subscriberMap.get(eventMessage.getType()).forEach(subscriberInfo -> {
                subscriberInfo.getEventListener().onEvent(Optional.ofNullable(eventMessage));

                if(eventLogger != null)
                    eventLogger.logEventForwarded(publisherId, subscriberInfo.getId(), eventMessage);
            });
    }

    public void subscribe(String subscriberId, EventFilter eventFilter, EventListener eventListener) throws EventBusException{

        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-subscribe() -> Error: SubscriberMap = NULL !");

        if(eventFilter == null || eventListener == null)
            throw new EventBusException("EventBus-subscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        for(String eventType: eventFilter) {
            //If required init the ArrayList for target eventyType
            if (!this.subscriberMap.containsKey(eventType))
                this.subscriberMap.put(eventType, new ArrayList<>());

            this.subscriberMap.get(eventType).add(new SubscriberInfo(subscriberId, eventListener));
            eventListener.onSubscribe(eventType);

            if(eventLogger != null)
                eventLogger.logClientSubscription(eventType, subscriberId);
        }
    }

    public void unSubscribe(String subscriberId, EventFilter eventFilter, EventListener eventListener) throws EventBusException{

        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: SubscriberMap = NULL !");

        if(eventFilter == null || eventListener == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        SubscriberInfo subscriberInfo = new SubscriberInfo(subscriberId, eventListener);

        for(String eventType: eventFilter) {
            if(this.subscriberMap.get(eventType) != null && this.subscriberMap.get(eventType).contains(subscriberInfo)) {
                this.subscriberMap.get(eventType).remove(subscriberInfo);
                eventListener.onUnSubscribe(eventType);

                if(eventLogger != null)
                    eventLogger.logClientUnSubscription(eventType, subscriberId);
            }
        }
    }

}
