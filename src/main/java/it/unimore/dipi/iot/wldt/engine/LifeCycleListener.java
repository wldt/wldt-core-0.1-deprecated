package it.unimore.dipi.iot.wldt.engine;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;

import java.util.Map;

public interface LifeCycleListener {

    public void onCreate();

    public void onStart();

    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription);

    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription);

    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage);

    public void onDigitalAdapterBound(String adapterId);

    public void onDigitalAdapterUnBound(String adapterId, String errorMessage);

    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap);

    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage);

    public void onSync(IDigitalTwinState digitalTwinState);

    public void onUnSync(IDigitalTwinState digitalTwinState);

    public void onStop();

    public void onDestroy();

}
