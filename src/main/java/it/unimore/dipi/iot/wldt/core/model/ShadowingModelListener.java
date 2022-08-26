package it.unimore.dipi.iot.wldt.core.model;

import it.unimore.dipi.iot.wldt.core.state.IDigitalTwinState;

public interface ShadowingModelListener {

    public void onShadowingSync(IDigitalTwinState digitalTwinState);

    public void onShadowingOutOfSync(IDigitalTwinState digitalTwinState);

}
