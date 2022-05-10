package it.unimore.wldt.test.adapter;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAdapter;
import it.unimore.dipi.iot.wldt.event.DigitalActionEventMessage;
import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.event.PhysicalActionEventMessage;
import it.unimore.dipi.iot.wldt.event.PhysicalEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DummyPhysicalAdapter extends PhysicalAdapter<DummyPhysicalAdapterConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DummyPhysicalAdapter.class);

    public static final int TARGET_GENERATED_MESSAGES = 10;

    public static long MESSAGE_SLEEP_PERIOD_MS = 2000;

    public static final String ENERGY_MESSAGE_TYPE = "telemetry.energy";

    public static final String EVENT_SWITCH_MESSAGE_TYPE = "switch";

    public static final String SWITCH_OFF_ACTION = "switch_off";

    public static final String SWITCH_ON_ACTION = "switch_on";

    private boolean isTelemetryOn = false;

    private Random random = new Random();

    public DummyPhysicalAdapter(String id, DummyPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    public DummyPhysicalAdapter(String id, DummyPhysicalAdapterConfiguration configuration, boolean isTelemetryOn) {
        super(id, configuration);
        this.isTelemetryOn = isTelemetryOn;
    }

    @Override
    public Optional<List<String>> getSupportedPhysicalActionEventTypeList() {
        return Optional.of(new ArrayList<String>() {{
            add(SWITCH_OFF_ACTION);
            add(SWITCH_ON_ACTION);
        }});
    }

    @Override
    public Optional<List<String>> getGeneratedPhysicalEventTypeList() {
        return Optional.of(new ArrayList<String>() {{
            add(ENERGY_MESSAGE_TYPE);
            add(EVENT_SWITCH_MESSAGE_TYPE);
        }});
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalActionEventMessage<?> physicalActionEventMessage) {
        try{
            logger.info("Received PhysicalActionEventMessage: {}", physicalActionEventMessage);

            if(physicalActionEventMessage != null && physicalActionEventMessage.getType().equals(PhysicalActionEventMessage.buildEventType(SWITCH_ON_ACTION))) {
                logger.info("{} Received ! Switching ON the device ...", physicalActionEventMessage.getType());
                Thread.sleep(MESSAGE_SLEEP_PERIOD_MS);
                EventBus.getInstance().publishEvent(getId(), new PhysicalEventMessage<>(EVENT_SWITCH_MESSAGE_TYPE, "ON"));
            } else if(physicalActionEventMessage != null && physicalActionEventMessage.getType().equals(PhysicalActionEventMessage.buildEventType(SWITCH_OFF_ACTION))){
                logger.info("{} Received ! Switching OFF the device ...", physicalActionEventMessage.getType());
                Thread.sleep(MESSAGE_SLEEP_PERIOD_MS);
                EventBus.getInstance().publishEvent(getId(), new PhysicalEventMessage<>(EVENT_SWITCH_MESSAGE_TYPE, "OFF"));
            } else
                logger.error("WRONG OR NULL ACTION RECEIVED !");

        }catch (Exception e){
           e.printStackTrace();
        }
    }

    @Override
    public void handleBinding() {
        //Emulate the real device on a different Thread and then send the PhysicalEvent
        if(isTelemetryOn)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        if(getPhysicalAdapterListener() != null)
                            getPhysicalAdapterListener().onBound(getId());

                        for(int i=0; i<TARGET_GENERATED_MESSAGES; i++){
                            Thread.sleep(MESSAGE_SLEEP_PERIOD_MS);
                            double randomEnergyValue = 10 + (100 - 10) * random.nextDouble();
                            EventBus.getInstance().publishEvent(getId(), new PhysicalEventMessage<>(ENERGY_MESSAGE_TYPE, randomEnergyValue));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    @Override
    public void onAdapterStart() {
        logger.info("DummyPhysicalAdapter Started !");
    }

    @Override
    public void onAdapterStop() {
        logger.info("DummyPhysicalAdapter Stopped !");
        if(getPhysicalAdapterListener() != null)
            getPhysicalAdapterListener().onUnBound(this.getId(), Optional.empty());
    }
}
