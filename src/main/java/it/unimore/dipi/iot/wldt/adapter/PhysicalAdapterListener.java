package it.unimore.dipi.iot.wldt.adapter;

import java.util.Optional;

public interface PhysicalAdapterListener {

    public void onBound(String adapterId);

    public void onUnBound(String adapterId, Optional<String> errorMessage);
}
