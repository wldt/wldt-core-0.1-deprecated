package it.unimore.dipi.iot.wldt.model;

import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.event.EventFilter;
import it.unimore.dipi.iot.wldt.event.EventListener;
import it.unimore.dipi.iot.wldt.event.EventMessage;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.state.DefaultDigitalTwinState;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
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
public abstract class ModelFunction {

    private static final Logger logger = LoggerFactory.getLogger(ModelFunction.class);

    private String id = null;

    private EventFilter statePropertyEventsFilter = null;
    private EventFilter physicalEventsFilter = null;
    private EventFilter digitalEventsFilter = null;

    protected IDigitalTwinState digitalTwinState = null;

    private ModelFunction(){}

    public ModelFunction(String id){
        this.id = id;
    }

    public void observeDigitalTwinState() throws EventBusException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);

        EventBus.getInstance().subscribe(this.id, eventFilter, new EventListener() {
            @Override
            public void onSubscribe(String eventType) {
                //TODO Implement
            }

            @Override
            public void onUnSubscribe(String eventType) {
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

    public void observeDigitalTwinProperties(List<String> propertyList) {
        this.statePropertyEventsFilter = statePropertyEventsFilter;
    }

    public void registerPhysicalEventFilter(EventFilter physicalEventsFilter) {
        this.physicalEventsFilter = physicalEventsFilter;
    }

    public void registerDigitalEventFilter(EventFilter digitalEventsFilter) {
        this.digitalEventsFilter = digitalEventsFilter;
    }

    abstract public void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract public void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract public void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract public void onPhysicalEvent();

    abstract public void onDigitalEvent();

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
        ModelFunction that = (ModelFunction) o;
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
        sb.append(", physicalEventsFilter=").append(physicalEventsFilter);
        sb.append(", digitalEventsFilter=").append(digitalEventsFilter);
        sb.append('}');
        return sb.toString();
    }
}
