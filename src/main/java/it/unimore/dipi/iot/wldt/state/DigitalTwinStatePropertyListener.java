package it.unimore.dipi.iot.wldt.state;

import java.util.Optional;

public interface DigitalTwinStatePropertyListener {

    public void onChange(String propertyKey, Optional<DigitalTwinStateProperty<?>> previousDtStateProperty, Optional<DigitalTwinStateProperty<?>> dtStateProperty);

}
