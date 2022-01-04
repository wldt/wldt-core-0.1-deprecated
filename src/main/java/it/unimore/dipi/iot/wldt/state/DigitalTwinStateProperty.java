package it.unimore.dipi.iot.wldt.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Objects;

public class DigitalTwinStateProperty<T> {

    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinStateProperty.class);

    private String key;
    private T value;
    private boolean readable = true;
    private boolean writable = true;

    private DigitalTwinStateProperty() {
    }

    public DigitalTwinStateProperty(String key, T value, boolean readable, boolean writable) {
        this();
        this.key = key;
        this.value = value;
        this.readable = readable;
        this.writable = writable;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DigitalTwinStateProperty<?> that = (DigitalTwinStateProperty<?>) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DigitalTwinStateProperty{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value=").append(value);
        sb.append(", readable=").append(readable);
        sb.append(", writable=").append(writable);
        sb.append('}');
        return sb.toString();
    }
}
