package it.unimore.dipi.iot.wldt.state;

import it.unimore.dipi.iot.wldt.exception.WldtDigitalTwinStateException;

import java.util.List;
import java.util.Optional;

public interface IDigitalTwinState {

    public Optional<List<DigitalTwinStateProperty<?>>> getPropertyList() throws WldtDigitalTwinStateException;

    public void createProperty(String propertyKey, DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStateException;

    public Optional<DigitalTwinStateProperty<?>> readProperty(String propertyKey) throws WldtDigitalTwinStateException;

    public void updateProperty(String propertyKey, DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStateException;

    public void deleteProperty(String propertyKey) throws WldtDigitalTwinStateException;

    public void observeState(DigitalTwinStateListener digitalTwinStateListener) throws WldtDigitalTwinStateException;

    public void unObserveState(DigitalTwinStateListener digitalTwinStateListener) throws WldtDigitalTwinStateException;

    public void observeProperty(String propertyKey, DigitalTwinStatePropertyListener listener) throws WldtDigitalTwinStateException;

    public void unObserveProperty(String propertyKey, DigitalTwinStatePropertyListener listener) throws WldtDigitalTwinStateException;
}
