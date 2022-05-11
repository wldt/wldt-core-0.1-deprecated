package it.unimore.wldt.test.lifecycle;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAdapter;
import it.unimore.dipi.iot.wldt.engine.LifeCycleListener;
import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.event.DefaultEventLogger;
import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.event.PhysicalActionEventMessage;
import it.unimore.dipi.iot.wldt.event.PhysicalEventMessage;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.ModelException;
import it.unimore.dipi.iot.wldt.exception.ModelFunctionException;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.model.ShadowingModelFunction;
import it.unimore.wldt.test.adapter.DummyPhysicalAdapter;
import it.unimore.wldt.test.adapter.DummyPhysicalAdapterConfiguration;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LifeCycleTester {

    private static final Logger logger = LoggerFactory.getLogger(LifeCycleTester.class);

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
    public void testLifeCycle() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException {

        this.receivedPhysicalTelemetryEventMessageList = new ArrayList<>();

        lock = new CountDownLatch(DummyPhysicalAdapter.TARGET_GENERATED_MESSAGES);

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Create Physical Adapter
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), true);

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(new ShadowingModelFunction("test-shadowing-function") {

            private boolean isShadowed = false;

            @Override
            protected void onStart() {
                logger.debug("ShadowingModelFunction - onStart()");
            }

            @Override
            protected void onStop() {
                logger.debug("ShadowingModelFunction - onStop()");
            }

            @Override
            protected void onPhysicalEvent(PhysicalEventMessage<?> physicalEventMessage) {

                //logger.info("onPhysicalEvent()-> {}", physicalEventMessage);

                if(physicalEventMessage != null
                        && getPhysicalEventRawTypeList().isPresent()
                        && getPhysicalEventsFilter().contains(physicalEventMessage.getType())
                        && physicalEventMessage.getBody() instanceof Double){

                    if(!isShadowed){
                        isShadowed = true;
                        if(getShadowingModelListener() != null)
                            getShadowingModelListener().onShadowingSync();
                        else
                            logger.error("ERROR ShadowingListener = NULL !");
                    }

                    lock.countDown();
                    receivedPhysicalTelemetryEventMessageList.add((PhysicalEventMessage<Double>) physicalEventMessage);
                }
                else
                    logger.error("WRONG Physical Event Message Received !");
            }

            @Override
            protected Optional<List<String>> getPhysicalEventRawTypeList() {
                //We use the same event type generated by the adopter Physical Adapter
                //TODO UPDATE !!!!! O_O
                return Optional.empty();
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
            public void onAdapterBound(String adapterId) {
                logger.debug("LifeCycleListener - onAdapterBound({})", adapterId);
            }

            @Override
            public void onAdapterUnBound(String adapterId, Optional<String> errorMessage) {
                logger.debug("LifeCycleListener - onAdapterUnBound({}) Reason: {}", adapterId, errorMessage);
            }

            @Override
            public void onBound() {
                logger.debug("LifeCycleListener - onBound()");
            }

            @Override
            public void onUnBound(Optional<String> errorMessage) {
                logger.debug("LifeCycleListener - onUnBound() Reason: {}", errorMessage);
            }

            @Override
            public void onSync() {
                logger.debug("LifeCycleListener - onSync()");
            }

            @Override
            public void onUnSync() {
                logger.debug("LifeCycleListener - onUnSync()");
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
                        + (DummyPhysicalAdapter.TARGET_GENERATED_MESSAGES*DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)),
                TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalTelemetryEventMessageList);
        assertEquals(DummyPhysicalAdapter.TARGET_GENERATED_MESSAGES, receivedPhysicalTelemetryEventMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

}
