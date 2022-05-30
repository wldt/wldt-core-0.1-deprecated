package it.unimore.dipi.iot.wldt.adapter;

import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.event.EventFilter;
import it.unimore.dipi.iot.wldt.event.EventListener;
import it.unimore.dipi.iot.wldt.event.EventMessage;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import it.unimore.dipi.iot.wldt.state.DefaultDigitalTwinState;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public abstract class DigitalAdapter<C> extends WldtWorker implements EventListener {

    private static final Logger logger = LoggerFactory.getLogger(DigitalAdapter.class);

    private String id = null;

    private C configuration;

    private EventFilter stateEventFilter = null;

    private EventFilter statePropertyEventsFilter = null;

    private boolean observeDigitalTwinState = false;

    protected IDigitalTwinState digitalTwinState = null;

    private DigitalAdapter(){}

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

        EventBus.getInstance().subscribe(this.id, eventFilter, new EventListener() {
            @Override
            public void onEventSubscribed(String eventType) {
                //TODO Implement
            }

            @Override
            public void onEventUnSubscribed(String eventType) {
                //TODO Implement
            }

            @Override
            public void onEvent(Optional<EventMessage<?>> eventMessage) {

                if(eventMessage.isPresent() && eventMessage.get().getBody() != null && (eventMessage.get().getBody() instanceof DigitalTwinStateProperty)){
                    DigitalTwinStateProperty digitalTwinStateProperty = (DigitalTwinStateProperty) eventMessage.get().getBody();

                    if(eventMessage.get().getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED))
                        onStateChangePropertyCreated(digitalTwinStateProperty);

                    if(eventMessage.get().getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED))
                        onStateChangePropertyUpdated(digitalTwinStateProperty);

                    if(eventMessage.get().getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED))
                        onStateChangePropertyDeleted(digitalTwinStateProperty);
                }
            }
        });
    }

    protected void observeDigitalTwinProperties(List<String> propertyList) throws EventBusException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();

        for(String propertyKey : propertyList) {
            eventFilter.add(digitalTwinState.getPropertyUpdatedEventMessageType(propertyKey));
            eventFilter.add(digitalTwinState.getPropertyDeletedEventMessageType(propertyKey));
        }

        //Save the adopted EventFilter
        this.statePropertyEventsFilter = eventFilter;

        EventBus.getInstance().subscribe(this.id, eventFilter, this);

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
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModelFunction{");
        sb.append("id='").append(id).append('\'');
        sb.append(", statePropertyEventsFilter=").append(statePropertyEventsFilter);
        sb.append('}');
        return sb.toString();
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
    public void onEvent(Optional<EventMessage<?>> eventMessage) {
        if(eventMessage.isPresent() && eventMessage.get().getBody() != null && (eventMessage.get().getBody() instanceof DigitalTwinStateProperty)){
            DigitalTwinStateProperty digitalTwinStateProperty = (DigitalTwinStateProperty) eventMessage.get().getBody();
            if(eventMessage.get().getType().equals(digitalTwinState.getPropertyCreatedEventMessageType(digitalTwinStateProperty.getKey())))
                onStateChangePropertyCreated(digitalTwinStateProperty);
            else if(eventMessage.get().getType().equals(digitalTwinState.getPropertyUpdatedEventMessageType(digitalTwinStateProperty.getKey())))
                onStatePropertyUpdated(digitalTwinStateProperty);
            else if(eventMessage.get().getType().equals(digitalTwinState.getPropertyDeletedEventMessageType(digitalTwinStateProperty.getKey())))
                onStatePropertyDeleted(digitalTwinStateProperty);
            else
                logger.error(String.format("ModelFunction(%s) -> observeDigitalTwinProperties: Error received type %s that is not matching", id, eventMessage.get().getType()));
        }
    }
}
