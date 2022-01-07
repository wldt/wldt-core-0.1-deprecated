package it.unimore.dipi.iot.wldt.event;

import java.util.*;

public class EventMessage<T> {

    private String id;
    private String topic;
    private T body;
    private Map<String, Object> metadata;
    private long creationTimestamp;

    private EventMessage() {
        this.metadata = new HashMap<>();
        this.id = UUID.randomUUID().toString();
        this.creationTimestamp = System.currentTimeMillis();
    }

    public EventMessage(String topic) {
        this();
        this.topic = topic;
    }

    public EventMessage(String topic, T body) {
        this();
        this.topic = topic;
        this.body = body;
    }

    public EventMessage(String topic, T body, Map<String, Object> metadata) {
        this();
        this.topic = topic;
        this.body = body;
        this.metadata = metadata;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Optional<Object> putMetadata(String key, Object value){
        if(this.metadata != null) {
            this.metadata.put(key, value);
            return Optional.of(this.metadata.get(key));
        }
        else
            return Optional.empty();
    }

    public Optional<Object> getMetadata(String key){
        if(this.metadata != null && this.metadata.containsKey(key))
            return Optional.of(this.metadata.get(key));
        else
            return Optional.empty();
    }

    public Optional<Object>  removeMetadata(String key){
        if(this.metadata != null && this.metadata.containsKey(key))
            return Optional.of(this.metadata.remove(key));
        else
            return Optional.empty();
    }

    private void setId(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    private void setCreationTimestamp(long creationTimestamp){
        this.creationTimestamp = creationTimestamp;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventMessage<?> that = (EventMessage<?>) o;
        return id.equals(that.id) && topic.equals(that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EventMessage{");
        sb.append("id='").append(id).append('\'');
        sb.append(", topic='").append(topic).append('\'');
        sb.append(", body=").append(body);
        sb.append(", metadata=").append(metadata);
        sb.append(", creationTimestamp=").append(creationTimestamp);
        sb.append('}');
        return sb.toString();
    }
}

