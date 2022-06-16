package it.unimore.wldt.test.lifecycle;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.engine.LifeCycleListener;
import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.event.DefaultEventLogger;
import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.event.PhysicalPropertyEventMessage;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.model.ShadowingModelFunction;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
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
public class LifeCycleTester {

    private static final Logger logger = LoggerFactory.getLogger(LifeCycleTester.class);

    private static CountDownLatch lock = null;

    private static List<PhysicalPropertyEventMessage<Double>> receivedPhysicalTelemetryEventMessageList = null;

    private static List<PhysicalPropertyEventMessage<String>> receivedPhysicalSwitchEventMessageList = null;

    private static final String DEMO_MQTT_BODY = "DEMO_BODY_MQTT";

    private static final String DEMO_MQTT_MESSAGE_TYPE = "mqtt.telemetry";

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


    @Test
    public void testLifeCycle() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException {

        this.receivedPhysicalTelemetryEventMessageList = new ArrayList<>();

        lock = new CountDownLatch(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Create Physical Adapter
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), true);

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(new ShadowingModelFunction("test-shadowing-function") {

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
            protected void onPhysicalEvent(PhysicalPropertyEventMessage<?> physicalEventMessage) {

                logger.info("onPhysicalEvent()-> {}", physicalEventMessage);

                if(physicalEventMessage != null
                        && getPhysicalEventsFilter().contains(physicalEventMessage.getType())
                        && physicalEventMessage.getBody() instanceof Double){

                    if(!isShadowed){
                        isShadowed = true;
                        if(getShadowingModelListener() != null)
                            notifyShadowingSync();
                        else
                            logger.error("ERROR ShadowingListener = NULL !");
                    }

                    lock.countDown();
                    receivedPhysicalTelemetryEventMessageList.add((PhysicalPropertyEventMessage<Double>) physicalEventMessage);
                }
                else
                    logger.error("WRONG Physical Event Message Received !");
            }

        }, buildWldtConfiguration());

        wldtEngine.addPhysicalAdapter(dummyPhysicalAdapter);
        wldtEngine.addLifeCycleListener(new LifeCycleListener() {
            @Override
            public void onCreate() {
                logger.debug("LifeCycleListener - onCreate()");
            }

            @Override
            public void onStart() {
                logger.debug("LifeCycleListener - onStart()");
            }

            @Override
            public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
                logger.debug("LifeCycleListener - onPhysicalAdapterBound({})", adapterId);
            }

            @Override
            public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
                logger.debug("LifeCycleListener - onPhysicalAdapterBindingUpdate({})", adapterId);
            }

            @Override
            public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
                logger.debug("LifeCycleListener - onAdapterUnBound({}) Reason: {}", adapterId, errorMessage);
            }

            @Override
            public void onDigitalAdapterBound(String adapterId) {
                logger.debug("LifeCycleListener - onDigitalAdapterBound({})", adapterId);
            }

            @Override
            public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {
                logger.debug("LifeCycleListener - onDigitalAdapterUnBound({}) Error: {}", adapterId, errorMessage);
            }

            @Override
            public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
                logger.debug("LifeCycleListener - onBound()");
            }

            @Override
            public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
                logger.debug("LifeCycleListener - onUnBound() Reason: {}", errorMessage);
            }

            @Override
            public void onSync(IDigitalTwinState digitalTwinState) {
                logger.debug("LifeCycleListener - onSync() - DT State: {}", digitalTwinState);
            }

            @Override
            public void onUnSync(IDigitalTwinState digitalTwinState) {
                logger.debug("LifeCycleListener - onUnSync() - DT State: {}", digitalTwinState);
            }

            @Override
            public void onStop() {
                logger.debug("LifeCycleListener - onStop()");
            }

            @Override
            public void onDestroy() {
                logger.debug("LifeCycleListener - onDestroy()");
            }
        });

        wldtEngine.startLifeCycle();

        //Wait until all the messages have been received
        lock.await((DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS
                        + (DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES *DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)),
                TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalTelemetryEventMessageList);
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedPhysicalTelemetryEventMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

}
