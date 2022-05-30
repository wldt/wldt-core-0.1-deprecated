package it.unimore.wldt.test.event;

import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.event.EventListener;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class EventBusTester {

    public static final String PUBLISHER_ID_1 = "testModulePublisher1";
    public static final String TEST_TOPIC_1 = "topic0001";
    public static final String TEST_VALUE_0001 = "TEST-STRING-1";
    public static final String METADATA_KEY_TEST_1 = "metadata-key-1";
    public static final String METADATA_VALUE_TEST_1 = "metadata-value-1";
    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public static final String TEST_TOPIC_2 = "topic0002";
    public static final String TEST_TOPIC_3 = "topic0003";
    public static final String TEST_TOPIC_4 = "topic0004";

    public static final int MESSAGE_COUNT = 100;
    public static final int PUBLISHER_SLEEP_TIME_MS = 10;
    public static int receivedMessageCount = 0;
    public static long delaySum = 0;

    private CountDownLatch lock = new CountDownLatch(1);

    private EventMessage<?> receivedMessage;

    private List<String> targetSubscriptionList = new ArrayList<>();

    @Test
    public void testMultipleSubscriptions() throws InterruptedException, EventBusException {

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        EventListener myEventListener = new EventListener() {
            @Override
            public void onEventSubscribed(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onSubscribe() called ! Event-Type:" + eventType);
                targetSubscriptionList.add(eventType);
            }

            @Override
            public void onEventUnSubscribed(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onUnSubscribe() called ! Event-Type:" + eventType);
                targetSubscriptionList.remove(eventType);
            }

            @Override
            public void onEvent(EventMessage<?> eventMessage) {
                if(eventMessage != null){
                    EventMessage<String> msg = (EventMessage<String>)eventMessage;
                    long diff = System.currentTimeMillis() - msg.getCreationTimestamp();
                    System.out.println("Message Received in: " + diff);
                }

                receivedMessage = eventMessage;
                lock.countDown();
            }
        };


        //Subscribe to TOPIC 1
        testSubscribeToEventTypes(SUBSCRIBER_ID_1, Collections.singletonList(TEST_TOPIC_1), myEventListener);
        testEventTransmission(TEST_TOPIC_1, TEST_VALUE_0001);
        Thread.sleep(1000);

        //ReSubscribe to TOPIC 1
        testSubscribeToEventTypes(SUBSCRIBER_ID_1, Collections.singletonList(TEST_TOPIC_1), myEventListener);
        testEventTransmission(TEST_TOPIC_1, TEST_VALUE_0001);
        Thread.sleep(1000);

        //UnSubscribe from Topic 1
        testUnsubscribe(SUBSCRIBER_ID_1, Collections.singletonList(TEST_TOPIC_1), myEventListener);
        testSubscribeToEventTypes(SUBSCRIBER_ID_1, Collections.singletonList(TEST_TOPIC_1), myEventListener);
        testEventTransmission(TEST_TOPIC_1, TEST_VALUE_0001);
        Thread.sleep(1000);

        //Subscribe to Topic 1 and Topic 2
        testSubscribeToEventTypes(SUBSCRIBER_ID_1, Arrays.asList(TEST_TOPIC_1, TEST_TOPIC_2), myEventListener);
        testEventTransmission(TEST_TOPIC_1, TEST_VALUE_0001);
        testEventTransmission(TEST_TOPIC_2, TEST_VALUE_0001);
        Thread.sleep(1000);

        //UnSubscribe from Topic1
        testUnsubscribe(SUBSCRIBER_ID_1, Arrays.asList(TEST_TOPIC_1), myEventListener);
        testEventTransmission(TEST_TOPIC_2, TEST_VALUE_0001);
        Thread.sleep(1000);

    }

    private void testUnsubscribe(String subscriberId, List<String> typeList, EventListener eventListener) throws EventBusException, InterruptedException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.addAll(typeList);
        EventBus.getInstance().unSubscribe(subscriberId, eventFilter, eventListener);

        Thread.sleep(1000);

        for(String type : typeList)
            assertFalse(targetSubscriptionList.contains(type));
    }

    private void testSubscribeToEventTypes(String subscriberId, List<String> typeList, EventListener eventListener) throws EventBusException, InterruptedException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.addAll(typeList);
        EventBus.getInstance().subscribe(subscriberId, eventFilter, eventListener);

        Thread.sleep(1000);
        assertEquals(targetSubscriptionList, typeList);
    }

    private void testEventTransmission(String targetTopic, String body) throws InterruptedException, EventBusException {

        lock = new CountDownLatch(1);

        //Define New Message
        EventMessage<String> eventMessage = new EventMessage<>(targetTopic);
        eventMessage.setBody(body);
        eventMessage.putMetadata(METADATA_KEY_TEST_1, METADATA_VALUE_TEST_1);

        //Publish Message on the target Topic1
        EventBus.getInstance().publishEvent(PUBLISHER_ID_1, eventMessage);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(receivedMessage);
        assertEquals(eventMessage, receivedMessage);
        assertEquals(TEST_VALUE_0001, receivedMessage.getBody());
        assertEquals(eventMessage.getMetadata(), receivedMessage.getMetadata());
    }

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
            public void onEventSubscribed(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onSubscribe() called ! Event-Type:" + eventType);
            }

            @Override
            public void onEventUnSubscribed(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onUnSubscribe() called ! Event-Type:" + eventType);
            }

            @Override
            public void onEvent(EventMessage<?> eventMessage) {
                if(eventMessage != null){
                    EventMessage<String> msg = (EventMessage<String>)eventMessage;
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

        assertNotNull(receivedMessage);
        assertEquals(eventMessage, receivedMessage);
        assertEquals(TEST_VALUE_0001, receivedMessage.getBody());
        assertEquals(eventMessage.getMetadata(), receivedMessage.getMetadata());
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
            public void onEventSubscribed(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onSubscribe() called ! Event-Type:" + eventType);
            }

            @Override
            public void onEventUnSubscribed(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onUnSubscribe() called ! Event-Type:" + eventType);
            }

            @Override
            public void onEvent(EventMessage<?> eventMessage) {

                if(eventMessage != null){
                    receivedMessageCount++;
                    EventMessage<String> msg = (EventMessage<String>)eventMessage;
                    long diff = System.currentTimeMillis() - msg.getCreationTimestamp();
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
