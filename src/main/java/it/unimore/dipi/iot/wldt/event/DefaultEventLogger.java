package it.unimore.dipi.iot.wldt.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEventLogger implements IEventLogger{

    private static final Logger logger = LoggerFactory.getLogger(DefaultEventLogger.class);

    @Override
    public void logEventPublish(String publisherId, EventMessage<?> eventMessage) {
        if(eventMessage != null)
            logger.info("PUBLISHER [{}] -> PUBLISH TOPIC: {} Message: {}", publisherId, eventMessage.getTopic(), eventMessage);
        else
            logger.error("PUBLISHER [{}] -> NULL MESSAGE !", publisherId);
    }

    @Override
    public void logEventForwarded(String publisherId, String subscriberId, EventMessage<?> eventMessage) {
        if(eventMessage != null)
            logger.info("PUBLISHER [{}] / SUBSCRIBER [{}] -> TOPIC: {} Message: {}", publisherId, subscriberId, eventMessage.getTopic(), eventMessage);
        else
            logger.error("PUBLISHER [{}] -> NULL MESSAGE !", subscriberId);
    }

    @Override
    public void logClientSubscription(String subscriberId) {
        logger.info("SUBSCRIBER [{}] -> Subscribed Correctly", subscriberId);
    }

    @Override
    public void logClientUnSubscription(String subscriberId) {
        logger.info("SUBSCRIBER [{}] -> UnSubscribed Correctly", subscriberId);
    }
}
