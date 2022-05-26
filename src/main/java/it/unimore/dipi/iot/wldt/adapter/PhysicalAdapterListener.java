package it.unimore.dipi.iot.wldt.adapter;

public interface PhysicalAdapterListener {

    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription);

    public void onPhysicalBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription);

    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage);
}
