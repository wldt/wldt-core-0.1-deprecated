package it.unimore.wldt.test.state;

import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.state.*;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class DigitalTwinStatePropertyObserverTester {

    public static final String TEST_PROPERTY_KEY_0001 = "testKey0001";
    public static final String TEST_PROPERTY_VALUE_0001 = "TEST-STRING";
    public static final String TEST_PROPERTY_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public static IDigitalTwinState digitalTwinState = null;

    public static DigitalTwinStateProperty<String> testProperty1 = null;

    private CountDownLatch lock = new CountDownLatch(1);

    private Optional<EventMessage<?>> propertyCreatedReceivedEventMessage;
    private DigitalTwinStateProperty<?>  propertyCreatedReceivedProperty;

    private Optional<EventMessage<?>> propertyUpdatedReceivedEventMessage;
    private DigitalTwinStateProperty<?> propertyUpdatedReceivedProperty;

    private Optional<EventMessage<?>> propertyDeletedReceivedEventMessage;
    private DigitalTwinStateProperty<?> propertyDeletedReceivedProperty;

    private void initTestDtState() {
        //Init DigitaTwin State
        digitalTwinState = new DefaultDigitalTwinState();
    }

    private void createProperty() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStateException {
        testProperty1 = new DigitalTwinStateProperty<>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);

        digitalTwinState.createProperty(TEST_PROPERTY_KEY_0001, testProperty1);
    }


    @Test
    public void observeStateCreatedProperty() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);

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

                propertyCreatedReceivedEventMessage = eventMessage;

                if(eventMessage.isPresent() && eventMessage.get().getBody() != null){

                    //If New Property Created
                    if(eventMessage.get().getBody() instanceof DigitalTwinStateProperty &&
                            eventMessage.get().getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED)) {

                        //Cast to the right property ! In that case a "simple" String
                        if((((DigitalTwinStateProperty<?>)eventMessage.get().getBody()).getValue()) instanceof String){
                            DigitalTwinStateProperty<String> digitalTwinStateProperty = (DigitalTwinStateProperty<String>) eventMessage.get().getBody();
                            propertyCreatedReceivedProperty = digitalTwinStateProperty;
                        }
                    }

                }

                lock.countDown();
            }
        });

        createProperty();

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertTrue(propertyCreatedReceivedEventMessage.isPresent());
        assertEquals(propertyCreatedReceivedEventMessage.get().getType(), DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);
        assertEquals(propertyCreatedReceivedEventMessage.get().getBody(), testProperty1);
        assertEquals(propertyCreatedReceivedProperty, testProperty1);

    }

    @Test
    public void observeStateUpdatedProperty() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        createProperty();

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);

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

                propertyUpdatedReceivedEventMessage = eventMessage;

                if(eventMessage.isPresent() && eventMessage.get().getBody() != null){

                    //If New Property Created
                    if(eventMessage.get().getBody() instanceof DigitalTwinStateProperty &&
                            eventMessage.get().getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED)) {

                        //Cast to the right property ! In that case a "simple" String
                        if((((DigitalTwinStateProperty<?>)eventMessage.get().getBody()).getValue()) instanceof String){
                            DigitalTwinStateProperty<String> digitalTwinStateProperty = (DigitalTwinStateProperty<String>) eventMessage.get().getBody();
                            propertyUpdatedReceivedProperty = digitalTwinStateProperty;
                        }
                    }

                }

                lock.countDown();
            }
        });

        //Update Property
        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001_UPDATED, true, true);
        digitalTwinState.updateProperty(TEST_PROPERTY_KEY_0001, updatedProperty);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertTrue(propertyUpdatedReceivedEventMessage.isPresent());
        assertEquals(propertyUpdatedReceivedEventMessage.get().getType(), DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);
        assertEquals(propertyUpdatedReceivedEventMessage.get().getBody(), updatedProperty);
        assertEquals(propertyUpdatedReceivedProperty, updatedProperty);

    }

    @Test
    public void observeStateDeletedProperty() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        createProperty();

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);

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

                propertyDeletedReceivedEventMessage = eventMessage;

                if(eventMessage.isPresent() && eventMessage.get().getBody() != null){

                    //If New Property Created
                    if(eventMessage.get().getBody() instanceof DigitalTwinStateProperty &&
                            eventMessage.get().getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED)) {

                        //Cast to the right property ! In that case a "simple" String
                        if((((DigitalTwinStateProperty<?>)eventMessage.get().getBody()).getValue()) instanceof String){
                            DigitalTwinStateProperty<String> digitalTwinStateProperty = (DigitalTwinStateProperty<String>) eventMessage.get().getBody();
                            propertyDeletedReceivedProperty = digitalTwinStateProperty;
                        }
                    }

                }

                lock.countDown();
            }
        });

        digitalTwinState.deleteProperty(TEST_PROPERTY_KEY_0001);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertTrue(propertyDeletedReceivedEventMessage.isPresent());
        assertEquals(propertyDeletedReceivedEventMessage.get().getType(), DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);
        assertEquals(propertyDeletedReceivedEventMessage.get().getBody(), testProperty1);
        assertEquals(propertyDeletedReceivedProperty, testProperty1);

    }

    @Test
    public void observePropertyUpdated() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();
        createProperty();

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(digitalTwinState.getPropertyUpdatedEventMessageType(TEST_PROPERTY_KEY_0001));

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

                propertyUpdatedReceivedEventMessage = eventMessage;

                if(eventMessage.isPresent() && eventMessage.get().getBody() != null){

                    //If New Property Created
                    if(eventMessage.get().getBody() instanceof DigitalTwinStateProperty &&
                            eventMessage.get().getType().equals(digitalTwinState.getPropertyUpdatedEventMessageType(TEST_PROPERTY_KEY_0001))) {

                        //Cast to the right property ! In that case a "simple" String
                        if((((DigitalTwinStateProperty<?>)eventMessage.get().getBody()).getValue()) instanceof String){
                            DigitalTwinStateProperty<String> digitalTwinStateProperty = (DigitalTwinStateProperty<String>) eventMessage.get().getBody();
                            propertyUpdatedReceivedProperty = digitalTwinStateProperty;
                        }
                    }
                }

                lock.countDown();
            }
        });

        //Update Property
        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001_UPDATED, true, true);
        digitalTwinState.updateProperty(TEST_PROPERTY_KEY_0001, updatedProperty);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertTrue(propertyUpdatedReceivedEventMessage.isPresent());
        assertEquals(propertyUpdatedReceivedEventMessage.get().getType(), digitalTwinState.getPropertyUpdatedEventMessageType(TEST_PROPERTY_KEY_0001));
        assertEquals(propertyUpdatedReceivedEventMessage.get().getBody(), updatedProperty);
        assertEquals(propertyUpdatedReceivedProperty, updatedProperty);

    }

    @Test
    public void observePropertyDeleted() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();
        createProperty();

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(digitalTwinState.getPropertyDeletedEventMessageType(TEST_PROPERTY_KEY_0001));

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

                propertyDeletedReceivedEventMessage = eventMessage;

                if(eventMessage.isPresent() && eventMessage.get().getBody() != null){

                    //If New Property Created
                    if(eventMessage.get().getBody() instanceof DigitalTwinStateProperty &&
                            eventMessage.get().getType().equals(digitalTwinState.getPropertyDeletedEventMessageType(TEST_PROPERTY_KEY_0001))) {

                        //Cast to the right property ! In that case a "simple" String
                        if((((DigitalTwinStateProperty<?>)eventMessage.get().getBody()).getValue()) instanceof String){
                            DigitalTwinStateProperty<String> digitalTwinStateProperty = (DigitalTwinStateProperty<String>) eventMessage.get().getBody();
                            propertyDeletedReceivedProperty = digitalTwinStateProperty;
                        }
                    }
                }

                lock.countDown();
            }
        });

        digitalTwinState.deleteProperty(TEST_PROPERTY_KEY_0001);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertTrue(propertyDeletedReceivedEventMessage.isPresent());
        assertEquals(propertyDeletedReceivedEventMessage.get().getType(), digitalTwinState.getPropertyDeletedEventMessageType(TEST_PROPERTY_KEY_0001));
        assertEquals(propertyDeletedReceivedEventMessage.get().getBody(), testProperty1);
        assertEquals(propertyDeletedReceivedProperty, testProperty1);

    }

}
