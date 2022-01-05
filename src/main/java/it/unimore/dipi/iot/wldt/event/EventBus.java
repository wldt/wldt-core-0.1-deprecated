package it.unimore.dipi.iot.wldt.event;

import it.unimore.dipi.iot.wldt.exception.EventBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {

    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);

    private static EventBus instance = null;
    private Map<String, List<EventListener>> subscriberMap = null;

    private EventBus(){
        this.subscriberMap = new HashMap<>();
    }

    public static EventBus getInstance(){
        if(instance == null)
            instance = new EventBus();
        return instance;
    }

    public void publishEvent(EventMessage eventMessage) throws EventBusException {
        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-publishEvent() -> Error: SubscriberMap = NULL !");

        if(eventMessage == null || eventMessage.getType() == null || (eventMessage.getType() != null && eventMessage.getType().length() == 0))
            throw new EventBusException(String.format("EventBus-publishEvent() -> Error: eventMessage = NULL or event-type (%s) is invalid !", eventMessage != null ? eventMessage.getType() : "null"));

        if(this.subscriberMap.containsKey(eventMessage.getType()) && this.subscriberMap.get(eventMessage.getType()).size() > 0)
            this.subscriberMap.get(eventMessage.getType()).forEach(eventListener -> {
                eventListener.onEvent(eventMessage);
            });
    }

    public void subscribe(EventFilter eventFilter, EventListener eventListener) throws EventBusException{

        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-subscribe() -> Error: SubscriberMap = NULL !");

        if(eventFilter == null || eventListener == null)
            throw new EventBusException("EventBus-subscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        for(String eventType: eventFilter) {
            //If required init the ArrayList for target eventyType
            if (!this.subscriberMap.containsKey(eventType))
                this.subscriberMap.put(eventType, new ArrayList<>());

            this.subscriberMap.get(eventType).add(eventListener);
            eventListener.onSubscribe();
        }
    }

    public void unSubscribe(EventFilter eventFilter, EventListener eventListener) throws EventBusException{

        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: SubscriberMap = NULL !");

        if(eventFilter == null || eventListener == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        for(String eventType: eventFilter) {
            if(this.subscriberMap.get(eventType) != null && this.subscriberMap.get(eventType).contains(eventListener)) {
                this.subscriberMap.get(eventType).remove(eventListener);
                eventListener.onUnSubscribe();
            }
        }
    }

}
