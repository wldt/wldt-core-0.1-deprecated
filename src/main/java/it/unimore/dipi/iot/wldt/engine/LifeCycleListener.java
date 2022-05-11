package it.unimore.dipi.iot.wldt.engine;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetState;

import java.util.Map;
import java.util.Optional;

public interface LifeCycleListener {

    public void onCreate();

    public void onStart();

    public void onAdapterBound(String adapterId, PhysicalAssetState physicalAssetState);

    public void onAdapterUnBound(String adapterId, PhysicalAssetState physicalAssetState, Optional<String> errorMessage);

    public void onBound(Map<String, PhysicalAssetState> adaptersPhysicalStateMap);

    public void onUnBound(Map<String, PhysicalAssetState> adaptersPhysicalStateMap, Optional<String> errorMessage);

    public void onSync();

    public void onUnSync();

    public void onStop();

    public void onDestroy();

}
