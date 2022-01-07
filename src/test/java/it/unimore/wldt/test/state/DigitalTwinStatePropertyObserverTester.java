package it.unimore.wldt.test.state;

import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.state.*;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class DigitalTwinStatePropertyObserverTester {

    public static final String TEST_PROPERTY_KEY_0001 = "testKey0001";
    public static final String TEST_PROPERTY_VALUE_0001 = "TEST-STRING";
    public static final String TEST_PROPERTY_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public static IDigitalTwinState digitalTwinState = null;

    public static DigitalTwinStateProperty<String> testProperty1 = null;

    private CountDownLatch lock = new CountDownLatch(1);

    private Optional<DigitalTwinStateProperty<?>>  receivedProperty;
    private Optional<DigitalTwinStateProperty<?>>  receivedOriginalProperty;
    private String receivedPropertyKey;

    private Optional<DigitalTwinStateProperty<?>>  secondObserverReceivedProperty;
    private String secondObserverReceivedPropertyKey;

    private void initTestDtState() {
        //Init DigitaTwin State
        digitalTwinState = new DefaultDigitalTwinState();
    }

    private void createProperty() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException {
        testProperty1 = new DigitalTwinStateProperty<>();
        testProperty1.setKey(TEST_PROPERTY_KEY_0001);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);
        testProperty1.setValue(TEST_PROPERTY_VALUE_0001);

        digitalTwinState.createProperty(TEST_PROPERTY_KEY_0001, testProperty1);
    }

    @Test
    public void observePropertyUpdate() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);

        //eventFilter.add(digitalTwinState.getPropertyUpdatedEventMessageType(TEST_PROPERTY_KEY_0001));
        //eventFilter.add(digitalTwinState.getPropertyDeletedEventMessageType(TEST_PROPERTY_KEY_0001));

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Subscribe for target topic
        EventBus.getInstance().subscribe(SUBSCRIBER_ID_1, eventFilter, new EventListener() {
            @Override
            public void onSubscribe() {
                System.out.println(SUBSCRIBER_ID_1  + " -> onSubscribe() called !");
            }

            @Override
            public void onUnSubscribe() {
                System.out.println(SUBSCRIBER_ID_1  + " -> onUnSubscribe() called !");
            }

            @Override
            public void onEvent(Optional<EventMessage<?>> eventMessage) {
                if(eventMessage.isPresent()){
                    EventMessage<String> msg = (EventMessage<String>)eventMessage.get();
                    long diff = System.currentTimeMillis() - msg.getCreationTimestamp();
                    System.out.println("Message Received in: " + diff);
                }

                //lock.countDown();
            }
        });

        createProperty();

        //Update Property
        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001_UPDATED, true, true);
        digitalTwinState.updateProperty(TEST_PROPERTY_KEY_0001, updatedProperty);

        lock.await(2000, TimeUnit.MILLISECONDS);

        /*
        assertEquals(TEST_PROPERTY_KEY_0001, receivedPropertyKey);
        assertTrue(receivedProperty.isPresent());
        assertTrue(receivedOriginalProperty.isPresent());
        assertEquals(testProperty1, receivedOriginalProperty.get());
        assertEquals(updatedProperty, receivedProperty.get());
        assertEquals(TEST_PROPERTY_VALUE_0001, receivedOriginalProperty.get().getValue());
        assertEquals(TEST_PROPERTY_VALUE_0001_UPDATED, receivedProperty.get().getValue());
        */
    }

//
//    @Test
//    public void multipleObserversPropertyUpdate() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException {
//
//        //Init DigitaTwin State
//        initTestDtState();
//        createProperty();
//
//        digitalTwinState.observeProperty(TEST_KEY_0001, new DigitalTwinStatePropertyListener() {
//            @Override
//            public void onChange(String propertyKey, Optional<DigitalTwinStateProperty<?>> previousDtStateProperty, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
//                receivedProperty = dtStateProperty;
//                receivedPropertyKey = propertyKey;
//                lock.countDown();
//            }
//
//            @Override
//            public void onDelete(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
//
//            }
//        });
//
//        digitalTwinState.observeProperty(TEST_KEY_0001, new DigitalTwinStatePropertyListener() {
//            @Override
//            public void onChange(String propertyKey, Optional<DigitalTwinStateProperty<?>> previousDtStateProperty, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
//                secondObserverReceivedProperty = dtStateProperty;
//                secondObserverReceivedPropertyKey = propertyKey;
//                lock.countDown();
//            }
//
//            @Override
//            public void onDelete(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
//
//            }
//        });
//
//        //Update Property
//        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_KEY_0001, TEST_VALUE_0001_UPDATED, true, true);
//        digitalTwinState.updateProperty(TEST_KEY_0001, updatedProperty);
//
//        lock.await(2000, TimeUnit.MILLISECONDS);
//
//        assertEquals(TEST_KEY_0001, receivedPropertyKey);
//        assertTrue(receivedProperty.isPresent());
//        assertEquals(updatedProperty, receivedProperty.get());
//        assertEquals(TEST_VALUE_0001_UPDATED, receivedProperty.get().getValue());
//
//        assertEquals(TEST_KEY_0001, secondObserverReceivedPropertyKey);
//        assertTrue(secondObserverReceivedProperty.isPresent());
//        assertEquals(updatedProperty, secondObserverReceivedProperty.get());
//        assertEquals(TEST_VALUE_0001_UPDATED, secondObserverReceivedProperty.get().getValue());
//    }
//
//    @Test
//    public void observePropertyDelete() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException {
//
//        //Init DigitaTwin State
//        initTestDtState();
//        createProperty();
//
//        digitalTwinState.observeProperty(TEST_KEY_0001, new DigitalTwinStatePropertyListener() {
//            @Override
//            public void onChange(String propertyKey, Optional<DigitalTwinStateProperty<?>> previousDtStateProperty, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
//            }
//
//            @Override
//            public void onDelete(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
//                receivedProperty = dtStateProperty;
//                receivedPropertyKey = propertyKey;
//                lock.countDown();
//            }
//        });
//
//        //Update Property
//        digitalTwinState.deleteProperty(TEST_KEY_0001);
//
//        lock.await(2000, TimeUnit.MILLISECONDS);
//
//        assertEquals(TEST_KEY_0001, receivedPropertyKey);
//        assertTrue(receivedProperty.isPresent());
//        assertEquals(testProperty1, receivedProperty.get());
//        assertEquals(TEST_VALUE_0001, receivedProperty.get().getValue());
//    }

}
