package it.unimore.wldt.test.adapter;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetAction;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetProperty;
import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.event.physical.PhysicalAssetActionWldtEvent;
import it.unimore.dipi.iot.wldt.event.physical.PhysicalAssetEventWldtEvent;
import it.unimore.dipi.iot.wldt.event.physical.PhysicalAssetPropertyWldtEvent;
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

    private static CountDownLatch wldtEventsLock = null;

    private static CountDownLatch actionLock = null;

    private static List<PhysicalAssetPropertyWldtEvent<Double>> receivedPhysicalTelemetryEventMessageList = null;

    private static List<PhysicalAssetEventWldtEvent<?>> receivedPhysicalEventEventMessageList = null;

    private static List<PhysicalAssetPropertyWldtEvent<String>> receivedPhysicalSwitchEventMessageList = null;

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
                           for(PhysicalAssetProperty<?> physicalAssetProperty : physicalAssetDescription.getProperties())
                               this.digitalTwinState.createProperty(new DigitalTwinStateProperty<>(physicalAssetProperty.getKey(), physicalAssetProperty.getInitialValue()));

                           for(PhysicalAssetAction physicalAssetAction : physicalAssetDescription.getActions())
                               this.digitalTwinState.enableAction(new DigitalTwinStateAction(physicalAssetAction.getKey(),
                                       physicalAssetAction.getType(),
                                       physicalAssetAction.getContentType()));

                           //TODO ADD EVENTS
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
                               this.observePhysicalAssetProperties(physicalAssetDescription.getProperties());
                           }
                           else
                               logger.info("ShadowingModelFunction - Empty property list on adapter {}. Nothing to observe !", adapterId);
                       }catch (Exception e){
                           e.printStackTrace();
                       }
                   }

                   //Observe all the target available Physical Asset Events for each Adapter
                   for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                       String adapterId = entry.getKey();
                       PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                       logger.info("ShadowingModelFunction - Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                       try{
                           if(physicalAssetDescription != null && physicalAssetDescription.getEvents() != null && physicalAssetDescription.getEvents().size() > 0){
                               logger.info("ShadowingModelFunction - Observing Physical Asset Events: {}", physicalAssetDescription.getEvents());
                               this.observePhysicalAssetEvents(physicalAssetDescription.getEvents());
                           }
                           else
                               logger.info("ShadowingModelFunction - Empty event list on adapter {}. Nothing to observe !", adapterId);

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
            protected void onPhysicalAssetPropertyWldtEvent(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage) {

                try {

                    logger.info("ShadowingModelFunction Physical Event Received: {}", physicalPropertyEventMessage);

                    if(physicalPropertyEventMessage != null && getPhysicalEventsFilter().contains(physicalPropertyEventMessage.getType())){

                        //Check if it is a switch change
                        if(PhysicalAssetPropertyWldtEvent.buildEventType(DummyPhysicalAdapter.SWITCH_PROPERTY_KEY).equals(physicalPropertyEventMessage.getType())
                                && physicalPropertyEventMessage.getBody() instanceof String){

                            logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

                            if(actionLock != null)
                                actionLock.countDown();

                            if(receivedPhysicalSwitchEventMessageList != null)
                                receivedPhysicalSwitchEventMessageList.add((PhysicalAssetPropertyWldtEvent<String>) physicalPropertyEventMessage);
                        }
                        else{

                            logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

                            //Update Digital Twin Status
                            this.digitalTwinState.updateProperty(
                                    new DigitalTwinStateProperty<>(
                                            physicalPropertyEventMessage.getPhysicalPropertyId(),
                                            physicalPropertyEventMessage.getBody()));

                            if(wldtEventsLock != null)
                                wldtEventsLock.countDown();

                            if(receivedPhysicalTelemetryEventMessageList != null)
                                receivedPhysicalTelemetryEventMessageList.add((PhysicalAssetPropertyWldtEvent<Double>) physicalPropertyEventMessage);
                        }
                    }
                    else
                        logger.error("WRONG Physical Event Message Received !");

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            protected void onPhysicalAssetEventWldtEvent(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {

                if(wldtEventsLock != null)
                    wldtEventsLock.countDown();

                logger.info("ShadowingModelFunction Physical Asset Event - Event Received: {}", physicalAssetEventWldtEvent);
                receivedPhysicalEventEventMessageList.add(physicalAssetEventWldtEvent);
                //TODO Handle Event MANAGEMENT ON THE DT
            }
        };
    }

    @Test
    public void testPhysicalAdapterEvents() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException {

        receivedPhysicalTelemetryEventMessageList = new ArrayList<>();
        receivedPhysicalEventEventMessageList = new ArrayList<>();

        receivedDigitalAdapterPropertyCreatedMessageList = new ArrayList<>();
        receivedDigitalAdapterPropertyUpdateMessageList = new ArrayList<>();
        receivedDigitalAdapterPropertyDeletedMessageList = new ArrayList<>();
        receivedDigitalAdapterSyncDigitalTwinStateList = new ArrayList<>();

        wldtEventsLock = new CountDownLatch(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES);

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        WldtEngine wldtEngine = buildWldtEngine(true);
        wldtEngine.startLifeCycle();

        //Wait until all the messages have been received
        wldtEventsLock.await((DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS + ((DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)), TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalTelemetryEventMessageList);

        //Check Received Physical Event on the Shadowing Function
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedPhysicalTelemetryEventMessageList.size());

        //Check Received Physical Asset Events correctly received by the Shadowing Function
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES, receivedPhysicalEventEventMessageList.size());

        //Check Correct Digital Twin State Property Update Events have been received on the Digital Adapter
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
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        WldtEngine wldtEngine = buildWldtEngine(false);
        wldtEngine.startLifeCycle();

        logger.info("WLDT Started ! Sleeping (5s) before sending actions ...");
        Thread.sleep(5000);

        //Send a Demo OFF PhysicalAction to the Adapter
        PhysicalAssetActionWldtEvent<String> switchOffPhysicalActionEvent = new PhysicalAssetActionWldtEvent<String>(DummyPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");
        WldtEventBus.getInstance().publishEvent("demo-action-tester", switchOffPhysicalActionEvent);
        logger.info("Physical Action OFF Sent ! Sleeping (5s) ...");
        Thread.sleep(5000);

        //Send a Demo OFF PhysicalAction to the Adapter
        PhysicalAssetActionWldtEvent<String> switchOnPhysicalActionEvent = new PhysicalAssetActionWldtEvent<String>(DummyPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");
        WldtEventBus.getInstance().publishEvent("demo-action-tester", switchOnPhysicalActionEvent);
        logger.info("Physical Action ON Sent ! Sleeping (5s) ...");

        //Wait until all the messages have been received
        actionLock.await(5000, TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalSwitchEventMessageList);
        assertEquals(2, receivedPhysicalSwitchEventMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

}
