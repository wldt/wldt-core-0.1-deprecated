package it.unimore.dipi.iot.wldt.adapter;

import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.ModelException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public abstract class PhysicalAdapter<C> extends WldtWorker {

    private static final Logger logger = LoggerFactory.getLogger(PhysicalAdapter.class);

    private String id;

    private C configuration;

    private EventFilter digitalActionEventsFilter;

    private PhysicalAdapter(){}

    public PhysicalAdapter(String id, C configuration){
        this.id = id;
        this.configuration = configuration;
    }

    @Override
    public void onWorkerStart() {
        onAdapterStart();
    }

    @Override
    public void onWorkerStop() {
        onAdapterStop();
    }

    @Override
    public void handleWorkerJob() throws WldtRuntimeException {
        try{
            observePhysicalActionEvents();
            handleBinding();
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    public String getId() {
        return id;
    }

    public C getConfiguration() {
        return configuration;
    }

    protected void observePhysicalActionEvents() throws EventBusException, ModelException {

        Optional<List<String>> optionalEventTypeList = getSupportedPhysicalActionEventTypeList();

        if(!optionalEventTypeList.isPresent())
            throw new ModelException("Error ! Missing PhysicalEvent Type List in Shadowing Function ...");

        //Define EventFilter and add the target topics
        EventFilter eventFilter = new EventFilter();
        for(String eventType : optionalEventTypeList.get())
            eventFilter.add(PhysicalActionEventMessage.buildEventType(eventType));

        //Save the adopted EventFilter
        this.digitalActionEventsFilter = eventFilter;

        EventBus.getInstance().subscribe(this.id, this.digitalActionEventsFilter, new EventListener() {
            @Override
            public void onSubscribe(String eventType) {
                logger.debug("{} -> Subscribed to: {}", id, eventType);
            }

            @Override
            public void onUnSubscribe(String eventType) {
                logger.debug("{} -> Unsubscribed from: {}", id, eventType);
            }

            @Override
            public void onEvent(Optional<EventMessage<?>> eventMessage) {
                logger.debug("{} -> Received Event: {}", id, eventMessage);
                if(eventMessage.isPresent() && eventMessage.get() instanceof PhysicalActionEventMessage){
                    onIncomingPhysicalAction((PhysicalActionEventMessage<?>) eventMessage.get());
                }
            }
        });
    }

    abstract protected Optional<List<String>> getSupportedPhysicalActionEventTypeList();

    abstract protected Optional<List<String>> getGeneratedPhysicalEventTypeList();

    public abstract void onIncomingPhysicalAction(PhysicalActionEventMessage<?> physicalActionEventMessage);

    public abstract void handleBinding();

    public abstract void onAdapterStart();

    public abstract void onAdapterStop();

}
