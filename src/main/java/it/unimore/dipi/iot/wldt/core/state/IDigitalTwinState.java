package it.unimore.dipi.iot.wldt.core.state;

import it.unimore.dipi.iot.wldt.exception.*;

import java.util.List;
import java.util.Optional;

public interface IDigitalTwinState {

    /////////////// PROPERTY MANAGEMENT ////////////////////////////

    public boolean containsProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException;

    public Optional<DigitalTwinStateProperty<?>> getProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException;

    public Optional<List<DigitalTwinStateProperty<?>>> getPropertyList() throws WldtDigitalTwinStatePropertyException;

    public void createProperty(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyBadRequestException;

    public Optional<DigitalTwinStateProperty<?>> readProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    public void updateProperty(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    public void deleteProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    public String getPropertyCreatedWldtEventMessageType(String propertyKey);

    public String getPropertyUpdatedWldtEventMessageType(String propertyKey);

    public String getPropertyDeletedWldtEventMessageType(String propertyKey);

    //////////////////////////////////////////////////////////////

    /////////////// ACTION MANAGEMENT ////////////////////////////

    public void enableAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionConflictException;

    public void updateAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException;

    public void disableAction(String actionKey) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException;

    //////////////////////////////////////////////////////////////

    /////////////// EVENT MANAGEMENT ////////////////////////////

    /**
     * Check if a Digital Twin State Event with the specified key is correctly registered
     * @param eventKey
     * @return
     * @throws WldtDigitalTwinStateEventException
     */
    public boolean containsEvent(String eventKey) throws WldtDigitalTwinStateEventException;

    /**
     * Return the description of a registered Digital Twin State Event according to its Key
     * @param eventKey
     * @return
     * @throws WldtDigitalTwinStateEventException
     */
    public Optional<DigitalTwinStateEvent> getEvent(String eventKey) throws WldtDigitalTwinStateEventException;

    /**
     * Return the list of existing and registered Digital Twin State Events
     * @return
     * @throws WldtDigitalTwinStateEventException
     */
    public Optional<List<DigitalTwinStateEvent>> getEventList() throws WldtDigitalTwinStateEventException;

    /**
     * Register a new Digital Twin State Event
     * @param digitalTwinStateEvent
     * @throws WldtDigitalTwinStateEventException
     * @throws WldtDigitalTwinStateEventConflictException
     */
    public void registerEvent(DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateEventException, WldtDigitalTwinStateEventConflictException;

    /**
     * Update the registration and signature of an existing Digital Twin State Event
     * @param digitalTwinStateEvent
     * @throws WldtDigitalTwinStateEventException
     */
    public void updateRegisteredEvent(DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateEventException;

    /**
     * Un-register a Digital Twin State Event
     * @param eventKey
     * @throws WldtDigitalTwinStateEventException
     */
    public void unRegisterEvent(String eventKey) throws WldtDigitalTwinStateEventException;

    /**
     * Return the Event Type associated to Digital Twin State Event Notifications
     * @param eventKey
     * @return
     */
    public String getEventNotificationWldtEventMessageType(String eventKey);

    /**
     * Method to notify the occurrence of the target Digital Twin State Event
     * @param digitalTwinStateEventNotification
     */
    public void notifyDigitalTwinStateEvent(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws WldtDigitalTwinStateEventNotificationException;

    //////////////////////////////////////////////////////////////

}
