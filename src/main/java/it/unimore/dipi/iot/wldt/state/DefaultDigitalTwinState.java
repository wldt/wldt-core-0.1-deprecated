package it.unimore.dipi.iot.wldt.state;

import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.event.EventMessage;
import it.unimore.dipi.iot.wldt.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultDigitalTwinState implements IDigitalTwinState {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDigitalTwinState.class);

    public static final String DT_STATE_PUBLISHER_ID = "dt-state-publisher";
    public static final String DT_STATE_SUBSCRIBER_ID = "dt-state-subscriber";

    private static final String DT_STATE_PROPERTY_BASE_TOPIC = "dt.state.property";

    public static final String DT_STATE_PROPERTY_CREATED = DT_STATE_PROPERTY_BASE_TOPIC + ".created";
    public static final String DT_STATE_PROPERTY_UPDATED = DT_STATE_PROPERTY_BASE_TOPIC + ".updated";
    public static final String DT_STATE_PROPERTY_DELETED = DT_STATE_PROPERTY_BASE_TOPIC + ".deleted";

    public static final String DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY = "dt.state.property.metadata.key";

    private Map<String, DigitalTwinStateProperty<?>> properties;

    public DefaultDigitalTwinState() {
        this.properties = new HashMap<>();
    }

    @Override
    public Optional<List<DigitalTwinStateProperty<?>>> getPropertyList() throws WldtDigitalTwinStatePropertyException {

        try {

            if (this.properties == null || this.properties.isEmpty())
                return Optional.empty();

            return Optional.of(new ArrayList<DigitalTwinStateProperty<?>>(this.properties.values()));

        } catch (Exception e) {
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    @Override
    public void createProperty(String propertyKey, DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyBadRequestException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DefaultDigitalTwinState: Properties Map = Null !");

        if (propertyKey == null || dtStateProperty == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DefaultDigitalTwinState: propertyKey(%s) and/or propertyValue(%s) = Null !", propertyKey, dtStateProperty));

        if (this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyConflictException(String.format("DefaultDigitalTwinState: property with Key: %s already existing ! Conflict !", propertyKey));

        try {
            this.properties.put(propertyKey, dtStateProperty);

            notifyPropertyCreated(propertyKey, dtStateProperty);
        } catch (Exception e) {
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<DigitalTwinStateProperty<?>> readProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DefaultDigitalTwinState: Properties Map = Null !");

        if (propertyKey == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException("DefaultDigitalTwinState: propertyKey = Null !");

        if (!this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DefaultDigitalTwinState: property with Key: %s not found !", propertyKey));

        //TODO Check the field exposed ? Is it really useful ?
        if (!this.properties.get(propertyKey).isReadable())
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DefaultDigitalTwinState: property with Key: %s not readable !", propertyKey));

        try{
            if (this.properties.get(propertyKey) != null)
                return Optional.of(this.properties.get(propertyKey));
            else
                return Optional.empty();
        }catch (Exception e){
            throw  new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }

    }

    @Override
    public void updateProperty(String propertyKey, DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DefaultDigitalTwinState: Properties Map = Null !");

        if (propertyKey == null || dtStateProperty == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DefaultDigitalTwinState: propertyKey(%s) and/or propertyValue(%s) = Null !", propertyKey, dtStateProperty));

        if (!this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DefaultDigitalTwinState: property with Key: %s not found !", propertyKey));

        if (!this.properties.get(propertyKey).isWritable())
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DefaultDigitalTwinState: property with Key: %s not writable !", propertyKey));

        //Check if there is a mismatch in the key between the propertyKey and statePropertyObject
        if (!propertyKey.equals(dtStateProperty.getKey()))
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DefaultDigitalTwinState: Mismatch between provided Key:{} and Property-Key: {} !", propertyKey, dtStateProperty.getKey()));

        try {
            this.properties.put(propertyKey, dtStateProperty);
            notifyPropertyUpdated(propertyKey, dtStateProperty);
        }catch (Exception e){
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    @Override
    public void deleteProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DefaultDigitalTwinState: Properties Map = Null !");

        if (propertyKey == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException("DefaultDigitalTwinState: propertyKey = Null !");

        if (!this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DefaultDigitalTwinState: property with Key: %s not found !", propertyKey));

        try{
            DigitalTwinStateProperty<?> originalValue = this.properties.get(propertyKey);
            this.properties.remove(propertyKey);

            notifyPropertyDeleted(propertyKey, originalValue);

        }catch (Exception e){
            throw  new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    private void notifyPropertyCreated(String propertyKey, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {

            EventMessage<DigitalTwinStateProperty<?>> eventMessage = new EventMessage<>(DT_STATE_PROPERTY_CREATED);
            eventMessage.setBody(digitalTwinStateProperty);
            eventMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, propertyKey);

            EventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventMessage);

        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyCreated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyPropertyUpdated(String propertyKey, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {

            //Publish the event for state observer
            EventMessage<DigitalTwinStateProperty<?>> eventStateMessage = new EventMessage<>(DT_STATE_PROPERTY_UPDATED);
            eventStateMessage.setBody(digitalTwinStateProperty);
            eventStateMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, propertyKey);

            EventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventStateMessage);

            //Publish the event for property observers
            EventMessage<DigitalTwinStateProperty<?>> eventPropertyMessage = new EventMessage<>(getPropertyUpdatedEventMessageType(propertyKey));
            eventPropertyMessage.setBody(digitalTwinStateProperty);
            eventPropertyMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, propertyKey);

            EventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventPropertyMessage);

        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyUpdated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyPropertyDeleted(String propertyKey, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {
            //Publish the event for state observer
            EventMessage<DigitalTwinStateProperty<?>> eventStateMessage = new EventMessage<>(DT_STATE_PROPERTY_DELETED);
            eventStateMessage.setBody(digitalTwinStateProperty);
            eventStateMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, propertyKey);

            EventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventStateMessage);

            //Publish the event for property observers
            EventMessage<DigitalTwinStateProperty<?>> eventPropertyMessage = new EventMessage<>(getPropertyDeletedEventMessageType(propertyKey));
            eventPropertyMessage.setBody(digitalTwinStateProperty);
            eventPropertyMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, propertyKey);

            EventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventPropertyMessage);
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyDeleted() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public String getPropertyUpdatedEventMessageType(String propertyKey){
        return String.format("%s.%s.updated", DT_STATE_PROPERTY_BASE_TOPIC, propertyKey);
    }

    @Override
    public String getPropertyDeletedEventMessageType(String propertyKey) {
        return String.format("%s.%s.deleted", DT_STATE_PROPERTY_BASE_TOPIC, propertyKey);
    }

}
