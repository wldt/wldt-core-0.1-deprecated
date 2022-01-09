package it.unimore.wldt.test.model;

import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.model.ModelFunction;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateUpdateModelFunction extends ModelFunction {

    private static final Logger logger = LoggerFactory.getLogger(StateUpdateModelFunction.class);

    public static final String TEST_PROPERTY_KEY_0001 = "testKey0001";
    public static final String TEST_PROPERTY_VALUE_0001 = "TEST-STRING";
    public static final String TEST_PROPERTY_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public StateUpdateModelFunction(String id) {
        super(id);
    }

    @Override
    protected void onStart() throws ModelFunctionException {
        try {
            logger.debug("onStart()");
            createTestStateProperty();
        }catch (Exception e){
            throw new ModelFunctionException(e.getLocalizedMessage());
        }
    }

    @Override
    protected void onStop() throws ModelFunctionException {
        logger.debug("onStop()");
    }

    private void createTestStateProperty() throws WldtDigitalTwinStateException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException {

        if(this.digitalTwinState != null){
            DigitalTwinStateProperty<Object> newStateProperty = new DigitalTwinStateProperty<>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001);
            newStateProperty.setReadable(true);
            newStateProperty.setWritable(true);
            digitalTwinState.createProperty(TEST_PROPERTY_KEY_0001, newStateProperty);
        }
    }

    @Override
    protected void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.debug("onStateChangePropertyCreated()-> {}", digitalTwinStateProperty);
    }

    @Override
    protected void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.debug("onStateChangePropertyUpdated()-> {}", digitalTwinStateProperty);
    }

    @Override
    protected void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.debug("onStateChangePropertyDeleted()-> {}", digitalTwinStateProperty);
    }

    @Override
    protected void onStatePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.debug("onStatePropertyUpdated()-> {}", digitalTwinStateProperty);
    }

    @Override
    protected void onStatePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.debug("onStatePropertyDeleted()-> {}", digitalTwinStateProperty);
    }

    @Override
    protected void onPhysicalEvent() {

    }

    @Override
    protected void onDigitalEvent() {

    }
}
