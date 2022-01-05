package it.unimore.dipi.iot.wldt.state;

import it.unimore.dipi.iot.wldt.exception.*;

import java.util.List;
import java.util.Optional;

public interface IDigitalTwinState {

    public Optional<List<DigitalTwinStateProperty<?>>> getPropertyList() throws WldtDigitalTwinStatePropertyException;

    public void createProperty(String propertyKey, DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyBadRequestException;

    public Optional<DigitalTwinStateProperty<?>> readProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    public void updateProperty(String propertyKey, DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    public void deleteProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    public void observeState(DigitalTwinStateListener digitalTwinStateListener) throws WldtDigitalTwinStateException, WldtDigitalTwinStateBadRequestException;

    public void unObserveState(DigitalTwinStateListener digitalTwinStateListener) throws WldtDigitalTwinStateException, WldtDigitalTwinStateBadRequestException;

    public void observeProperty(String propertyKey, DigitalTwinStatePropertyListener listener) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    public void unObserveProperty(String propertyKey, DigitalTwinStatePropertyListener listener) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;
}
