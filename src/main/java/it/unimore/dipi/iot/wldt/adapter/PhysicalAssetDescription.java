package it.unimore.dipi.iot.wldt.adapter;

import java.util.List;

public class PhysicalAssetDescription {

    private List<PhysicalAssetAction> actions;

    private List<PhysicalAssetProperty<?>> properties;

    private List<PhysicalAssetEvent> events;

    public PhysicalAssetDescription() {
    }

    public PhysicalAssetDescription(List<PhysicalAssetAction> actions, List<PhysicalAssetProperty<?>> properties, List<PhysicalAssetEvent> events) {
        this.actions = actions;
        this.properties = properties;
        this.events = events;
    }

    public List<PhysicalAssetAction> getActions() {
        return actions;
    }

    public void setActions(List<PhysicalAssetAction> actions) {
        this.actions = actions;
    }

    public List<PhysicalAssetProperty<?>> getProperties() {
        return properties;
    }

    public void setProperties(List<PhysicalAssetProperty<?>> properties) {
        this.properties = properties;
    }

    public List<PhysicalAssetEvent> getEvents() {
        return events;
    }

    public void setEvents(List<PhysicalAssetEvent> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetDescription{");
        sb.append("actions=").append(actions);
        sb.append(", properties=").append(properties);
        sb.append(", events=").append(events);
        sb.append('}');
        return sb.toString();
    }
}
