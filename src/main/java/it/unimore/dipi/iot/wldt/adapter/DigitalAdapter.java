package it.unimore.dipi.iot.wldt.adapter;

import it.unimore.dipi.iot.wldt.engine.LifeCycleListener;
import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.event.EventFilter;
import it.unimore.dipi.iot.wldt.event.EventListener;
import it.unimore.dipi.iot.wldt.event.EventMessage;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.PhysicalAdapterException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import it.unimore.dipi.iot.wldt.state.DefaultDigitalTwinState;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
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
public abstract class DigitalAdapter<C> extends WldtWorker implements EventListener, LifeCycleListener {

    private static final Logger logger = LoggerFactory.getLogger(DigitalAdapter.class);

    private String id = null;

    private C configuration;

    private EventFilter stateEventFilter = null;

    private EventFilter statePropertyEventsFilter = null;

    private boolean observeDigitalTwinState = true;

    protected IDigitalTwinState digitalTwinState = null;

    private DigitalAdapterListener digitalAdapterListener;

    private final EventListener digitalTwinStateEventListener = new EventListener() {
        @Override
        public void onEventSubscribed(String eventType) {
            //TODO Implement
        }

        @Override
        public void onEventUnSubscribed(String eventType) {
            //TODO Implement
        }

        @Override
        public void onEvent(EventMessage<?> eventMessage) {

            if(eventMessage != null && eventMessage.getBody() != null && (eventMessage.getBody() instanceof DigitalTwinStateProperty)){
                DigitalTwinStateProperty<?> digitalTwinStateProperty = (DigitalTwinStateProperty<?>) eventMessage.getBody();

                if(eventMessage.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED))
                    onStateChangePropertyCreated(digitalTwinStateProperty);

                if(eventMessage.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED))
                    onStateChangePropertyUpdated(digitalTwinStateProperty);

                if(eventMessage.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED))
                    onStateChangePropertyDeleted(digitalTwinStateProperty);
            }
        }
    };

    private DigitalAdapter(){}

    public DigitalAdapter(String id, C configuration){
        this.id = id;
        this.configuration = configuration;
    }

    public DigitalAdapter(String id, boolean observeDigitalTwinState){
        this.id = id;
        this.observeDigitalTwinState = observeDigitalTwinState;
    }

    protected void observeDigitalTwinState() throws EventBusException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);

        //Save the adopted EventFilter
        this.stateEventFilter = eventFilter;

        EventBus.getInstance().subscribe(this.id, eventFilter, digitalTwinStateEventListener);
    }

    protected void unObserveDigitalTwinState() throws EventBusException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);

        //Save the adopted EventFilter
        this.stateEventFilter = eventFilter;

        EventBus.getInstance().unSubscribe(this.id, eventFilter, digitalTwinStateEventListener);
    }

    protected void observeDigitalTwinProperties(List<String> propertyList) throws EventBusException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();

        for(String propertyKey : propertyList) {
            eventFilter.add(digitalTwinState.getPropertyUpdatedEventMessageType(propertyKey));
            eventFilter.add(digitalTwinState.getPropertyDeletedEventMessageType(propertyKey));
        }

        //Save the adopted EventFilter
        this.statePropertyEventsFilter.addAll(eventFilter);

        EventBus.getInstance().subscribe(this.id, eventFilter, this);

    }

    protected void unObserveDigitalTwinProperties(List<String> propertyList) throws EventBusException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();

        for(String propertyKey : propertyList) {
            eventFilter.add(digitalTwinState.getPropertyUpdatedEventMessageType(propertyKey));
            eventFilter.add(digitalTwinState.getPropertyDeletedEventMessageType(propertyKey));
        }

        //Save the adopted EventFilter
        this.statePropertyEventsFilter.removeAll(eventFilter);

        EventBus.getInstance().unSubscribe(this.id, eventFilter, this);
    }

    protected void observeDigitalTwinProperty(String propertyKey) throws EventBusException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();

        eventFilter.add(digitalTwinState.getPropertyUpdatedEventMessageType(propertyKey));
        eventFilter.add(digitalTwinState.getPropertyDeletedEventMessageType(propertyKey));

        //Save the adopted EventFilter
        this.statePropertyEventsFilter.addAll(eventFilter);

        EventBus.getInstance().subscribe(this.id, eventFilter, this);
    }

    protected void unObserveDigitalTwinProperty(String propertyKey) throws EventBusException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();

        eventFilter.add(digitalTwinState.getPropertyUpdatedEventMessageType(propertyKey));
        eventFilter.add(digitalTwinState.getPropertyDeletedEventMessageType(propertyKey));

        //Save the adopted EventFilter
        this.statePropertyEventsFilter.removeAll(eventFilter);

        EventBus.getInstance().unSubscribe(this.id, eventFilter, this);
    }

    /**
     * This method allows an implementation of a Digital Adapter to notify active listeners
     * when there is an issue in the binding with the Digital Asset.
     *
     * @param errorMessage
     */
    protected void notifyDigitalAdapterUnBound(String errorMessage){
        //Notify Listeners
        if(getDigitalAdapterListener() != null)
            getDigitalAdapterListener().onDigitalAdapterUnBound(getId(), errorMessage);
    }

    /**
     * This method allows an implementation of a Digital Adapter to notify active listeners when
     * the adapter is ready to work and correctly bound to the associated external digital services.
     *
     * @throws PhysicalAdapterException
     * @throws EventBusException
     */
    protected void notifyPhysicalAdapterBound() throws PhysicalAdapterException, EventBusException {

        //Notify Listeners
        if(getDigitalAdapterListener() != null)
            getDigitalAdapterListener().onDigitalAdapterBound(getId());
    }

    public void init(IDigitalTwinState digitalTwinState){
        this.digitalTwinState = digitalTwinState;
    }

    abstract protected void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStatePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStatePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    public abstract void onAdapterStart();

    public abstract void onAdapterStop();

    public abstract void onDigitalTwinSync(IDigitalTwinState digitalTwinState);

    public abstract void onDigitalTwinUnSync(IDigitalTwinState digitalTwinState);

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try{
            onAdapterStart();
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStop() throws WldtRuntimeException {
        try{
            unObserveDigitalTwinState();
            onAdapterStop();
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    public C getConfiguration() {
        return configuration;
    }

    public void setConfiguration(C configuration) {
        this.configuration = configuration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public DigitalAdapterListener getDigitalAdapterListener() {
        return digitalAdapterListener;
    }

    public void setDigitalAdapterListener(DigitalAdapterListener digitalAdapterListener) {
        this.digitalAdapterListener = digitalAdapterListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DigitalAdapter that = (DigitalAdapter) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("Subscribed to: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("UnSubscribed from: {}", eventType);
    }

    @Override
    public void onEvent(EventMessage<?> eventMessage) {
        if(eventMessage != null && eventMessage.getBody() != null && (eventMessage.getBody() instanceof DigitalTwinStateProperty)){
            DigitalTwinStateProperty<?> digitalTwinStateProperty = (DigitalTwinStateProperty<?>) eventMessage.getBody();
            if(eventMessage.getType().equals(digitalTwinState.getPropertyCreatedEventMessageType(digitalTwinStateProperty.getKey())))
                onStateChangePropertyCreated(digitalTwinStateProperty);
            else if(eventMessage.getType().equals(digitalTwinState.getPropertyUpdatedEventMessageType(digitalTwinStateProperty.getKey())))
                onStatePropertyUpdated(digitalTwinStateProperty);
            else if(eventMessage.getType().equals(digitalTwinState.getPropertyDeletedEventMessageType(digitalTwinStateProperty.getKey())))
                onStatePropertyDeleted(digitalTwinStateProperty);
            else
                logger.error(String.format("Digital Adapter (%s) -> observeDigitalTwinProperties: Error received type %s that is not matching", id, eventMessage.getType()));
        }
    }

    @Override
    public void onSync(IDigitalTwinState digitalTwinState) {
        logger.info("Digital Adapter ({}) Received DT onSync callback ! Ready to start ...", this.id);
        onDigitalTwinSync(digitalTwinState);

        try{
            if(observeDigitalTwinState)
                observeDigitalTwinState();
        }catch (Exception e){
            logger.error(String.format("Digital Adapter (%s) -> observe DigitalTwin State: Error: %s", id, e.getLocalizedMessage()));
        }
    }

    @Override
    public void onUnSync(IDigitalTwinState digitalTwinState) {
        logger.debug("Digital Adapter ({}) Received DT unSync callback ...", this.id);
        onDigitalTwinUnSync(digitalTwinState);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {

    }

    @Override
    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {

    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {

    }

    @Override
    public void onDigitalAdapterBound(String adapterId) {

    }

    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {

    }

    @Override
    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {

    }

    @Override
    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }
}
