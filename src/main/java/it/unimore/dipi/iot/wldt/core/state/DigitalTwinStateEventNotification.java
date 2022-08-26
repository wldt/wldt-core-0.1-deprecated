package it.unimore.dipi.iot.wldt.core.state;

import it.unimore.dipi.iot.wldt.core.event.WldtEvent;
import it.unimore.dipi.iot.wldt.exception.EventBusException;

import java.util.Map;

public class DigitalTwinStateEventNotification<T> {

    public static final String DIGITAL_TWIN_STATE_EVENT_BASIC_TYPE = "dt.digital.event.event";

    private String digitalEventKey;

    private T body;

    public DigitalTwinStateEventNotification(String digitalEventKey, T body) throws EventBusException {
        this.digitalEventKey = digitalEventKey;
        this.body = body;
    }

    public String getDigitalEventKey() {
        return digitalEventKey;
    }

    public void setDigitalEventKey(String digitalEventKey) {
        this.digitalEventKey = digitalEventKey;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DigitalTwinStateEventNotification{");
        sb.append("digitalEventKey='").append(digitalEventKey).append('\'');
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
