package it.unimore.dipi.iot.wldt.process;

import it.unimore.dipi.iot.wldt.cache.WldtCache;
import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipeline;
import it.unimore.dipi.iot.wldt.worker.dummy.DummyProcessingStep;
import it.unimore.dipi.iot.wldt.worker.dummy.DummyWorkerConfiguration;
import it.unimore.dipi.iot.wldt.worker.dummy.WldtDummyWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin - Java Framework
 */
public class WldtDummyProcess {

    private static final String TAG = "[WLDT-Process]";

    private static final Logger logger = LoggerFactory.getLogger(WldtDummyProcess.class);

    public static void main(String[] args)  {

        try{

            logger.info("{} Initializing WLDT-Engine ... ", TAG);

            //Example loading everything from the configuration file
            //WldtEngine wldtEngine = new WldtEngine();
            //wldtEngine.startWorkers();

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
            WldtEngine wldtEngine = new WldtEngine(wldtConfiguration);

            //Init Dummy Worker with Cache
            WldtDummyWorker wldtDummyWorker = new WldtDummyWorker(
                    wldtEngine.getWldtId(),
                    new DummyWorkerConfiguration(),
                    new WldtCache<>(5, TimeUnit.SECONDS));

            //Init without caching
            //WldtDummyWorker wldtDummyWorker = new WldtDummyWorker(wldtEngine.getWldtId(), new DummyWorkerConfiguration());

            //Set a Processing Pipeline
            wldtDummyWorker.addProcessingPipeline(WldtDummyWorker.DEFAULT_PROCESSING_PIPELINE, new ProcessingPipeline(new DummyProcessingStep()));

            //Init with Worker Cache
            wldtEngine.addNewWorker(wldtDummyWorker);


            wldtEngine.startWorkers();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
