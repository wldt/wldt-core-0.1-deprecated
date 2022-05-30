package it.unimore.dipi.iot.wldt.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 20/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class define a generic property associated to the Digital Twin State.
 * Each property is associated to a Key and a Value. Furthermore, it can also be associated to a type
 * to identify its nature and data structure. By default, it is associated to the type of the
 * Class (e.g., java.lang.String) but it can be directly changed by the developer
 * to associate it to a specific ontology or data type.
 *
 * @param <T>
 */
public class PhysicalProperty<T> {

    private static final Logger logger = LoggerFactory.getLogger(PhysicalProperty.class);

    /**
     * Key uniquely identifying the property in the Digital Twin State
     */
    private String key;

    /**
     * Value of the property for the target Digital Twin State
     */
    private T initialValue;

    /**
     * Type of the Property. By default, it is associated to the type of the Class (e.g., java.lang.String) but it
     * can be directly changed by the developer to associate it to a specific ontology or data type.
     * Furthermore, it can be useful if the event management system will be extended to the event-base communication
     * between DTs over the network. In that case, the field can be used to de-serialize the object and understand
     * the property type
     */
    private String type = null;

    /**
     * Identify if the property is immutable by external modules. If it is immutable the initial exposed value does not
     * change during the lifecycle of the physical asset
     */
    private boolean immutable = false;

    /**
     * Identify if the property is writable by external modules
     */
    private boolean writable = true;

    private PhysicalProperty() {
    }

    public PhysicalProperty(String key, T initialValue) {
        this.key = key;
        this.initialValue = initialValue;
        this.type = initialValue.getClass().getName();
    }

    public PhysicalProperty(String key, T initialValue, boolean immutable, boolean writable) {
        this(key, initialValue);
        this.immutable = immutable;
        this.writable = writable;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(T initialValue) {
        this.initialValue = initialValue;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public String getType(){
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalProperty<?> that = (PhysicalProperty<?>) o;
        return immutable == that.immutable && writable == that.writable && key.equals(that.key) && initialValue.equals(that.initialValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, initialValue, immutable, writable);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalProperty{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value=").append(initialValue);
        sb.append(", readable=").append(immutable);
        sb.append(", writable=").append(writable);
        sb.append('}');
        return sb.toString();
    }
}
