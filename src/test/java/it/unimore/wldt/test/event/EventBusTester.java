package it.unimore.wldt.test.event;

import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class EventBusTester {

    public static final String PUBLISHER_ID_1 = "testModulePublisher1";
    public static final String TEST_TOPIC_1 = "topic0001";
    public static final String TEST_VALUE_0001 = "TEST-STRING-1";
    public static final String METADATA_KEY_TEST_1 = "metadata-key-1";
    public static final String METADATA_VALUE_TEST_1 = "metadata-value-1";
    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public static final int MESSAGE_COUNT = 100;
    public static final int PUBLISHER_SLEEP_TIME_MS = 10;
    public static int receivedMessageCount = 0;
    public static long delaySum = 0;

    private CountDownLatch lock = new CountDownLatch(1);

    private Optional<EventMessage<?>> receivedMessage;

    @Test
    public void singlePubSubTest() throws InterruptedException, EventBusException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(TEST_TOPIC_1);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Subscribe for target topic
        EventBus.getInstance().subscribe(SUBSCRIBER_ID_1, eventFilter, new EventListener() {
            @Override
            public void onSubscribe(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onSubscribe() called ! Event-Type:" + eventType);
            }

            @Override
            public void onUnSubscribe(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onUnSubscribe() called ! Event-Type:" + eventType);
            }

            @Override
            public void onEvent(Optional<EventMessage<?>> eventMessage) {
                if(eventMessage.isPresent()){
                    EventMessage<String> msg = (EventMessage<String>)eventMessage.get();
                    long diff = System.currentTimeMillis() - msg.getCreationTimestamp();
                    System.out.println("Message Received in: " + diff);
                }

                receivedMessage = eventMessage;
                lock.countDown();
            }
        });

        //Define New Message
        EventMessage<String> eventMessage = new EventMessage<>(TEST_TOPIC_1);
        eventMessage.setBody(TEST_VALUE_0001);
        eventMessage.putMetadata(METADATA_KEY_TEST_1, METADATA_VALUE_TEST_1);

        //Publish Message on the target Topic1
        EventBus.getInstance().publishEvent(PUBLISHER_ID_1, eventMessage);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertTrue(receivedMessage.isPresent());
        assertEquals(eventMessage, receivedMessage.get());
        assertEquals(TEST_VALUE_0001, receivedMessage.get().getBody());
        assertEquals(eventMessage.getMetadata(), receivedMessage.get().getMetadata());
    }

    @Test
    public void multipleMessagesPubSubTest() throws InterruptedException, EventBusException {

        receivedMessageCount = 0;
        delaySum = 0;

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(TEST_TOPIC_1);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Subscribe for target topic
        EventBus.getInstance().subscribe(SUBSCRIBER_ID_1, eventFilter, new EventListener() {
            @Override
            public void onSubscribe(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onSubscribe() called ! Event-Type:" + eventType);
            }

            @Override
            public void onUnSubscribe(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onUnSubscribe() called ! Event-Type:" + eventType);
            }

            @Override
            public void onEvent(Optional<EventMessage<?>> eventMessage) {

                if(eventMessage.isPresent()){
                    receivedMessageCount++;
                    EventMessage<String> msg = (EventMessage<String>)eventMessage.get();
                    long diff = System.currentTimeMillis() - msg.getCreationTimestamp();
                    //System.out.println("Message Received in: " + diff);
                    delaySum += diff;
                }

                lock.countDown();
            }
        });

        //Publish Message on the target Topic1
        for(int i=0; i<MESSAGE_COUNT; i++) {
            //Define New Message
            EventMessage<String> eventMessage = new EventMessage<>(TEST_TOPIC_1);
            eventMessage.setBody(TEST_VALUE_0001);
            eventMessage.putMetadata(METADATA_KEY_TEST_1, METADATA_VALUE_TEST_1);

            EventBus.getInstance().publishEvent(PUBLISHER_ID_1, eventMessage);
            Thread.sleep(PUBLISHER_SLEEP_TIME_MS);
        }

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertEquals(MESSAGE_COUNT, receivedMessageCount);

        double averageDelay = (double)delaySum / (double)MESSAGE_COUNT;

        System.out.println("Average Internal Delay: " + averageDelay);
    }

}
