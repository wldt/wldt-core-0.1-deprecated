package it.unimore.dipi.iot.wldt.state;

import it.unimore.dipi.iot.wldt.exception.*;

import java.util.List;
import java.util.Optional;

public interface IDigitalTwinState {

    public boolean containsProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException;

    public Optional<DigitalTwinStateProperty<?>> getProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException;

    public Optional<List<DigitalTwinStateProperty<?>>> getPropertyList() throws WldtDigitalTwinStatePropertyException;

    public void createProperty(String propertyKey, DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyBadRequestException;

    public Optional<DigitalTwinStateProperty<?>> readProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    public void updateProperty(String propertyKey, DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    public void deleteProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    public String getPropertyCreatedEventMessageType(String propertyKey);

    public String getPropertyUpdatedEventMessageType(String propertyKey);

    public String getPropertyDeletedEventMessageType(String propertyKey);

    public void enableAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionConflictException;

    public void updateAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException;

    public void disableAction(String actionKey) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException;

}
