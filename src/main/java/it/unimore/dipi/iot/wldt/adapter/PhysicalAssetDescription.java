package it.unimore.dipi.iot.wldt.adapter;

import java.util.List;

public class PhysicalAssetDescription {

    private List<PhysicalAction> actions;

    private List<PhysicalProperty<?>> properties;

    public PhysicalAssetDescription() {
    }

    public PhysicalAssetDescription(List<PhysicalAction> actions, List<PhysicalProperty<?>> properties) {
        this.actions = actions;
        this.properties = properties;
    }

    public List<PhysicalAction> getActions() {
        return actions;
    }

    public void setActions(List<PhysicalAction> actions) {
        this.actions = actions;
    }

    public List<PhysicalProperty<?>> getProperties() {
        return properties;
    }

    public void setProperties(List<PhysicalProperty<?>> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetState{");
        sb.append("actions=").append(actions);
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }
}
