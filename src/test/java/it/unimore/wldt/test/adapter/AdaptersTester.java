package it.unimore.wldt.test.adapter;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAction;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.adapter.PhysicalProperty;
import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.model.ShadowingModelFunction;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateAction;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
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
public class AdaptersTester {

    private static final Logger logger = LoggerFactory.getLogger(AdaptersTester.class);

    private static CountDownLatch telemetryLock = null;

    private static CountDownLatch actionLock = null;

    private static List<PhysicalPropertyEventMessage<Double>> receivedPhysicalTelemetryEventMessageList = null;

    private static List<PhysicalPropertyEventMessage<String>> receivedPhysicalSwitchEventMessageList = null;

    private static List<DigitalTwinStateProperty<?>> receivedDigitalAdapterPropertyCreatedMessageList = null;

    private static List<DigitalTwinStateProperty<?>> receivedDigitalAdapterPropertyUpdateMessageList = null;

    private static List<DigitalTwinStateProperty<?>> receivedDigitalAdapterPropertyDeletedMessageList = null;

    private static List<IDigitalTwinState> receivedDigitalAdapterSyncDigitalTwinStateList = null;

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

    private WldtEngine buildWldtEngine(boolean physicalTelemetryOn) throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException {

        //Create Physical Adapter
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), physicalTelemetryOn);

        //Create Digital Adapter
        DummyDigitalAdapter dummyDigitalAdapter = new DummyDigitalAdapter("dummy-physical-adapter", new DummyDigitalAdapterConfiguration(),
                receivedDigitalAdapterPropertyCreatedMessageList,
                receivedDigitalAdapterPropertyUpdateMessageList,
                receivedDigitalAdapterPropertyDeletedMessageList,
                receivedDigitalAdapterSyncDigitalTwinStateList
        );

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(getTargetShadowingFunction(), buildWldtConfiguration());
        wldtEngine.addPhysicalAdapter(dummyPhysicalAdapter);
        wldtEngine.addDigitalAdapter(dummyDigitalAdapter);

        return wldtEngine;
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

               try{

                   logger.debug("ShadowingModelFunction - DigitalTwin - LifeCycleListener - onDigitalTwinBound()");

                   //Handle Shadowing & Update Digital Twin State
                   if(!isShadowed){

                       isShadowed = true;

                       for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                           String adapterId = entry.getKey();
                           PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                           //In that simple case the Digital Twin shadow all the properties and actions available in the physical asset
                           for(PhysicalProperty<?> physicalProperty : physicalAssetDescription.getProperties())
                               this.digitalTwinState.createProperty(new DigitalTwinStateProperty<>(physicalProperty.getKey(), physicalProperty.getInitialValue()));

                           for(PhysicalAction physicalAction : physicalAssetDescription.getActions())
                               this.digitalTwinState.enableAction(new DigitalTwinStateAction(physicalAction.getKey(),
                                       physicalAction.getType(),
                                       physicalAction.getContentType()));
                       }

                       //Notify Shadowing Completed
                       notifyShadowingSync();
                   }

                   //Observer Target Physical Properties
                   for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                       String adapterId = entry.getKey();
                       PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                       logger.info("ShadowingModelFunction - Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                       try{
                           if(physicalAssetDescription != null && physicalAssetDescription.getProperties() != null && physicalAssetDescription.getProperties().size() > 0){
                               logger.info("ShadowingModelFunction - Observing Physical Asset Properties: {}", physicalAssetDescription.getProperties());
                               this.observePhysicalProperties(physicalAssetDescription.getProperties());
                           }
                           else
                               logger.info("ShadowingModelFunction - Empty property list on adapter {}. Nothing to observe !", adapterId);
                       }catch (Exception e){
                           e.printStackTrace();
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

                try {

                    logger.info("ShadowingModelFunction Physical Event Received: {}", physicalPropertyEventMessage);

                    if(physicalPropertyEventMessage != null && getPhysicalEventsFilter().contains(physicalPropertyEventMessage.getType())){

                        //Check if it is a switch change
                        if(PhysicalPropertyEventMessage.buildEventType(DummyPhysicalAdapter.SWITCH_PROPERTY_KEY).equals(physicalPropertyEventMessage.getType())
                                && physicalPropertyEventMessage.getBody() instanceof String){

                            logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

                            if(actionLock != null)
                                actionLock.countDown();

                            if(receivedPhysicalSwitchEventMessageList != null)
                                receivedPhysicalSwitchEventMessageList.add((PhysicalPropertyEventMessage<String>) physicalPropertyEventMessage);
                        }
                        else{

                            logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

                            //Update Digital Twin Status
                            this.digitalTwinState.updateProperty(
                                    new DigitalTwinStateProperty<>(
                                            physicalPropertyEventMessage.getPhysicalPropertyId(),
                                            physicalPropertyEventMessage.getBody()));

                            if(telemetryLock != null)
                                telemetryLock.countDown();

                            if(receivedPhysicalTelemetryEventMessageList != null)
                                receivedPhysicalTelemetryEventMessageList.add((PhysicalPropertyEventMessage<Double>) physicalPropertyEventMessage);
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
    public void testPhysicalAdapterEvents() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException {

        receivedPhysicalTelemetryEventMessageList = new ArrayList<>();
        receivedDigitalAdapterPropertyCreatedMessageList = new ArrayList<>();
        receivedDigitalAdapterPropertyUpdateMessageList = new ArrayList<>();
        receivedDigitalAdapterPropertyDeletedMessageList = new ArrayList<>();
        receivedDigitalAdapterSyncDigitalTwinStateList = new ArrayList<>();

        telemetryLock = new CountDownLatch(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        WldtEngine wldtEngine = buildWldtEngine(true);
        wldtEngine.startLifeCycle();

        //Wait until all the messages have been received
        telemetryLock.await((DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS
                        + (DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES *DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)),
                TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalTelemetryEventMessageList);
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedPhysicalTelemetryEventMessageList.size());
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedDigitalAdapterPropertyUpdateMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

    @Test
    public void testPhysicalAdapterActions() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException {

        receivedPhysicalSwitchEventMessageList = new ArrayList<>();

        //Our target is to received two event changes associated to switch changes
        actionLock = new CountDownLatch(2);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        WldtEngine wldtEngine = buildWldtEngine(false);
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
