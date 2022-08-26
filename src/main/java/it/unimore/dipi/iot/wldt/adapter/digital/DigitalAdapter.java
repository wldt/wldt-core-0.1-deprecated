package it.unimore.dipi.iot.wldt.adapter.digital;

import it.unimore.dipi.iot.wldt.core.state.DigitalTwinStateEventNotification;
import it.unimore.dipi.iot.wldt.adapter.physical.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.core.engine.LifeCycleListener;
import it.unimore.dipi.iot.wldt.core.event.WldtEventBus;
import it.unimore.dipi.iot.wldt.core.event.WldtEventFilter;
import it.unimore.dipi.iot.wldt.core.event.WldtEventListener;
import it.unimore.dipi.iot.wldt.core.event.WldtEvent;
import it.unimore.dipi.iot.wldt.core.state.*;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.PhysicalAdapterException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import it.unimore.dipi.iot.wldt.core.worker.WldtWorker;
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
public abstract class DigitalAdapter<C> extends WldtWorker implements WldtEventListener, LifeCycleListener {

    private static final Logger logger = LoggerFactory.getLogger(DigitalAdapter.class);

    private String id = null;

    private C configuration;

    private WldtEventFilter statePropertiesWldtEventFilter = null;

    private WldtEventFilter stateActionsWldtEventFilter = null;

    private WldtEventFilter stateEventsAvailabilityWldtEventFilter = null;

    private WldtEventFilter stateTargetPropertyWldtEventsFilter = null;

    private WldtEventFilter stateTargetEventNotificationWldtEventsFilter = null;

    private boolean observeDigitalTwinState = true;

    protected IDigitalTwinState digitalTwinState = null;

    private DigitalAdapterListener digitalAdapterListener;

    private final WldtEventListener digitalTwinStateWldtEventListener = new WldtEventListener() {
        @Override
        public void onEventSubscribed(String eventType) {
            //TODO Implement
        }

        @Override
        public void onEventUnSubscribed(String eventType) {
            //TODO Implement
        }

        @Override
        public void onEvent(WldtEvent<?> wldtEvent) {

            logger.debug("{} - Digital Adapter - Received Event: {}", getId(), wldtEvent);

            ///////// DT STATE PROPERTY MANAGEMENT ///////////
            if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateProperty)){
                DigitalTwinStateProperty<?> digitalTwinStateProperty = (DigitalTwinStateProperty<?>) wldtEvent.getBody();

                if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED))
                    onStateChangePropertyCreated(digitalTwinStateProperty);

                if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED))
                    onStateChangePropertyUpdated(digitalTwinStateProperty);

                if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED))
                    onStateChangePropertyDeleted(digitalTwinStateProperty);
            }

            ///////// DT STATE ACTIONS MANAGEMENT ///////////
            if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateAction)) {

                DigitalTwinStateAction digitalTwinStateAction = (DigitalTwinStateAction) wldtEvent.getBody();

                if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_ACTION_ENABLED))
                    onStateChangeActionEnabled(digitalTwinStateAction);

                if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_ACTION_UPDATED))
                    onStateChangeActionUpdated(digitalTwinStateAction);

                if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_ACTION_DISABLED))
                    onStateChangeActionDisabled(digitalTwinStateAction);
            }

            ///////// DT STATE EVENTS AVAILABILITY MANAGEMENT ///////////
            if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateEvent)) {

                DigitalTwinStateEvent digitalTwinStateEvent = (DigitalTwinStateEvent) wldtEvent.getBody();

                if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_EVENT_REGISTERED))
                    onStateChangeEventRegistered(digitalTwinStateEvent);

                if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_EVENT_REGISTRATION_UPDATED))
                    onStateChangeEventRegistrationUpdated(digitalTwinStateEvent);

                if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_EVENT_UNREGISTERED))
                    onStateChangeEventUnregistered(digitalTwinStateEvent);
            }

            ///////// DT STATE EVENTS NOTIFICATION MANAGEMENT ///////////
            if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateEventNotification)) {
                DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification = (DigitalTwinStateEventNotification<?>) wldtEvent.getBody();
                onDigitalTwinStateEventNotificationReceived(digitalTwinStateEventNotification);
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

    //////////////////////// PROPERTIES OBSERVATION ///////////////////////////////////////////////////////////////////

    /**
     * Enable the observation of all the Digital Twin State properties, when they are created, updated and deleted.
     * With respect to properties an update contains the new value and no additional observations are required
     * @throws EventBusException
     */
    protected void observeDigitalTwinStateProperties() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);

        //Save the adopted EventFilter
        this.statePropertiesWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Cancel the observation of all the Digital Twin State properties, when they are created, updated and deleted.
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinStateProperties() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);

        //Save the adopted EventFilter
        this.statePropertiesWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Enable the observation of a specific list of Digital Twin State properties, when they are updated and/or deleted.
     * With respect to properties an update contains the new value and no additional observations are required
     * @param propertyList
     * @throws EventBusException
     */
    protected void observeTargetDigitalTwinProperties(List<String> propertyList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String propertyKey : propertyList) {
            wldtEventFilter.add(digitalTwinState.getPropertyUpdatedWldtEventMessageType(propertyKey));
            wldtEventFilter.add(digitalTwinState.getPropertyDeletedWldtEventMessageType(propertyKey));
        }

        //Save the adopted EventFilter
        if(stateTargetPropertyWldtEventsFilter == null)
            this.stateTargetPropertyWldtEventsFilter = new WldtEventFilter();

        this.stateTargetPropertyWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, this);

    }

    /**
     * Cancel the observation of a target list of properties
     * @throws EventBusException
     */
    protected void unObserveTargetDigitalTwinProperties(List<String> propertyList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String propertyKey : propertyList) {
            wldtEventFilter.add(digitalTwinState.getPropertyUpdatedWldtEventMessageType(propertyKey));
            wldtEventFilter.add(digitalTwinState.getPropertyDeletedWldtEventMessageType(propertyKey));
        }

        if(stateTargetPropertyWldtEventsFilter == null)
            this.stateTargetPropertyWldtEventsFilter = new WldtEventFilter();

        this.stateTargetPropertyWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    /**
     * Enable the observation of a single Digital Twin State properties, when it is updated and/or deleted.
     * With respect to properties an update contains the new value and no additional observations are required
     * @param propertyKey
     * @throws EventBusException
     */
    protected void observeDigitalTwinProperty(String propertyKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(digitalTwinState.getPropertyUpdatedWldtEventMessageType(propertyKey));
        wldtEventFilter.add(digitalTwinState.getPropertyDeletedWldtEventMessageType(propertyKey));

        if(stateTargetPropertyWldtEventsFilter == null)
            this.stateTargetPropertyWldtEventsFilter = new WldtEventFilter();

        this.stateTargetPropertyWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of a single target property
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinProperty(String propertyKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(digitalTwinState.getPropertyUpdatedWldtEventMessageType(propertyKey));
        wldtEventFilter.add(digitalTwinState.getPropertyDeletedWldtEventMessageType(propertyKey));

        if(stateTargetPropertyWldtEventsFilter == null)
            this.stateTargetPropertyWldtEventsFilter = new WldtEventFilter();

        this.stateTargetPropertyWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////// ACTIONS OBSERVATION /////////////////////////////////////////////////////////

    /**
     * Enable the observation of available Digital Twin State Actions.
     * Callbacks will be received when an action is enabled, updated or disable.
     * The update of an action is associated to the variation of its signature and declaration and it is not associated
     * to any attached payload or value.
     *
     * @throws EventBusException
     */
    protected void observeDigitalTwinStateActionsAvailability() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_ACTION_ENABLED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_ACTION_UPDATED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_ACTION_DISABLED);

        //Save the adopted EventFilter
        this.stateActionsWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Cancel the observation of Digital Twin State Actions
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinStateActionsAvailability() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_ACTION_ENABLED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_ACTION_UPDATED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_ACTION_DISABLED);

        //Save the adopted EventFilter
        this.stateActionsWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////// EVENTS OBSERVATION //////////////////////////////////////////////////////////

    /**
     * Enable the observation of available Digital Twin State Events.
     * Callbacks will be received when an event is registered, updated or unregistered.
     * The update of an event is associated to the variation of its signature and declaration and it is not associated
     * to any attached payload or value.
     *
     * @throws EventBusException
     */
    protected void observeDigitalTwinStateEventsAvailability() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_EVENT_REGISTERED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_EVENT_REGISTRATION_UPDATED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_EVENT_UNREGISTERED);

        //Save the adopted EventFilter
        this.stateEventsAvailabilityWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Cancel the observation of Digital Twin State Events
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinStateEventsAvailability() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_EVENT_REGISTERED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_EVENT_REGISTRATION_UPDATED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_EVENT_UNREGISTERED);

        //Save the adopted EventFilter
        this.stateEventsAvailabilityWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Enable the observation of the notification associated to a specific list of Digital Twin State events.
     * With respect to event a notification contains the new associated value
     * @param eventsList
     * @throws EventBusException
     */
    protected void observeDigitalTwinEventsNotifications(List<String> eventsList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String eventKey : eventsList)
            wldtEventFilter.add(digitalTwinState.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter = new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Cancel the observation of a target list of properties
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinEventsNotifications(List<String> eventsList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String eventKey : eventsList)
            wldtEventFilter.add(digitalTwinState.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    /**
     * Enable the observation of the notification associated to a single Digital Twin State event.
     * With respect to event a notification contains the new associated value
     * @param eventKey
     * @throws EventBusException
     */
    protected void observeDigitalTwinEventNotification(String eventKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(digitalTwinState.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of a single target event
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinEventNotification(String eventKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(digitalTwinState.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    //////////////////////// PROPERTIES VARIATIONS CALLBACKS /////////////////////////////////////////////////////

    abstract protected void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStatePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStatePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    //////////////////////// ACTIONS VARIATIONS CALLBACKS /////////////////////////////////////////////////////

    abstract protected void onStateChangeActionEnabled(DigitalTwinStateAction digitalTwinStateAction);

    abstract protected void onStateChangeActionUpdated(DigitalTwinStateAction digitalTwinStateAction);

    abstract protected void onStateChangeActionDisabled(DigitalTwinStateAction digitalTwinStateAction);

    //////////////////////// EVENTS VARIATIONS CALLBACKS /////////////////////////////////////////////////////

    abstract protected void onStateChangeEventRegistered(DigitalTwinStateEvent digitalTwinStateEvent);

    abstract protected void onStateChangeEventRegistrationUpdated(DigitalTwinStateEvent digitalTwinStateEvent);

    abstract protected void onStateChangeEventUnregistered(DigitalTwinStateEvent digitalTwinStateEvent);

    //////////////////////// EVENTS NOTIFICATION CALLBACK /////////////////////////////////////////////////////

    abstract protected void onDigitalTwinStateEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification);

    //////////////////////// ADAPTER CALLBACKS /////////////////////////////////////////////////////

    public abstract void onAdapterStart();

    public abstract void onAdapterStop();

    //////////////////////// DT SYNC CALLBACKS /////////////////////////////////////////////////////

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
            unObserveDigitalTwinStateProperties();
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
    public void onEvent(WldtEvent<?> wldtEvent) {
        if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateProperty)){
            DigitalTwinStateProperty<?> digitalTwinStateProperty = (DigitalTwinStateProperty<?>) wldtEvent.getBody();
            if(wldtEvent.getType().equals(digitalTwinState.getPropertyCreatedWldtEventMessageType(digitalTwinStateProperty.getKey())))
                onStateChangePropertyCreated(digitalTwinStateProperty);
            else if(wldtEvent.getType().equals(digitalTwinState.getPropertyUpdatedWldtEventMessageType(digitalTwinStateProperty.getKey())))
                onStatePropertyUpdated(digitalTwinStateProperty);
            else if(wldtEvent.getType().equals(digitalTwinState.getPropertyDeletedWldtEventMessageType(digitalTwinStateProperty.getKey())))
                onStatePropertyDeleted(digitalTwinStateProperty);
            else
                logger.error(String.format("Digital Adapter (%s) -> observeDigitalTwinProperties: Error received type %s that is not matching", id, wldtEvent.getType()));
        }
    }

    @Override
    public void onSync(IDigitalTwinState digitalTwinState) {

        logger.info("Digital Adapter ({}) Received DT onSync callback ! Ready to start ...", this.id);

        this.digitalTwinState = digitalTwinState;

        onDigitalTwinSync(digitalTwinState);

        try{
            if(observeDigitalTwinState) {
                observeDigitalTwinStateProperties();
                observeDigitalTwinStateActionsAvailability();
                observeDigitalTwinStateEventsAvailability();
            }
        }catch (Exception e){
            logger.error(String.format("Digital Adapter (%s) -> observe DigitalTwin State: Error: %s", id, e.getLocalizedMessage()));
        }
    }

    @Override
    public void onUnSync(IDigitalTwinState digitalTwinState) {
        logger.debug("Digital Adapter ({}) Received DT unSync callback ...", this.id);
        onDigitalTwinUnSync(digitalTwinState);
        this.digitalTwinState = null;
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
