package it.unimore.dipi.iot.wldt.model;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetEvent;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetProperty;
import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.event.physical.PhysicalAssetEventWldtEvent;
import it.unimore.dipi.iot.wldt.event.physical.PhysicalAssetPropertyWldtEvent;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.ModelException;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public abstract class ShadowingModelFunction implements WldtEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ShadowingModelFunction.class);

    private String id = null;

    private WldtEventFilter physicalEventsFilter = null;

    protected IDigitalTwinState digitalTwinState = null;

    private ShadowingModelFunction(){}

    private ShadowingModelListener shadowingModelListener;

    public ShadowingModelFunction(String id){
        this.id = id;
        this.physicalEventsFilter = new WldtEventFilter();
    }

    /**
     *
     * @param physicalAssetProperty
     * @throws EventBusException
     * @throws ModelException
     */
    protected void observePhysicalAssetProperty(PhysicalAssetProperty<?> physicalAssetProperty) throws EventBusException, ModelException {
        if(physicalAssetProperty == null)
            throw new ModelException("Error ! NULL PhysicalProperty ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetPropertyWldtEvent.buildEventType(physicalAssetProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, this);
    }

    /**
     *
     * @param physicalAssetPropertyList
     * @throws EventBusException
     * @throws ModelException
     */
    protected void observePhysicalAssetProperties(List<PhysicalAssetProperty<?>> physicalAssetPropertyList) throws EventBusException, ModelException {

        if(physicalAssetPropertyList == null)
            throw new ModelException("Error ! NULL PhysicalProperty List ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(PhysicalAssetProperty<?> physicalAssetProperty : physicalAssetPropertyList)
            wldtEventFilter.add(PhysicalAssetPropertyWldtEvent.buildEventType(physicalAssetProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, this);

    }

    /**
     *
     * @param physicalAssetProperty
     * @throws EventBusException
     * @throws ModelException
     */
    protected void unObservePhysicalAssetProperty(PhysicalAssetProperty<?> physicalAssetProperty) throws EventBusException, ModelException {

        if(physicalAssetProperty == null)
            throw new ModelException("Error ! NULL PhysicalProperty ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetPropertyWldtEvent.buildEventType(physicalAssetProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    /**
     *
     * @param physicalAssetPropertyList
     * @throws EventBusException
     * @throws ModelException
     */
    protected void unObservePhysicalAssetProperties(List<PhysicalAssetProperty<?>> physicalAssetPropertyList) throws EventBusException, ModelException {

        if(physicalAssetPropertyList == null)
            throw new ModelException("Error ! NULL PhysicalProperty List ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(PhysicalAssetProperty<?> physicalAssetProperty : physicalAssetPropertyList)
            wldtEventFilter.add(PhysicalAssetPropertyWldtEvent.buildEventType(physicalAssetProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    ///////////////////// PHYSICAL ASSET EVENT OBSERVATION MANAGEMENT ////////////////////////////////

    /**
     *
     * @param physicalAssetEvent
     * @throws EventBusException
     * @throws ModelException
     */
    protected void observePhysicalAssetEvent(PhysicalAssetEvent physicalAssetEvent) throws EventBusException, ModelException {
        if(physicalAssetEvent == null)
            throw new ModelException("Error ! NULL PhysicalAssetEvent ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetEventWldtEvent.buildEventType(physicalAssetEvent.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, this);
    }

    /**
     *
     * @param physicalAssetEventList
     * @throws EventBusException
     * @throws ModelException
     */
    protected void observePhysicalAssetEvents(List<PhysicalAssetEvent> physicalAssetEventList) throws EventBusException, ModelException {

        if(physicalAssetEventList == null)
            throw new ModelException("Error ! NULL PhysicalAssetEvent List ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(PhysicalAssetEvent physicalAssetEvent : physicalAssetEventList)
            wldtEventFilter.add(PhysicalAssetEventWldtEvent.buildEventType(physicalAssetEvent.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, this);

    }

    /**
     *
     * @param physicalAssetEvent
     * @throws EventBusException
     * @throws ModelException
     */
    protected void unObservePhysicalAssetEvent(PhysicalAssetEvent physicalAssetEvent) throws EventBusException, ModelException {

        if(physicalAssetEvent == null)
            throw new ModelException("Error ! NULL PhysicalAssetEvent ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetEventWldtEvent.buildEventType(physicalAssetEvent.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    /**
     *
     * @param physicalAssetEventList
     * @throws EventBusException
     * @throws ModelException
     */
    protected void unObservePhysicalAssetEvents(List<PhysicalAssetEvent> physicalAssetEventList) throws EventBusException, ModelException {

        if(physicalAssetEventList == null)
            throw new ModelException("Error ! NULL PhysicalAssetEvent List ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(PhysicalAssetEvent physicalAssetEvent : physicalAssetEventList)
            wldtEventFilter.add(PhysicalAssetEventWldtEvent.buildEventType(physicalAssetEvent.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("Shadowing Model Function -> Subscribed to: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("Shadowing Model Function -> Unsubscribed from: {}", eventType);
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {

        logger.info("Shadowing Model Function -> Received Event: {} Class: {}", wldtEvent, wldtEvent.getClass());

        if(wldtEvent != null && wldtEvent instanceof PhysicalAssetPropertyWldtEvent)
            onPhysicalAssetPropertyWldtEvent((PhysicalAssetPropertyWldtEvent<?>) wldtEvent);

        if(wldtEvent != null && wldtEvent instanceof PhysicalAssetEventWldtEvent)
            onPhysicalAssetEventWldtEvent((PhysicalAssetEventWldtEvent<?>) wldtEvent);
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

    abstract protected void onPhysicalAssetPropertyWldtEvent(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage);

    abstract protected void onPhysicalAssetEventWldtEvent(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WldtEventFilter getPhysicalEventsFilter() {
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
            getShadowingModelListener().onShadowingSync(digitalTwinState);
    }

    protected void notifyShadowingOutOfSync(){
        if(getShadowingModelListener() != null)
            getShadowingModelListener().onShadowingOutOfSync(digitalTwinState);
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
