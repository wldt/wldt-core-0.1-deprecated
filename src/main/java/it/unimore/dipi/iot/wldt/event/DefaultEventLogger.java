package it.unimore.dipi.iot.wldt.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEventLogger implements IEventLogger{

    private static final Logger logger = LoggerFactory.getLogger(DefaultEventLogger.class);

    @Override
    public void logEventPublish(String publisherId, EventMessage<?> eventMessage) {
        if(eventMessage != null)
            logger.info("PUBLISHER [{}] -> PUBLISH EVENT TYPE: {} Message: {}", publisherId, eventMessage.getType(), eventMessage);
        else
            logger.error("PUBLISHER [{}] -> NULL MESSAGE !", publisherId);
    }

    @Override
    public void logEventForwarded(String publisherId, String subscriberId, EventMessage<?> eventMessage) {
        if(eventMessage != null)
            logger.info("EVENT-BUS -> FORWARDED from PUBLISHER [{}] to SUBSCRIBER [{}] -> TOPIC: {} Message: {}", publisherId, subscriberId, eventMessage.getType(), eventMessage);
        else
            logger.error("EVENT-BUS FORWARDING from PUBLISHER [{}] to SUBSCRIBER [{}] -> NULL MESSAGE ! ", publisherId, subscriberId);
    }

    @Override
    public void logClientSubscription(String eventType, String subscriberId) {
        logger.info("SUBSCRIBER [{}] -> Subscribed Correctly - Event Type: {}", subscriberId, eventType);
    }

    @Override
    public void logClientUnSubscription(String eventType, String subscriberId) {
        logger.info("SUBSCRIBER [{}] -> UnSubscribed Correctly  - Event Type: {}", subscriberId, eventType);
    }
}
