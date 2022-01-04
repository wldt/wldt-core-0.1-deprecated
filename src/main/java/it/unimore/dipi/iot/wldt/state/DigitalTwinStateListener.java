package it.unimore.dipi.iot.wldt.state;

import java.util.Optional;

public interface DigitalTwinStateListener {

    public void onPropertyCreated(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty);

    public void onPropertyUpdated(String propertyKey, Optional<DigitalTwinStateProperty<?>> previousDtStateProperty, Optional<DigitalTwinStateProperty<?>> currentDtStateProperty);

    public void onPropertyDeleted(String propertyKey, Optional<DigitalTwinStateProperty<?>> dtStateProperty);

}

