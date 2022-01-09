package it.unimore.wldt.test.model;

import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.event.*;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.state.DigitalTwinStateProperty;
import org.junit.Test;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;


public class ModelTester {

    public static DigitalTwinStateProperty<String> testProperty1 = null;

    private WldtEngine wldtEngine = null;

    private void initWldtEngine() throws WldtConfigurationException {

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

        //Init the Engine
        wldtEngine = new WldtEngine(wldtConfiguration);
    }


    @Test
    public void createModelFunction() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException {

        initWldtEngine();

        //Add two Model Function. The first observe the DT stat and the second one creates a new property
        wldtEngine.getModelEngine().startModelFunction(new ObserverModelFunction("state-observer-model-function"), true, null);
        wldtEngine.getModelEngine().startModelFunction(new StateUpdateModelFunction("state-updated-model-function"), false, null);
    }
}
