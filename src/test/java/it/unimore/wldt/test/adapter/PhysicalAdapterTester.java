package it.unimore.wldt.test.adapter;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetState;
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

    private CountDownLatch lock = null;

    private List<PhysicalEventMessage<Double>> receivedPhysicalTelemetryEventMessageList = null;

    private List<PhysicalEventMessage<String>> receivedPhysicalSwitchEventMessageList = null;

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
    public void testPhysicalAdapterEvents() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException {

        this.receivedPhysicalTelemetryEventMessageList = new ArrayList<>();

        lock = new CountDownLatch(DummyPhysicalAdapter.TARGET_GENERATED_MESSAGES);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Create Physical Adapter
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), true);

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(new ShadowingModelFunction("test-shadowing-function") {

            @Override
            protected void onCreate() {
                logger.debug("ShadowingModelFunction - onCreate()");
            }

            @Override
            protected void onStart() {
                logger.debug("ShadowingModelFunction - onStart()");
            }

            @Override
            protected void onStop() {
                logger.debug("ShadowingModelFunction - onStop()");
            }

            @Override
            protected void onPhysicalAdapterBound(String adapterId, PhysicalAssetState adapterPhysicalAssetState) {
                if(adapterPhysicalAssetState != null){
                    logger.info("Received a valid AdapterPhysicalAssetState");
                    if(adapterPhysicalAssetState.getProperties() != null && adapterPhysicalAssetState.getProperties().size() > 0){
                        try {
                            observePhysicalProperties(adapterPhysicalAssetState.getProperties());
                        }catch (Exception e){
                            logger.error("ERROR Observing Physical Properties ! Msg: {}", e.getLocalizedMessage());
                        }
                    }
                }
                else
                    logger.warn("WARNING ! Received a Null AdapterPhysicalAssetState");
            }

            @Override
            protected void onPhysicalEvent(PhysicalEventMessage<?> physicalEventMessage) {

                logger.info("onPhysicalEvent()-> {}", physicalEventMessage);

                if(physicalEventMessage != null
                        && getPhysicalEventsFilter().contains(physicalEventMessage.getType())
                        && physicalEventMessage.getBody() instanceof Double){
                    logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalEventMessage.getType(), physicalEventMessage);
                    lock.countDown();
                    receivedPhysicalTelemetryEventMessageList.add((PhysicalEventMessage<Double>) physicalEventMessage);
                }
                else
                    logger.error("WRONG Physical Event Message Received !");
            }
        }, buildWldtConfiguration());

        wldtEngine.addPhysicalAdapter(dummyPhysicalAdapter);
        wldtEngine.startLifeCycle();

        //Wait until all the messages have been received
        lock.await((DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS
                        + (DummyPhysicalAdapter.TARGET_GENERATED_MESSAGES*DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)),
                TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalTelemetryEventMessageList);
        assertEquals(DummyPhysicalAdapter.TARGET_GENERATED_MESSAGES, receivedPhysicalTelemetryEventMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

    @Test
    public void testPhysicalAdapterActions() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException {

        this.receivedPhysicalSwitchEventMessageList = new ArrayList<>();

        //Our target is to received two event changes associated to switch changes
        lock = new CountDownLatch(2);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Create Physical Adapter disabling the telemetry since we would like only to test actions and the associated swith events generation
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), false);

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(new ShadowingModelFunction("test-shadowing-function") {

            @Override
            protected void onCreate() {
                logger.debug("ShadowingModelFunction - onCreate()");
            }

            @Override
            protected void onStart() {
                logger.debug("ShadowingModelFunction - onStart()");
            }

            @Override
            protected void onStop() {
                logger.debug("ShadowingModelFunction - onStop()");
            }

            @Override
            protected void onPhysicalAdapterBound(String adapterId, PhysicalAssetState adapterPhysicalAssetState) {

            }

            @Override
            protected void onPhysicalEvent(PhysicalEventMessage<?> physicalEventMessage) {
                logger.info("onPhysicalEvent()-> {}", physicalEventMessage);

                if(physicalEventMessage != null
                        && PhysicalEventMessage.buildEventType(DummyPhysicalAdapter.SWITCH_PROPERTY_KEY).equals(physicalEventMessage.getType())
                        && physicalEventMessage.getBody() instanceof String){
                    logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalEventMessage.getType(), physicalEventMessage);
                    lock.countDown();
                    receivedPhysicalSwitchEventMessageList.add((PhysicalEventMessage<String>) physicalEventMessage);
                }
                else
                logger.error("WRONG Physical Event Message Received !");
            }

        }, buildWldtConfiguration());

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
        lock.await(5000, TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalSwitchEventMessageList);
        assertEquals(2, receivedPhysicalSwitchEventMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

}
