package it.unimore.wldt.test.state;

import it.unimore.dipi.iot.wldt.exception.WldtDigitalTwinStateException;
import it.unimore.dipi.iot.wldt.state.DefaultDigitalTwinState;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
import org.junit.Test;

import static org.junit.Assert.*;


public class DigitalTwinStatePropertyCRUDTester {

    public static final String TEST_KEY_0001 = "testKey0001";
    public static final String TEST_VALUE_0001 = "TEST-STRING";
    public static final String TEST_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public static IDigitalTwinState digitalTwinState = null;

    public static DigitalTwinStateProperty<String> testProperty1 = null;

    private void initTestDtState() throws WldtDigitalTwinStateException {

        //Init DigitaTwin State
        digitalTwinState = new DefaultDigitalTwinState();

        testProperty1 = new DigitalTwinStateProperty<>();
        testProperty1.setKey(TEST_KEY_0001);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);
        testProperty1.setValue(TEST_VALUE_0001);

        digitalTwinState.createProperty(TEST_KEY_0001, testProperty1);

    }

    @Test
    public void createProperty() throws WldtDigitalTwinStateException {

        //Init DigitaTwin State
        initTestDtState();

        assertTrue(digitalTwinState.getPropertyList().isPresent());
        assertEquals(1, digitalTwinState.getPropertyList().get().size());
        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(testProperty1, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
    }

    @Test
    public void readProperty() throws WldtDigitalTwinStateException {

        //Init DigitaTwin State
        initTestDtState();

        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(testProperty1, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
    }

    @Test
    public void updateProperty() throws WldtDigitalTwinStateException {

        //Init DigitaTwin State
        initTestDtState();

        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_KEY_0001, TEST_VALUE_0001_UPDATED, true, true);

        digitalTwinState.updateProperty(TEST_KEY_0001, updatedProperty);

        assertTrue(digitalTwinState.getPropertyList().isPresent());
        assertEquals(1, digitalTwinState.getPropertyList().get().size());
        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(updatedProperty, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001_UPDATED, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
    }

    @Test
    public void deleteProperty() throws WldtDigitalTwinStateException {
        //Init DigitalTwin State
        initTestDtState();

        //Remove target property
        digitalTwinState.deleteProperty(TEST_KEY_0001);
        assertFalse(digitalTwinState.getPropertyList().isPresent());
    }

}
