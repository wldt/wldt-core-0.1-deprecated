package it.unimore.wldt.test.state;

import it.unimore.dipi.iot.wldt.exception.WldtDigitalTwinStateException;
import it.unimore.dipi.iot.wldt.state.DefaultDigitalTwinState;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateListener;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class DigitalTwinStateObserverTester {

    public static final String TEST_KEY_0001 = "testKey0001";
    public static final String TEST_VALUE_0001 = "TEST-STRING";
    public static final String TEST_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public static IDigitalTwinState digitalTwinState = null;

    public static DigitalTwinStateProperty<String> testProperty1 = null;

    private CountDownLatch lock = new CountDownLatch(1);

    private Optional<DigitalTwinStateProperty<?>>  receivedProperty;
    private Optional<DigitalTwinStateProperty<?>>  receivedOriginalProperty;
    private String receivedPropertyKey;

    private Optional<DigitalTwinStateProperty<?>>  secondObserverReceivedProperty;
    private String secondObserverReceivedPropertyKey;

    private void initTestDtState() throws WldtDigitalTwinStateException {
        //Init DigitaTwin State
        digitalTwinState = new DefaultDigitalTwinState();
    }

    private void createProperty() throws WldtDigitalTwinStateException {
        testProperty1 = new DigitalTwinStateProperty<>();
        testProperty1.setKey(TEST_KEY_0001);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);
        testProperty1.setValue(TEST_VALUE_0001);

        digitalTwinState.createProperty(TEST_KEY_0001, testProperty1);
    }

    @Test
    public void observePropertyCreation() throws WldtDigitalTwinStateException, InterruptedException {

        //Init DigitaTwin State
        initTestDtState();

        digitalTwinState.observeState(new DigitalTwinStateListener() {
            @Override
            public void onPropertyCreated(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
                receivedProperty = dtStateProperty;
                receivedPropertyKey = propertyKey;
                lock.countDown();
            }

            @Override
            public void onPropertyUpdated(String propertyKey, Optional<DigitalTwinStateProperty<?>> previousDtStateProperty, Optional<DigitalTwinStateProperty<?>> currentDtStateProperty) {
            }

            @Override
            public void onPropertyDeleted(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
            }
        });

        createProperty();

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertEquals(TEST_KEY_0001, receivedPropertyKey);
        assertTrue(receivedProperty.isPresent());
        assertEquals(testProperty1, receivedProperty.get());
        assertEquals(TEST_VALUE_0001, receivedProperty.get().getValue());
    }

    @Test
    public void multipleObserversPropertyCreation() throws WldtDigitalTwinStateException, InterruptedException {

        //Init DigitaTwin State
        initTestDtState();

        digitalTwinState.observeState(new DigitalTwinStateListener() {
            @Override
            public void onPropertyCreated(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
                receivedProperty = dtStateProperty;
                receivedPropertyKey = propertyKey;
                lock.countDown();
            }

            @Override
            public void onPropertyUpdated(String propertyKey, Optional<DigitalTwinStateProperty<?>> previousDtStateProperty, Optional<DigitalTwinStateProperty<?>> currentDtStateProperty) {
            }

            @Override
            public void onPropertyDeleted(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
            }
        });

        digitalTwinState.observeState(new DigitalTwinStateListener() {
            @Override
            public void onPropertyCreated(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
                secondObserverReceivedProperty = dtStateProperty;
                secondObserverReceivedPropertyKey = propertyKey;
                lock.countDown();
            }

            @Override
            public void onPropertyUpdated(String propertyKey, Optional<DigitalTwinStateProperty<?>> previousDtStateProperty, Optional<DigitalTwinStateProperty<?>> currentDtStateProperty) {
            }

            @Override
            public void onPropertyDeleted(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
            }
        });

        createProperty();

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertEquals(TEST_KEY_0001, receivedPropertyKey);
        assertTrue(receivedProperty.isPresent());
        assertEquals(testProperty1, receivedProperty.get());
        assertEquals(TEST_VALUE_0001, receivedProperty.get().getValue());

        assertEquals(TEST_KEY_0001, secondObserverReceivedPropertyKey);
        assertTrue(secondObserverReceivedProperty.isPresent());
        assertEquals(testProperty1, secondObserverReceivedProperty.get());
        assertEquals(TEST_VALUE_0001, secondObserverReceivedProperty.get().getValue());
    }

    @Test
    public void observePropertyUpdate() throws WldtDigitalTwinStateException, InterruptedException {

        //Init DigitaTwin State
        initTestDtState();

        digitalTwinState.observeState(new DigitalTwinStateListener() {
            @Override
            public void onPropertyCreated(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
            }

            @Override
            public void onPropertyUpdated(String propertyKey, Optional<DigitalTwinStateProperty<?>> previousDtStateProperty, Optional<DigitalTwinStateProperty<?>> currentDtStateProperty) {
                receivedOriginalProperty = previousDtStateProperty;
                receivedProperty = currentDtStateProperty;
                receivedPropertyKey = propertyKey;
                lock.countDown();
            }

            @Override
            public void onPropertyDeleted(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
            }
        });

        createProperty();

        //Update Property
        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_KEY_0001, TEST_VALUE_0001_UPDATED, true, true);
        digitalTwinState.updateProperty(TEST_KEY_0001, updatedProperty);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertEquals(TEST_KEY_0001, receivedPropertyKey);
        assertTrue(receivedProperty.isPresent());
        assertTrue(receivedOriginalProperty.isPresent());
        assertEquals(testProperty1, receivedOriginalProperty.get());
        assertEquals(updatedProperty, receivedProperty.get());
        assertEquals(TEST_VALUE_0001, receivedOriginalProperty.get().getValue());
        assertEquals(TEST_VALUE_0001_UPDATED, receivedProperty.get().getValue());
    }

    @Test
    public void observePropertyDelete() throws WldtDigitalTwinStateException, InterruptedException {

        //Init DigitaTwin State
        initTestDtState();

        digitalTwinState.observeState(new DigitalTwinStateListener() {
            @Override
            public void onPropertyCreated(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
            }

            @Override
            public void onPropertyUpdated(String propertyKey, Optional<DigitalTwinStateProperty<?>> previousDtStateProperty, Optional<DigitalTwinStateProperty<?>> currentDtStateProperty) {
            }

            @Override
            public void onPropertyDeleted(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty) {
                receivedProperty = dtStateProperty;
                receivedPropertyKey = propertyKey;
                lock.countDown();
            }
        });

        createProperty();

        //Update Property
        digitalTwinState.deleteProperty(TEST_KEY_0001);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertEquals(TEST_KEY_0001, receivedPropertyKey);
        assertTrue(receivedProperty.isPresent());
        assertEquals(testProperty1, receivedProperty.get());
        assertEquals(TEST_VALUE_0001, receivedProperty.get().getValue());
    }

}
