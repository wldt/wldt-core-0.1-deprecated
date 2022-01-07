package it.unimore.dipi.iot.wldt.state;

import it.unimore.dipi.iot.wldt.exception.WldtDigitalTwinStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

public class DigitalTwinStateProperty<T> {

    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinStateProperty.class);

    private String key;
    private T value;
    private String type = null;
    private boolean readable = true;
    private boolean writable = true;
    private boolean exposed = true;

    private DigitalTwinStateProperty() {
    }

    public DigitalTwinStateProperty(String key, T value) throws WldtDigitalTwinStateException {

        if(key == null || value == null)
            throw new WldtDigitalTwinStateException("Error creating DigitalTwinStateProperty ! Key or Value = Null !");

        this.key = key;
        this.value = value;
        this.type = value.getClass().getName();
    }

    public DigitalTwinStateProperty(String key, T value, boolean readable, boolean writable) throws WldtDigitalTwinStateException {
        this(key, value);
        this.readable = readable;
        this.writable = writable;
    }

    public DigitalTwinStateProperty(String key, T value, boolean readable, boolean writable, boolean exposed) throws WldtDigitalTwinStateException {
        this(key, value);
        this.readable = readable;
        this.writable = writable;
        this.exposed = exposed;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isReadable() {
        return readable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public boolean isExposed() {
        return exposed;
    }

    public void setExposed(boolean exposed) {
        this.exposed = exposed;
    }

    public String getType(){
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DigitalTwinStateProperty<?> that = (DigitalTwinStateProperty<?>) o;
        return readable == that.readable && writable == that.writable && exposed == that.exposed && key.equals(that.key) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, readable, writable, exposed);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DigitalTwinStateProperty{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value=").append(value);
        sb.append(", readable=").append(readable);
        sb.append(", writable=").append(writable);
        sb.append(", exposed=").append(exposed);
        sb.append('}');
        return sb.toString();
    }
}
