package it.unimore.dipi.iot.wldt.model;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.adapter.PhysicalProperty;
import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.ModelException;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public abstract class ShadowingModelFunction implements EventListener {

    private static final Logger logger = LoggerFactory.getLogger(ShadowingModelFunction.class);

    private String id = null;

    private EventFilter physicalEventsFilter = null;

    protected IDigitalTwinState digitalTwinState = null;

    private ShadowingModelFunction(){}

    private ShadowingModelListener shadowingModelListener;

    public ShadowingModelFunction(String id){
        this.id = id;
        this.physicalEventsFilter = new EventFilter();
    }

    /**
     *
     * @param physicalProperty
     * @throws EventBusException
     * @throws ModelException
     */
    protected void observePhysicalProperty(PhysicalProperty<?> physicalProperty) throws EventBusException, ModelException {
        if(physicalProperty == null)
            throw new ModelException("Error ! NULL PhysicalProperty ...");

        //Define EventFilter and add the target topics
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(PhysicalPropertyEventMessage.buildEventType(physicalProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(eventFilter);

        EventBus.getInstance().subscribe(this.id, eventFilter, this);
    }

    /**
     *
     * @param physicalPropertyList
     * @throws EventBusException
     * @throws ModelException
     */
    protected void observePhysicalProperties(List<PhysicalProperty<?>> physicalPropertyList) throws EventBusException, ModelException {

        if(physicalPropertyList == null)
            throw new ModelException("Error ! NULL PhysicalProperty List ...");

        //Define EventFilter and add the target topics
        EventFilter eventFilter = new EventFilter();

        for(PhysicalProperty<?> physicalProperty : physicalPropertyList)
            eventFilter.add(PhysicalPropertyEventMessage.buildEventType(physicalProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(eventFilter);

        EventBus.getInstance().subscribe(this.id, eventFilter, this);

    }

    /**
     *
     * @param physicalProperty
     * @throws EventBusException
     * @throws ModelException
     */
    protected void unObservePhysicalProperty(PhysicalProperty<?> physicalProperty) throws EventBusException, ModelException {

        if(physicalProperty == null)
            throw new ModelException("Error ! NULL PhysicalProperty ...");

        //Define EventFilter and add the target topics
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(PhysicalPropertyEventMessage.buildEventType(physicalProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(eventFilter);

        EventBus.getInstance().unSubscribe(this.id, eventFilter, this);
    }

    /**
     *
     * @param physicalPropertyList
     * @throws EventBusException
     * @throws ModelException
     */
    protected void unObservePhysicalProperties(List<PhysicalProperty<?>> physicalPropertyList) throws EventBusException, ModelException {

        if(physicalPropertyList == null)
            throw new ModelException("Error ! NULL PhysicalProperty List ...");

        //Define EventFilter and add the target topics
        EventFilter eventFilter = new EventFilter();

        for(PhysicalProperty<?> physicalProperty : physicalPropertyList)
            eventFilter.add(PhysicalPropertyEventMessage.buildEventType(physicalProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(eventFilter);

        EventBus.getInstance().unSubscribe(this.id, eventFilter, this);
    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("Shadowing Model Function -> Subscribed to: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("Shadowing Model Function -> Unsubscribed from: {}", eventType);
    }

    @Override
    public void onEvent(Optional<EventMessage<?>> eventMessage) {
        logger.info("Shadowing Model Function -> Received Event: {}", eventMessage);
        if(eventMessage.isPresent() && eventMessage.get() instanceof PhysicalPropertyEventMessage){
            onPhysicalEvent((PhysicalPropertyEventMessage<?>) eventMessage.get());
        }
    }

    protected void init(IDigitalTwinState digitalTwinState){
        this.digitalTwinState = digitalTwinState;
    }

    abstract protected void onCreate();

    abstract protected void onStart();

    abstract protected void onStop();

    abstract protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap);

    abstract protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage);

    abstract protected void onPhysicalAdapterBidingUpdate(String adapterId, PhysicalAssetDescription adapterPhysicalAssetDescription);

    abstract protected void onPhysicalEvent(PhysicalPropertyEventMessage<?> physicalPropertyEventMessage);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventFilter getPhysicalEventsFilter() {
        return physicalEventsFilter;
    }

    public ShadowingModelListener getShadowingModelListener() {
        return shadowingModelListener;
    }

    public void setShadowingModelListener(ShadowingModelListener shadowingModelListener) {
        this.shadowingModelListener = shadowingModelListener;
    }

    protected void notifyShadowingSync(){
        if(getShadowingModelListener() != null)
            getShadowingModelListener().onShadowingSync();
    }

    protected void notifyShadowingOutOfSync(){
        if(getShadowingModelListener() != null)
            getShadowingModelListener().onShadowingOutOfSync();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShadowingModelFunction that = (ShadowingModelFunction) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModelFunction{");
        sb.append("id='").append(id).append('\'');
        sb.append(", physicalEventsFilter=").append(physicalEventsFilter);
        sb.append('}');
        return sb.toString();
    }
}
