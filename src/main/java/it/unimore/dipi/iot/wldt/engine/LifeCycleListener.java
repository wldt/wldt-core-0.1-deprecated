package it.unimore.dipi.iot.wldt.engine;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAdapter;

import java.util.Optional;

public interface LifeCycleListener {

    public void onCreate();

    public void onStart();

    public void onAdapterBound(String adapterId);

    public void onAdapterUnBound(String adapterId, Optional<String> errorMessage);

    public void onBound();

    public void onUnBound(Optional<String> errorMessage);

    public void onSync();

    public void onUnSync();

    public void onStop();

    public void onDestroy();

}
