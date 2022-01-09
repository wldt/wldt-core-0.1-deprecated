package it.unimore.wldt.test.model;

import it.unimore.dipi.iot.wldt.exception.ModelFunctionException;
import it.unimore.dipi.iot.wldt.model.ModelFunction;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObserverModelFunction extends ModelFunction {

    private static final Logger logger = LoggerFactory.getLogger(ObserverModelFunction.class);

    public ObserverModelFunction(String id) {
        super(id);
    }

    @Override
    protected void onStart() throws ModelFunctionException {
        logger.debug("onStart()");
    }

    @Override
    protected void onStop() throws ModelFunctionException {
        logger.debug("onStop()");
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
