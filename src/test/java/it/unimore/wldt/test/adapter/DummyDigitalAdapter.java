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
import java.util.List;
import java.util.Random;

public class DummyDigitalAdapter extends DigitalAdapter<DummyDigitalAdapterConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DummyDigitalAdapter.class);

    private List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyCreatedMessageList = null;

    private List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyUpdateMessageList = null;

    private List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyDeletedMessageList = null;

    private List<IDigitalTwinState> receivedDigitalAdapterSyncDigitalTwinStateList = null;

    public DummyDigitalAdapter(String id, DummyDigitalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    public DummyDigitalAdapter(String id, DummyDigitalAdapterConfiguration configuration,
                               List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyCreatedMessageList,
                               List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyUpdateMessageList,
                               List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyDeletedMessageList,
                               List<IDigitalTwinState> receivedDigitalAdapterSyncDigitalTwinStateList
                               ) {

        super(id, configuration);

        this.receivedDigitalTwinPropertyCreatedMessageList = receivedDigitalTwinPropertyCreatedMessageList;
        this.receivedDigitalTwinPropertyUpdateMessageList = receivedDigitalTwinPropertyUpdateMessageList;
        this.receivedDigitalTwinPropertyDeletedMessageList = receivedDigitalTwinPropertyDeletedMessageList;
        this.receivedDigitalAdapterSyncDigitalTwinStateList = receivedDigitalAdapterSyncDigitalTwinStateList;
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

        if(this.receivedDigitalAdapterSyncDigitalTwinStateList != null)
            this.receivedDigitalAdapterSyncDigitalTwinStateList.add(digitalTwinState);
    }

    @Override
    public void onDigitalTwinUnSync(IDigitalTwinState digitalTwinState) {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinUnSync() - DT State: {}", digitalTwinState);
    }

    @Override
    protected void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DummyDigitalTwinAdapter -> onStateChangePropertyCreated() - Property CREATED: {}", digitalTwinStateProperty);

        if(this.receivedDigitalTwinPropertyCreatedMessageList != null)
            this.receivedDigitalTwinPropertyCreatedMessageList.add(digitalTwinStateProperty);
    }

    @Override
    protected void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DummyDigitalTwinAdapter -> onStateChangePropertyUpdated() - Property UPDATED: {}", digitalTwinStateProperty);

        if(this.receivedDigitalTwinPropertyUpdateMessageList != null)
            this.receivedDigitalTwinPropertyUpdateMessageList.add(digitalTwinStateProperty);
    }

    @Override
    protected void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DummyDigitalTwinAdapter -> onStateChangePropertyDeleted() - Property DELETED: {}", digitalTwinStateProperty);

        if(this.receivedDigitalTwinPropertyDeletedMessageList != null)
            this.receivedDigitalTwinPropertyDeletedMessageList.add(digitalTwinStateProperty);
    }

    @Override
    protected void onStatePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {}

    @Override
    protected void onStatePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {}

    public List<DigitalTwinStateProperty<?>> getReceivedDigitalTwinPropertyCreatedMessageList() {
        return receivedDigitalTwinPropertyCreatedMessageList;
    }

    public List<DigitalTwinStateProperty<?>> getReceivedDigitalTwinPropertyUpdateMessageList() {
        return receivedDigitalTwinPropertyUpdateMessageList;
    }

    public List<DigitalTwinStateProperty<?>> getReceivedDigitalTwinPropertyDeletedMessageList() {
        return receivedDigitalTwinPropertyDeletedMessageList;
    }

    public List<IDigitalTwinState> getReceivedDigitalAdapterSyncDigitalTwinStateList() {
        return receivedDigitalAdapterSyncDigitalTwinStateList;
    }
}
