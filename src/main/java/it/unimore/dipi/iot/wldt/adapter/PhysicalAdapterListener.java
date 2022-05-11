package it.unimore.dipi.iot.wldt.adapter;

import java.util.Optional;

public interface PhysicalAdapterListener {

    public void onBound(String adapterId, PhysicalAssetState physicalAssetState);

    public void onBindingUpdate(String adapterId, PhysicalAssetState physicalAssetState);

    public void onUnBound(String adapterId,  PhysicalAssetState physicalAssetState, Optional<String> errorMessage);
}
