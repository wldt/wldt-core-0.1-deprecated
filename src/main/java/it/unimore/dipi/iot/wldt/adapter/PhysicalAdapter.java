package it.unimore.dipi.iot.wldt.adapter;

import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.PhysicalAdapterException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class PhysicalAdapter<C> extends WldtWorker implements EventListener{

    private static final Logger logger = LoggerFactory.getLogger(PhysicalAdapter.class);

    private String id;

    private C configuration;

    private EventFilter physicalActionEventsFilter;

    private PhysicalAdapterListener physicalAdapterListener;

    private PhysicalAdapter(){}

    private PhysicalAssetState adapterPhysicalAssetState;

    public PhysicalAdapter(String id, C configuration){
        this.id = id;
        this.configuration = configuration;
    }

    @Override
    public void onWorkerCreated() throws WldtRuntimeException {
        try{
            onAdapterCreate();
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStop() throws WldtRuntimeException {
        try{
            onAdapterStop();
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try{

            Optional<PhysicalAssetState> optionalPhysicalAssetState = onAdapterStart();

            if(optionalPhysicalAssetState.isPresent())
                updateAdapterPhysicalAssetState(optionalPhysicalAssetState.get());

            if(getPhysicalAdapterListener() != null)
                getPhysicalAdapterListener().onBound(getId(), this.adapterPhysicalAssetState);

        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    public String getId() {
        return id;
    }

    public C getConfiguration() {
        return configuration;
    }

    public PhysicalAdapterListener getPhysicalAdapterListener() {
        return physicalAdapterListener;
    }

    public void setPhysicalAdapterListener(PhysicalAdapterListener physicalAdapterListener) {
        this.physicalAdapterListener = physicalAdapterListener;
    }

    public abstract void onIncomingPhysicalAction(PhysicalActionEventMessage<?> physicalActionEventMessage);

    public abstract void onAdapterCreate();

    public abstract Optional<PhysicalAssetState> onAdapterStart();

    public abstract void onAdapterStop();

    public PhysicalAssetState getAdapterPhysicalAssetState() {
        return adapterPhysicalAssetState;
    }

    /**
     * This method allows an implementation of a PhysicalAdapter to notify the DigitalTwin that the representation
     * of the PhysicalAssetState is changed and should be potentially handled by other modules and core components.
     *
     * @param physicalAssetState
     * @throws PhysicalAdapterException
     * @throws EventBusException
     */
    protected void notifyPhysicalAssetStateVariation(PhysicalAssetState physicalAssetState) throws PhysicalAdapterException, EventBusException {

        if(physicalAssetState == null)
            throw new PhysicalAdapterException("Error updating AdapterPhysicalAssetState ! Provided State = Null.");

        updateAdapterPhysicalAssetState(physicalAssetState);

        if(getPhysicalAdapterListener() != null)
            getPhysicalAdapterListener().onBindingUpdate(getId(), this.adapterPhysicalAssetState);
    }

    /**
     * Handle and update of the Physical Asset State.
     * This method is automatically internally called by the PhysicalAdapter basic class after the adapter startup and
     * can be manually called by an adapter implementation through the method notifyPhysicalAssetStateVariation()
     * if it detects a variation in the state of the Physical Asset.
     *
     * This method manages the proper subscription to receive action events from other DT's modules according to the
     * exposed actions in the PhysicalAssetState.
     *
     * @param physicalAssetState
     * @throws PhysicalAdapterException
     * @throws EventBusException
     */
    private void updateAdapterPhysicalAssetState(PhysicalAssetState physicalAssetState) throws PhysicalAdapterException, EventBusException {

        if(physicalAssetState == null)
            throw new PhysicalAdapterException("Error updating AdapterPhysicalAssetState ! Provided State = Null.");

        this.adapterPhysicalAssetState = physicalAssetState;

        if(physicalAssetState.getActions() != null && physicalAssetState.getActions().size() > 0) {

            //Handle PhysicalActionEvent EventFilter
            if(this.physicalActionEventsFilter == null)
                this.physicalActionEventsFilter = new EventFilter();
            else {
                //Clean existing subscriptions and the local event filter
                EventBus.getInstance().unSubscribe(this.id, this.physicalActionEventsFilter, this);
                this.physicalActionEventsFilter.clear();
            }

            //Create/Update the event filter and handle subscription
            for(PhysicalAction physicalAction : physicalAssetState.getActions())
                this.physicalActionEventsFilter.add(PhysicalActionEventMessage.buildEventType(physicalAction.getKey()));

            EventBus.getInstance().subscribe(this.id, this.physicalActionEventsFilter, this);

        }
        else
            logger.info("No Supported Action Exposed. Subscription to PhysicalActionEvents not required !");
    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.debug("{} -> Subscribed to: {}", id, eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.debug("{} -> Unsubscribed from: {}", id, eventType);
    }

    @Override
    public void onEvent(Optional<EventMessage<?>> eventMessage) {
        logger.debug("{} -> Received Event: {}", id, eventMessage);
        if (eventMessage.isPresent() && eventMessage.get() instanceof PhysicalActionEventMessage) {
            onIncomingPhysicalAction((PhysicalActionEventMessage<?>) eventMessage.get());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalAdapter<?> that = (PhysicalAdapter<?>) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
