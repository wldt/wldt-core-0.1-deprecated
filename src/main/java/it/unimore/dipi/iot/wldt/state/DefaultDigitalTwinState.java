package it.unimore.dipi.iot.wldt.state;

import it.unimore.dipi.iot.wldt.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultDigitalTwinState implements IDigitalTwinState {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDigitalTwinState.class);

    private Map<String, DigitalTwinStateProperty<?>> properties;
    private List<DigitalTwinStateListener> listenerList;
    private Map<String, List<DigitalTwinStatePropertyListener>> propertyListenerMap = null;

    public DefaultDigitalTwinState() {
        this.properties = new HashMap<>();
        this.listenerList = new ArrayList<>();
        this.propertyListenerMap = new HashMap<>();
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

            notifyStateListenersPropertyCreated(propertyKey, dtStateProperty);
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

            DigitalTwinStateProperty<?> originalValue = this.properties.get(propertyKey);
            this.properties.put(propertyKey, dtStateProperty);

            notifyStateListenersPropertyUpdated(propertyKey, originalValue, dtStateProperty);
            notifyPropertyUpdatedListeners(propertyKey, originalValue, dtStateProperty);

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

            notifyStateListenersPropertyDeleted(propertyKey, originalValue);

            notifyPropertyDeletedListeners(propertyKey, originalValue);

            //Remove all listener for the target property
            this.propertyListenerMap.remove(propertyKey);
        }catch (Exception e){
            throw  new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    @Override
    public void observeState(DigitalTwinStateListener digitalTwinStateListener) throws WldtDigitalTwinStateException, WldtDigitalTwinStateBadRequestException {

        if (this.listenerList == null)
            throw new WldtDigitalTwinStateException("DefaultDigitalTwinState: Listener List = Null !");

        if (digitalTwinStateListener == null)
            throw new WldtDigitalTwinStateBadRequestException("Null Listener provided to observe DigitalTwinState !");

        try{
            this.listenerList.add(digitalTwinStateListener);
        }catch (Exception e){
            throw new WldtDigitalTwinStateException(e.getLocalizedMessage());
        }
    }

    @Override
    public void unObserveState(DigitalTwinStateListener digitalTwinStateListener) throws WldtDigitalTwinStateException, WldtDigitalTwinStateBadRequestException {

        if (this.listenerList == null)
            throw new WldtDigitalTwinStateException("DefaultDigitalTwinState: Listener List = Null !");

        if (digitalTwinStateListener == null)
            throw new WldtDigitalTwinStateBadRequestException("Null Listener provided to observe DigitalTwinState !");

        try{
            this.listenerList.remove(digitalTwinStateListener);
        }catch (Exception e){
            throw new WldtDigitalTwinStateException(e.getLocalizedMessage());
        }
    }

    @Override
    public void observeProperty(String propertyKey, DigitalTwinStatePropertyListener listener) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (listener == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("Null Listener provided to observe property: %s!", propertyKey));

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DefaultDigitalTwinState: Properties Map = Null !");

        if (propertyKey == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException("DefaultDigitalTwinState: propertyKey = Null !");

        if (!this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DefaultDigitalTwinState: property with Key: %s not found !", propertyKey));

        try{
            //If required init the list of listener for the target property
            if (!this.propertyListenerMap.containsKey(propertyKey))
                this.propertyListenerMap.put(propertyKey, new ArrayList<>());

            this.propertyListenerMap.get(propertyKey).add(listener);
        }catch (Exception e){
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    @Override
    public void unObserveProperty(String propertyKey, DigitalTwinStatePropertyListener listener) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DefaultDigitalTwinState: Properties Map = Null !");

        if (listener == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("Null Listener provided to observe property: %s!", propertyKey));

        if (propertyKey == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException("DefaultDigitalTwinState: propertyKey = Null !");

        if (!this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DefaultDigitalTwinState: property with Key: %s not found !", propertyKey));

        if (!this.propertyListenerMap.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DefaultDigitalTwinState: property with Key: %s not found in Listener Map !", propertyKey));

        try{
            this.propertyListenerMap.get(propertyKey).remove(listener);
        }catch (Exception e){
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    private void notifyStateListenersPropertyCreated(String propertyKey, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {
            if (this.listenerList != null)
                for (DigitalTwinStateListener listener : this.listenerList)
                    listener.onPropertyCreated(propertyKey, Optional.ofNullable(digitalTwinStateProperty));
            else
                logger.warn("notifyStateListenersPropertyCreated() -> DT State Listener List Empty ! No one is observing ...");
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyCreated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyStateListenersPropertyUpdated(String propertyKey, DigitalTwinStateProperty<?> previousDigitalTwinStateProperty, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {
            if (this.listenerList != null)
                for (DigitalTwinStateListener listener : this.listenerList)
                    listener.onPropertyUpdated(propertyKey, Optional.ofNullable(previousDigitalTwinStateProperty), Optional.ofNullable(digitalTwinStateProperty));
            else
                logger.warn("notifyStateListenersPropertyUpdated() -> DT State Listener List Empty ! No one is observing ...");
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyUpdated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyPropertyUpdatedListeners(String propertyKey, DigitalTwinStateProperty<?> previousDigitalTwinStateProperty, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {
            if (this.propertyListenerMap != null && this.propertyListenerMap.containsKey(propertyKey) && this.propertyListenerMap.get(propertyKey) != null)
                for (DigitalTwinStatePropertyListener listener : this.propertyListenerMap.get(propertyKey))
                    listener.onChange(propertyKey, Optional.ofNullable(previousDigitalTwinStateProperty), Optional.ofNullable(digitalTwinStateProperty));
            else
                logger.warn("notifyPropertyUpdateListeners() -> DT Property Listener List Empty for propertyKey: {} ! No one is observing ...", propertyKey);
        } catch (Exception e) {
            logger.error("notifyPropertyUpdateListeners() -> Error Notifying State Listeners for propertyKey: {} ! Error: {}", propertyKey, e.getLocalizedMessage());
        }
    }

    private void notifyStateListenersPropertyDeleted(String propertyKey, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {
            if (this.listenerList != null)
                for (DigitalTwinStateListener listener : this.listenerList)
                    listener.onPropertyDeleted(propertyKey, Optional.ofNullable(digitalTwinStateProperty));
            else
                logger.warn("notifyStateListenersPropertyDeleted() -> DT State Listener List Empty ! No one is observing ...");
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyDeleted() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyPropertyDeletedListeners(String propertyKey, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {
            if (this.propertyListenerMap != null && this.propertyListenerMap.containsKey(propertyKey) && this.propertyListenerMap.get(propertyKey) != null)
                for (DigitalTwinStatePropertyListener listener : this.propertyListenerMap.get(propertyKey))
                    listener.onDelete(propertyKey, Optional.ofNullable(digitalTwinStateProperty));
            else
                logger.warn("notifyPropertyUpdateListeners() -> DT Property Listener List Empty for propertyKey: {} ! No one is observing ...", propertyKey);
        } catch (Exception e) {
            logger.error("notifyPropertyUpdateListeners() -> Error Notifying State Listeners for propertyKey: {} ! Error: {}", propertyKey, e.getLocalizedMessage());
        }
    }

}
