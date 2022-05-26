package it.unimore.dipi.iot.wldt.adapter;

import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.PhysicalAdapterException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.util.Optional;

public abstract class PhysicalAdapter<C> extends WldtWorker implements EventListener{

    private static final Logger logger = LoggerFactory.getLogger(PhysicalAdapter.class);

    private String id;

    private C configuration;

    private EventFilter physicalActionEventsFilter;

    private PhysicalAdapterListener physicalAdapterListener;

    private PhysicalAdapter(){}

    private PhysicalAssetDescription adapterPhysicalAssetDescription;

    public PhysicalAdapter(String id, C configuration){
        this.id = id;
        this.configuration = configuration;
    }

    @Override
    public void onWorkerCreated() throws WldtRuntimeException {
        try{
            onAdapterCreate();
        }catch (Exception e){
            //Notify Listeners
            if(getPhysicalAdapterListener() != null)
                getPhysicalAdapterListener().onPhysicalAdapterUnBound(this.id, this.adapterPhysicalAssetDescription, e.getLocalizedMessage());

            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStop() throws WldtRuntimeException {
        try{

            onAdapterStop();

            if(getPhysicalAdapterListener() != null)
                getPhysicalAdapterListener().onPhysicalAdapterUnBound(this.id, this.adapterPhysicalAssetDescription, null);

        }catch (Exception e){

            //Notify Listeners
            if(getPhysicalAdapterListener() != null)
                getPhysicalAdapterListener().onPhysicalAdapterUnBound(this.id, this.adapterPhysicalAssetDescription, e.getLocalizedMessage());

            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try{
            onAdapterStart();
        }catch (Exception e){

            //Notify Listeners
            if(getPhysicalAdapterListener() != null)
                getPhysicalAdapterListener().onPhysicalAdapterUnBound(this.id, this.adapterPhysicalAssetDescription, e.getLocalizedMessage());

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

    public abstract void onAdapterStart();

    public abstract void onAdapterStop();

    protected void publishPhysicalEventMessage(PhysicalEventMessage<?> targetPhysicalEventMessage) throws EventBusException {
        EventBus.getInstance().publishEvent(getId(), targetPhysicalEventMessage);
    }

    public PhysicalAssetDescription getAdapterPhysicalAssetState() {
        return adapterPhysicalAssetDescription;
    }

    /**
     * This method allows an implementation of a PhysicalAdapter to notify the DigitalTwin that the representation
     * of the PhysicalAssetState is changed and should be potentially handled by other modules and core components.
     *
     * @param physicalAssetDescription
     * @throws PhysicalAdapterException
     * @throws EventBusException
     */
    protected void notifyPhysicalAssetBindingUpdate(PhysicalAssetDescription physicalAssetDescription) throws PhysicalAdapterException, EventBusException {

        if(physicalAssetDescription == null)
            throw new PhysicalAdapterException("Error updating AdapterPhysicalAssetState ! Provided State = Null.");

        updateAdapterPhysicalAssetDescription(physicalAssetDescription);

        //Notify Listeners
        if(getPhysicalAdapterListener() != null)
            getPhysicalAdapterListener().onPhysicalBindingUpdate(getId(), this.adapterPhysicalAssetDescription);
    }

    /**
     * This method allows an implementation of a Physical Adapter to notify active listeners
     * when there is an issue in the binding with the Physical Asset. If the binding is restored
     * the adapter can use notifyPhysicalAssetBindingUpdate to notify the binding update.
     *
     * @param errorMessage
     */
    protected void notifyPhysicalAdapterUnBound(String errorMessage){
        //Notify Listeners
        if(getPhysicalAdapterListener() != null)
            getPhysicalAdapterListener().onPhysicalAdapterUnBound(getId(), this.adapterPhysicalAssetDescription, errorMessage);
    }

    protected void notifyPhysicalAdapterBound(PhysicalAssetDescription physicalAssetDescription) throws PhysicalAdapterException, EventBusException {

        if(physicalAssetDescription == null)
            throw new PhysicalAdapterException("Error updating AdapterPhysicalAssetState ! Provided Description = Null.");

        updateAdapterPhysicalAssetDescription(physicalAssetDescription);

        //Notify Listeners
        if(getPhysicalAdapterListener() != null)
            getPhysicalAdapterListener().onPhysicalAdapterBound(getId(), this.adapterPhysicalAssetDescription);
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
     * @param physicalAssetDescription
     * @throws PhysicalAdapterException
     * @throws EventBusException
     */
    private void updateAdapterPhysicalAssetDescription(PhysicalAssetDescription physicalAssetDescription) throws PhysicalAdapterException, EventBusException {

        if(physicalAssetDescription == null)
            throw new PhysicalAdapterException("Error updating AdapterPhysicalAssetState ! Provided State = Null.");

        this.adapterPhysicalAssetDescription = physicalAssetDescription;

        if(physicalAssetDescription.getActions() != null && physicalAssetDescription.getActions().size() > 0) {

            //Handle PhysicalActionEvent EventFilter
            if(this.physicalActionEventsFilter == null)
                this.physicalActionEventsFilter = new EventFilter();
            else {
                //Clean existing subscriptions and the local event filter
                EventBus.getInstance().unSubscribe(this.id, this.physicalActionEventsFilter, this);
                this.physicalActionEventsFilter.clear();
            }

            //Create/Update the event filter and handle subscription
            for(PhysicalAction physicalAction : physicalAssetDescription.getActions())
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
