package it.unimore.wldt.test.adapter;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAdapter;
import it.unimore.dipi.iot.wldt.event.DigitalActionEventMessage;
import it.unimore.dipi.iot.wldt.event.EventBus;
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

    private String ENERGY_MESSAGE_TYPE = "telemetry.energy";

    private Random random = new Random();

    public DummyPhysicalAdapter(String id, DummyPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    @Override
    protected Optional<List<String>> getSupportedDigitalActionEventTypeList() {
        return Optional.of(new ArrayList<String>() {{
            add("switch_off");
            add("switch_on");
        }});
    }

    @Override
    protected Optional<List<String>> getGeneratedPhysicalEventTypeList() {
        return Optional.of(new ArrayList<String>() {{
            add(ENERGY_MESSAGE_TYPE);
        }});
    }

    @Override
    public void onIncomingDigitalAction(DigitalActionEventMessage<?> physicalEventMessage) {
        //TODO Add something here :)
    }

    @Override
    public void handleBinding() {
        //Emulate the real device on a different Thread and then send the PhysicalEvent
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
    }
}
