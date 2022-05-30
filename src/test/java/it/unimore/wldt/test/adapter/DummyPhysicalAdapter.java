package it.unimore.wldt.test.adapter;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAction;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAdapter;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.adapter.PhysicalProperty;
import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.event.PhysicalActionEventMessage;
import it.unimore.dipi.iot.wldt.event.PhysicalPropertyEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Random;

public class DummyPhysicalAdapter extends PhysicalAdapter<DummyPhysicalAdapterConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DummyPhysicalAdapter.class);

    public static final int TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES = 10;

    public static long MESSAGE_SLEEP_PERIOD_MS = 2000;

    public static final String ENERGY_PROPERTY_KEY = "energy";

    public static final String SWITCH_PROPERTY_KEY = "switch";

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
    public void onIncomingPhysicalAction(PhysicalActionEventMessage<?> physicalActionEventMessage) {
        try{
            logger.info("Received PhysicalActionEventMessage: {}", physicalActionEventMessage);

            if(physicalActionEventMessage != null && physicalActionEventMessage.getType().equals(PhysicalActionEventMessage.buildEventType(SWITCH_ON_ACTION))) {
                logger.info("{} Received ! Switching ON the device ...", physicalActionEventMessage.getType());
                Thread.sleep(MESSAGE_SLEEP_PERIOD_MS);
                EventBus.getInstance().publishEvent(getId(), new PhysicalPropertyEventMessage<>(SWITCH_PROPERTY_KEY, "ON"));
            } else if(physicalActionEventMessage != null && physicalActionEventMessage.getType().equals(PhysicalActionEventMessage.buildEventType(SWITCH_OFF_ACTION))){
                logger.info("{} Received ! Switching OFF the device ...", physicalActionEventMessage.getType());
                Thread.sleep(MESSAGE_SLEEP_PERIOD_MS);
                EventBus.getInstance().publishEvent(getId(), new PhysicalPropertyEventMessage<>(SWITCH_PROPERTY_KEY, "OFF"));
            } else
                logger.error("WRONG OR NULL ACTION RECEIVED !");

        }catch (Exception e){
           e.printStackTrace();
        }
    }

    @Override
    public void onAdapterStart() {

        //Emulate the real device on a different Thread and then send the PhysicalEvent
        if(isTelemetryOn)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for(int i = 0; i< TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES; i++){
                            Thread.sleep(MESSAGE_SLEEP_PERIOD_MS);
                            double randomEnergyValue = 10 + (100 - 10) * random.nextDouble();
                            publishPhysicalEventMessage(new PhysicalPropertyEventMessage<>(ENERGY_PROPERTY_KEY, randomEnergyValue));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();

        try{

            PhysicalAssetDescription physicalAssetDescription = new PhysicalAssetDescription();

            physicalAssetDescription.setActions(new ArrayList<PhysicalAction>() {{
                add(new PhysicalAction(SWITCH_OFF_ACTION, "demo.actuation", "application/json"));
                add(new PhysicalAction(SWITCH_ON_ACTION, "demo.actuation", "application/json"));
            }});

            physicalAssetDescription.setProperties(new ArrayList<PhysicalProperty<?>>() {{
                add(new PhysicalProperty<String>(SWITCH_PROPERTY_KEY, "OFF"));
                add(new PhysicalProperty<Double>(ENERGY_PROPERTY_KEY, 0.0));
            }});

            this.notifyPhysicalAdapterBound(physicalAssetDescription);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onAdapterStop() {
        logger.info("DummyPhysicalAdapter Stopped !");
    }
}
