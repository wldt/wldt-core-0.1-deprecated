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
    private static final String DT_STATE_ACTION_BASE_TOPIC = "dt.state.action";

    public static final String CREATED_STRING  = "created";
    public static final String UPDATED_STRING  = "updated";
    public static final String DELETED_STRING  = "deleted";
    public static final String ENABLED_STRING  = "enabled";
    public static final String DISABLED_STRING = "disabled";


    public static final String DT_STATE_PROPERTY_CREATED = DT_STATE_PROPERTY_BASE_TOPIC + "." + CREATED_STRING;
    public static final String DT_STATE_PROPERTY_UPDATED = DT_STATE_PROPERTY_BASE_TOPIC + "." + UPDATED_STRING;
    public static final String DT_STATE_PROPERTY_DELETED = DT_STATE_PROPERTY_BASE_TOPIC + "." + DELETED_STRING;

    public static final String DT_STATE_ACTION_ENABLED = DT_STATE_ACTION_BASE_TOPIC + "." + ENABLED_STRING;
    public static final String DT_STATE_ACTION_UPDATED = DT_STATE_ACTION_BASE_TOPIC + "." + UPDATED_STRING;
    public static final String DT_STATE_ACTION_DISABLED = DT_STATE_ACTION_BASE_TOPIC + "." + DISABLED_STRING;

    public static final String DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY = "dt.state.property.metadata.key";
    public static final String DT_STATE_ACTION_METADATA_KEY_PROPERTY_KEY = "dt.state.action.metadata.key";

    private Map<String, DigitalTwinStateProperty<?>> properties;

    private Map<String, DigitalTwinStateAction> actions;

    public DefaultDigitalTwinState() {
        this.properties = new HashMap<>();
        this.actions = new HashMap<>();
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
    public String getPropertyCreatedEventMessageType(String propertyKey) {
        return String.format("%s.%s.%s", DT_STATE_PROPERTY_BASE_TOPIC, propertyKey, CREATED_STRING);
    }

    @Override
    public String getPropertyUpdatedEventMessageType(String propertyKey){
        return String.format("%s.%s.%s", DT_STATE_PROPERTY_BASE_TOPIC, propertyKey, UPDATED_STRING);
    }

    @Override
    public String getPropertyDeletedEventMessageType(String propertyKey) {
        return String.format("%s.%s.%s", DT_STATE_PROPERTY_BASE_TOPIC, propertyKey, DELETED_STRING);
    }

    @Override
    public void enableAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionConflictException {

        if (this.actions == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: Action Map = Null !");

        if (digitalTwinStateAction == null || digitalTwinStateAction.getKey() == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: digitalTwinStateAction or its Key = Null !");

        if (this.actions.containsKey(digitalTwinStateAction.getKey()))
            throw new WldtDigitalTwinStateActionConflictException(String.format("DefaultDigitalTwinState: action with Key: %s already existing ! Conflict !", digitalTwinStateAction.getKey()));

        try {
            this.actions.put(digitalTwinStateAction.getKey(), digitalTwinStateAction);
            notifyActionEnabled(digitalTwinStateAction);
        } catch (Exception e) {
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    @Override
    public void updateAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        if (this.actions == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: Action Map = Null !");

        if (digitalTwinStateAction == null || digitalTwinStateAction.getKey() == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: digitalTwinStateAction or its Key = Null !");

        if (!this.actions.containsKey(digitalTwinStateAction.getKey()))
            throw new WldtDigitalTwinStateActionNotFoundException(String.format("DefaultDigitalTwinState: Action with Key: %s not found !", digitalTwinStateAction.getKey()));

        try {
            this.actions.put(digitalTwinStateAction.getKey(), digitalTwinStateAction);
            notifyActionUpdated(digitalTwinStateAction);
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }

    }

    @Override
    public void disableAction(String actionKey) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        if (this.actions == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: Action Map = Null !");

        if (actionKey == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: digitalTwinStateAction Key = Null !");

        if (!this.actions.containsKey(actionKey))
            throw new WldtDigitalTwinStateActionNotFoundException(String.format("DefaultDigitalTwinState: Action with Key: %s not found !", actionKey));

        try {
            DigitalTwinStateAction originalValue = this.actions.get(actionKey);
            this.actions.remove(actionKey);
            notifyActionDisabled(originalValue);
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    private void notifyActionEnabled(DigitalTwinStateAction digitalTwinStateAction) {
        try {
            EventMessage<DigitalTwinStateAction> eventMessage = new EventMessage<>(DT_STATE_ACTION_ENABLED);
            eventMessage.setBody(digitalTwinStateAction);
            eventMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, digitalTwinStateAction.getKey());
            EventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventMessage);
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyCreated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyActionUpdated(DigitalTwinStateAction digitalTwinStateAction) {
        try {
            EventMessage<DigitalTwinStateAction> eventMessage = new EventMessage<>(DT_STATE_ACTION_UPDATED);
            eventMessage.setBody(digitalTwinStateAction);
            eventMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, digitalTwinStateAction.getKey());
            EventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventMessage);
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyCreated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyActionDisabled(DigitalTwinStateAction digitalTwinStateAction) {
        try {
            EventMessage<DigitalTwinStateAction> eventMessage = new EventMessage<>(DT_STATE_ACTION_DISABLED);
            eventMessage.setBody(digitalTwinStateAction);
            eventMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, digitalTwinStateAction.getKey());
            EventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventMessage);
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyCreated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

}
