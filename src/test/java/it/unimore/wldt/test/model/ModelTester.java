package it.unimore.wldt.test.model;

import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.event.DefaultEventLogger;
import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.event.PhysicalEventMessage;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.model.ShadowingModelFunction;
import it.unimore.wldt.test.adapter.DummyPhysicalAdapter;
import it.unimore.wldt.test.adapter.DummyPhysicalAdapterConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ModelTester {

    private static final Logger logger = LoggerFactory.getLogger(ModelTester.class);

    private CountDownLatch lock = new CountDownLatch(1);

    private PhysicalEventMessage<String> receivedMessage = null;

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
        wldtConfiguration.setMetricsReporterList(Arrays.asList("csv", "graphite"));
        wldtConfiguration.setGraphitePrefix("wldt");
        wldtConfiguration.setGraphiteReporterAddress("127.0.0.1");
        wldtConfiguration.setGraphiteReporterPort(2003);

        return wldtConfiguration;
    }


    @Test
    public void testShadowingFunction() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException {

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(new ShadowingModelFunction("test-shadowing-function") {

            @Override
            protected void onStart() {
                logger.info("ShadowingModelFunction - onStart()");
            }

            @Override
            protected void onStop() {
                logger.info("ShadowingModelFunction - onStop()");
            }

            @Override
            protected void onPhysicalEvent(PhysicalEventMessage<?> physicalEventMessage) {
                logger.info("onPhysicalEvent()-> {}", physicalEventMessage);
                lock.countDown();

                receivedMessage = (PhysicalEventMessage<String>) physicalEventMessage;
            }

            @Override
            protected Optional<List<String>> getPhysicalEventRawTypeList() {
                return Optional.of(new ArrayList<String>() {{
                    add(DEMO_MQTT_MESSAGE_TYPE);
                }});
            }
        }, buildWldtConfiguration());

        wldtEngine.addPhysicalAdapter(new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), false));
        wldtEngine.startLifeCycle();

        //Wait just to complete the correct startup of the DT instance
        Thread.sleep(2000);

        //Generate an emulated Physical Event
        PhysicalEventMessage<String> physicalEventMessage = new PhysicalEventMessage<>(DEMO_MQTT_MESSAGE_TYPE);
        physicalEventMessage.setBody(DEMO_MQTT_BODY);
        physicalEventMessage.setContentType("text");

        //Publish Message on the target Topic1
        EventBus.getInstance().publishEvent("demo-physical-adapter", physicalEventMessage);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(receivedMessage);
        assertEquals(physicalEventMessage, receivedMessage);
        assertEquals(DEMO_MQTT_BODY, receivedMessage.getBody());
        assertEquals(PhysicalEventMessage.buildEventType(DEMO_MQTT_MESSAGE_TYPE), receivedMessage.getType());

        wldtEngine.stopLifeCycle();
    }

    @Test
    public void testInternalModelFunction() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException {

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(new ShadowingModelFunction("test-shadowing-function") {

            @Override
            protected void onStart() {
                logger.info("onStart()");
            }

            @Override
            protected void onStop() {
                logger.info("onStop()");
            }

            @Override
            protected void onPhysicalEvent(PhysicalEventMessage<?> physicalEventMessage) {
                logger.info("onPhysicalEvent()-> {}", physicalEventMessage);
                lock.countDown();
            }

            @Override
            protected Optional<List<String>> getPhysicalEventRawTypeList() {
                return Optional.of(new ArrayList<String>() {{
                    add(DEMO_MQTT_MESSAGE_TYPE);
                }});
            }
        }, buildWldtConfiguration());

        //Add two Model Function. The first observe the DT status and the second one creates a new property
        wldtEngine.getModelEngine().addStateModelFunction(new ObserverStateModelFunction("state-observer-model-function"), true, null);
        wldtEngine.getModelEngine().addStateModelFunction(new StateUpdateStateModelFunction("state-updated-model-function"), false, null);
    }
}
