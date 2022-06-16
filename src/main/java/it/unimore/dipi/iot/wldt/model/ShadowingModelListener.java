package it.unimore.dipi.iot.wldt.model;

import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;

public interface ShadowingModelListener {

    public void onShadowingSync(IDigitalTwinState digitalTwinState);

    public void onShadowingOutOfSync(IDigitalTwinState digitalTwinState);

}
