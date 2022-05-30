package it.unimore.wldt.test.adapter;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.ModelException;
import it.unimore.dipi.iot.wldt.exception.ModelFunctionException;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.model.ShadowingModelFunction;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PhysicalAdapterTester {

    private static final Logger logger = LoggerFactory.getLogger(PhysicalAdapterTester.class);

    private static CountDownLatch telemetryLock = null;

    private static CountDownLatch actionLock = null;

    private static List<PhysicalEventMessage<Double>> receivedPhysicalTelemetryEventMessageList = null;

    private static List<PhysicalEventMessage<String>> receivedPhysicalSwitchEventMessageList = null;

    private WldtConfiguration buildWldtConfiguration() throws WldtConfigurationException, ModelException {

        //Manual creation of the WldtConfiguration
        WldtConfiguration wldtConfiguration = new WldtConfiguration();
        wldtConfiguration.setDeviceNameSpace("it.unimore.dipi.things");
        wldtConfiguration.setWldtBaseIdentifier("wldt");
        wldtConfiguration.setWldtStartupTimeSeconds(10);
        wldtConfiguration.setApplicationMetricsEnabled(true);
        wldtConfiguration.setApplicationMetricsReportingPeriodSeconds(10);
        wldtConfiguration.setMetricsReporterList(Collections.singletonList("csv"));
        wldtConfiguration.setGraphitePrefix("wldt");
        wldtConfiguration.setGraphiteReporterAddress("127.0.0.1");
        wldtConfiguration.setGraphiteReporterPort(2003);

        return wldtConfiguration;
    }


    private ShadowingModelFunction getTargetShadowingFunction(){

        return new ShadowingModelFunction("demo-shadowing-model-function") {

            private boolean isShadowed = false;

            @Override
            protected void onCreate() {
                logger.debug("Shadowing Function - onCreate()");
            }

            @Override
            protected void onStart() {
                logger.debug("Shadowing Function - onStart()");
            }

            @Override
            protected void onStop() {
                logger.debug("Shadowing Function - onStop()");
            }

            @Override
            protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
                logger.debug("DigitalTwin - LifeCycleListener - onDigitalTwinBound()");

                for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                    String adapterId = entry.getKey();
                    PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                    logger.info("Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                    try{
                        if(physicalAssetDescription != null && physicalAssetDescription.getProperties() != null && physicalAssetDescription.getProperties().size() > 0){
                            logger.info("Observing Physical Asset Properties: {}", physicalAssetDescription.getProperties());
                            this.observePhysicalProperties(physicalAssetDescription.getProperties());
                        }
                        else
                            logger.info("Empty property list on adapter {}. Nothing to observe !", adapterId);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
                logger.debug("DigitalTwin - LifeCycleListener - onDigitalTwinUnBound()");
            }

            @Override
            protected void onPhysicalAdapterBidingUpdate(String adapterId, PhysicalAssetDescription adapterPhysicalAssetDescription) {
                logger.debug("DigitalTwin - LifeCycleListener - onPhysicalAdapterBidingUpdate()");
            }

            @Override
            protected void onPhysicalEvent(PhysicalEventMessage<?> physicalEventMessage) {

                logger.info("ShadowingModelFunction Physical Event Received: {}", physicalEventMessage);

                if(physicalEventMessage != null
                        && getPhysicalEventsFilter().contains(physicalEventMessage.getType())){

                    if(!isShadowed){
                        isShadowed = true;
                        notifyShadowingSync();
                    }

                    //Check if it is a switch change
                    if(PhysicalEventMessage.buildEventType(DummyPhysicalAdapter.SWITCH_PROPERTY_KEY).equals(physicalEventMessage.getType())
                            && physicalEventMessage.getBody() instanceof String){

                        logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalEventMessage.getType(), physicalEventMessage);

                        if(actionLock != null)
                            actionLock.countDown();

                        if(receivedPhysicalSwitchEventMessageList != null)
                            receivedPhysicalSwitchEventMessageList.add((PhysicalEventMessage<String>) physicalEventMessage);
                    }
                    else{

                        logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalEventMessage.getType(), physicalEventMessage);

                        if(telemetryLock != null)
                            telemetryLock.countDown();

                        if(receivedPhysicalTelemetryEventMessageList != null)
                            receivedPhysicalTelemetryEventMessageList.add((PhysicalEventMessage<Double>) physicalEventMessage);
                    }
                }
                else
                    logger.error("WRONG Physical Event Message Received !");
            }
        };
    }

    @Test
    public void testPhysicalAdapterEvents() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException {

        receivedPhysicalTelemetryEventMessageList = new ArrayList<>();

        telemetryLock = new CountDownLatch(DummyPhysicalAdapter.TARGET_GENERATED_MESSAGES);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Create Physical Adapter
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), true);

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(getTargetShadowingFunction(), buildWldtConfiguration());

        wldtEngine.addPhysicalAdapter(dummyPhysicalAdapter);
        wldtEngine.startLifeCycle();

        //Wait until all the messages have been received
        telemetryLock.await((DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS
                        + (DummyPhysicalAdapter.TARGET_GENERATED_MESSAGES*DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)),
                TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalTelemetryEventMessageList);
        assertEquals(DummyPhysicalAdapter.TARGET_GENERATED_MESSAGES, receivedPhysicalTelemetryEventMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

    @Test
    public void testPhysicalAdapterActions() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException {

        receivedPhysicalSwitchEventMessageList = new ArrayList<>();

        //Our target is to received two event changes associated to switch changes
        actionLock = new CountDownLatch(2);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Create Physical Adapter disabling the telemetry since we would like only to test actions and the associated swith events generation
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), false);

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(getTargetShadowingFunction(), buildWldtConfiguration());

        wldtEngine.addPhysicalAdapter(dummyPhysicalAdapter);
        wldtEngine.startLifeCycle();

        logger.info("WLDT Started ! Sleeping (5s) before sending actions ...");
        Thread.sleep(5000);

        //Send a Demo OFF PhysicalAction to the Adapter
        PhysicalActionEventMessage<String> switchOffPhysicalActionEvent = new PhysicalActionEventMessage<String>(DummyPhysicalAdapter.SWITCH_OFF_ACTION, "OFF");
        EventBus.getInstance().publishEvent("demo-action-tester", switchOffPhysicalActionEvent);
        logger.info("Physical Action OFF Sent ! Sleeping (5s) ...");
        Thread.sleep(5000);

        //Send a Demo OFF PhysicalAction to the Adapter
        PhysicalActionEventMessage<String> switchOnPhysicalActionEvent = new PhysicalActionEventMessage<String>(DummyPhysicalAdapter.SWITCH_ON_ACTION, "ON");
        EventBus.getInstance().publishEvent("demo-action-tester", switchOnPhysicalActionEvent);
        logger.info("Physical Action ON Sent ! Sleeping (5s) ...");

        //Wait until all the messages have been received
        actionLock.await(5000, TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalSwitchEventMessageList);
        assertEquals(2, receivedPhysicalSwitchEventMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

}
