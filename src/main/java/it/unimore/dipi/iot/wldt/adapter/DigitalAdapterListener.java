package it.unimore.dipi.iot.wldt.adapter;

public interface DigitalAdapterListener {

    public void onDigitalAdapterBound(String adapterId);

    public void onDigitalAdapterUnBound(String adapterId, String errorMessage);
}
