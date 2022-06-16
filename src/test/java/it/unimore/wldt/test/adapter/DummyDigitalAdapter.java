package it.unimore.wldt.test.adapter;

import it.unimore.dipi.iot.wldt.adapter.*;
import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.event.PhysicalActionEventMessage;
import it.unimore.dipi.iot.wldt.event.PhysicalPropertyEventMessage;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Random;

public class DummyDigitalAdapter extends DigitalAdapter<DummyDigitalAdapterConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DummyDigitalAdapter.class);

    public DummyDigitalAdapter(String id, DummyDigitalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    @Override
    public void onAdapterStart() {
        logger.info("DummyDigitalTwinAdapter -> onAdapterStart()");
    }

    @Override
    public void onAdapterStop() {
        logger.info("DummyDigitalTwinAdapter -> onAdapterStop()");
    }

    @Override
    public void onDigitalTwinSync(IDigitalTwinState digitalTwinState) {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinSync() - DT State: {}", digitalTwinState);
    }

    @Override
    public void onDigitalTwinUnSync(IDigitalTwinState digitalTwinState) {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinUnSync() - DT State: {}", digitalTwinState);
    }

    @Override
    protected void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DummyDigitalTwinAdapter -> onStateChangePropertyCreated() - Property CREATED: {}", digitalTwinStateProperty);
    }


    @Override
    protected void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DummyDigitalTwinAdapter -> onStateChangePropertyCreated() - Property UPDATED: {}", digitalTwinStateProperty);
    }

    @Override
    protected void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DummyDigitalTwinAdapter -> onStateChangePropertyCreated() - Property DELETED: {}", digitalTwinStateProperty);
    }

    @Override
    protected void onStatePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {}

    @Override
    protected void onStatePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {}
}
