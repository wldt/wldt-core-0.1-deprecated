package it.unimore.wldt.test.shadowing;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.adapter.PhysicalProperty;
import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.event.EventListener;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.model.ShadowingModelFunction;
import it.unimore.dipi.iot.wldt.state.DefaultDigitalTwinState;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import it.unimore.wldt.test.adapter.DummyDigitalAdapter;
import it.unimore.wldt.test.adapter.DummyDigitalAdapterConfiguration;
import it.unimore.wldt.test.adapter.DummyPhysicalAdapter;
import it.unimore.wldt.test.adapter.DummyPhysicalAdapterConfiguration;
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
public class ShadowingFunctionTester {

    private static final Logger logger = LoggerFactory.getLogger(ShadowingFunctionTester.class);

    private static CountDownLatch testCountDownLatch = null;

    private static List<EventMessage<?>> receivedStateEventMessageList = null;

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

    private void createDigitalTwinStateObserver() throws EventBusException {

        //Define EventFilter and add the target topic
        EventFilter eventFilter = new EventFilter();
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);
        eventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);

        EventBus.getInstance().subscribe("demo-state-observer", eventFilter, new EventListener() {
            @Override
            public void onEventSubscribed(String eventType) {
                logger.info("DT-State-Observer - onEventSubscribed(): {}", eventType);
            }

            @Override
            public void onEventUnSubscribed(String eventType) {
                logger.info("DT-State-Observer - onEventUnSubscribed(): {}", eventType);
            }

            @Override
            public void onEvent(EventMessage<?> eventMessage) {

                if(eventMessage != null && eventMessage.getBody() != null && (eventMessage.getBody() instanceof DigitalTwinStateProperty)) {
                    logger.info("DT-State-Observer - onEvent(): Type: {} Event:{}", eventMessage.getType(), eventMessage);
                    receivedStateEventMessageList.add(eventMessage);
                    testCountDownLatch.countDown();
                }
                else
                    logger.error("DT-State-Observer - ERROR Wrong Event Received: {}", eventMessage);
            }
        });
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

                try {

                    for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                        String adapterId = entry.getKey();
                        PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                        logger.info("Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                        //Analyze Physical Asset Properties for the target PhysicalAdapter
                        for(PhysicalProperty<?> physicalProperty : physicalAssetDescription.getProperties()) {

                            String physicalPropertyKey = physicalProperty.getKey();
                            String physicalPropertyType = physicalProperty.getType();

                            logger.info("New Physical Property Detected ! Key: {} Type: {} InstanceType: {}", physicalPropertyKey, physicalPropertyType, physicalProperty.getClass());

                            //Update Digital Twin State creating the new Property
                            if(!this.digitalTwinState.containsProperty(physicalPropertyKey)) {

                                this.digitalTwinState.createProperty(new DigitalTwinStateProperty<>(
                                                physicalPropertyKey,
                                                physicalProperty.getInitialValue()));

                                logger.info("New DigitalTwinStateProperty {} Created !", physicalPropertyKey);
                            }
                            else
                                logger.warn("DT Property {} already available !", physicalPropertyKey);

                            //Observe Physical Property in order to receive Physical Events related to Asset updates
                            logger.info("Observing Physical Asset Property: {}", physicalPropertyKey);
                            this.observePhysicalProperty(physicalProperty);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
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
            protected void onPhysicalEvent(PhysicalPropertyEventMessage<?> physicalPropertyEventMessage) {

                try{

                    logger.info("ShadowingModelFunction Physical Event Received: {}", physicalPropertyEventMessage);

                    if(physicalPropertyEventMessage != null
                            && getPhysicalEventsFilter().contains(physicalPropertyEventMessage.getType())
                            && physicalPropertyEventMessage.getPhysicalPropertyId() != null
                            && this.digitalTwinState.containsProperty(physicalPropertyEventMessage.getPhysicalPropertyId())
                            && physicalPropertyEventMessage.getBody() instanceof Double
                    ){

                        logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

                        //Update DT State Property
                        this.digitalTwinState.updateProperty(
                                new DigitalTwinStateProperty<Double>(
                                        physicalPropertyEventMessage.getPhysicalPropertyId(),
                                        (Double) physicalPropertyEventMessage.getBody()));

                        if(!isShadowed){
                            isShadowed = true;
                            notifyShadowingSync();
                        }

                    }
                    else
                        logger.error("WRONG Physical Event Message Received !");

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }

    @Test
    public void testShadowingFunctionOnPhysicalEvents() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException {

        receivedStateEventMessageList = new ArrayList<>();

        testCountDownLatch = new CountDownLatch(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Register DigitalTwin State Observer
        createDigitalTwinStateObserver();

        //Create Physical Adapter
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), true);

        //Create Digital Adapter
        DummyDigitalAdapter dummyDigitalAdapter = new DummyDigitalAdapter("dummy-digital-adapter", new DummyDigitalAdapterConfiguration());

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(getTargetShadowingFunction(), buildWldtConfiguration());
        wldtEngine.addPhysicalAdapter(dummyPhysicalAdapter);
        wldtEngine.addDigitalAdapter(dummyDigitalAdapter);
        wldtEngine.startLifeCycle();

        //Wait until all the messages have been received
        testCountDownLatch.await((DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS
                        + (DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES *DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)),
                TimeUnit.MILLISECONDS);

        assertNotNull(receivedStateEventMessageList);
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedStateEventMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

}
