package it.unimore.dipi.iot.wldt.engine;

import it.unimore.dipi.iot.wldt.event.DefaultEventLogger;
import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.metrics.MetricsReporterIdentifier;
import it.unimore.dipi.iot.wldt.metrics.WldtMetricsManager;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.model.ModelEngine;
import it.unimore.dipi.iot.wldt.model.ShadowingModelFunction;
import it.unimore.dipi.iot.wldt.state.DefaultDigitalTwinState;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import it.unimore.dipi.iot.wldt.utils.WldtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtEngine {

    private static final Logger logger = LoggerFactory.getLogger(WldtEngine.class);

    private static final int THREAD_POOL_SIZE = 5;

    private static final String TAG = "[WLDT-Engine]";

    public static final String WLDT_CONFIGURATION_FOLDER = "conf";

    private static final String WLDT_CONFIGURATION_FILE = "wldt.yaml";

    private String wldtId;

    private ExecutorService executor = null;

    private List<WldtWorker> workerList = null;

    private WldtConfiguration wldtConfiguration;

    private IDigitalTwinState digitalTwinState = null;

    private ModelEngine modelEngine = null;

    public WldtEngine(ShadowingModelFunction shadowingModelFunction, WldtConfiguration wldtConfiguration) throws WldtConfigurationException, ModelException, EventBusException {
        this.wldtConfiguration = wldtConfiguration;
        init(shadowingModelFunction);
    }

    public WldtEngine(ShadowingModelFunction shadowingModelFunction) throws WldtConfigurationException, ModelException, EventBusException {
        this.wldtConfiguration = (WldtConfiguration) WldtUtils.readConfigurationFile(WLDT_CONFIGURATION_FOLDER, WLDT_CONFIGURATION_FILE, WldtConfiguration.class);
        logger.info("{} WLDT Configuration Loaded ! Conf: {}", TAG, wldtConfiguration);
        init(shadowingModelFunction);
    }

    private void init(ShadowingModelFunction shadowingModelFunction) throws WldtConfigurationException, ModelException, EventBusException {

        this.wldtId = WldtUtils.generateRandomWldtId(this.wldtConfiguration.getDeviceNameSpace(), this.wldtConfiguration.getWldtBaseIdentifier());

        //Initialize the Digital Twin State
        this.digitalTwinState = new DefaultDigitalTwinState();

        //Setup EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Create the WorkerList
        this.workerList = new ArrayList<>();

        //executor = Executors.newFixedThreadPool((wldtConfiguration.getThreadPoolSize() > 0) ? wldtConfiguration.getThreadPoolSize() : THREAD_POOL_SIZE);

        if(this.wldtConfiguration.getApplicationMetricsEnabled() && this.wldtConfiguration.getMetricsReporterList() != null && this.wldtConfiguration.getMetricsReporterList().size() > 0) {
            enableMetricsReporter(this.wldtConfiguration.getMetricsReporterList());
            WldtMetricsManager.getInstance().startMonitoring(this.wldtConfiguration.getApplicationMetricsReportingPeriodSeconds());
        }

        //Init Model Engine & Add to the List of Workers
        this.modelEngine = new ModelEngine(this.digitalTwinState, shadowingModelFunction);
        addNewWorker(this.modelEngine);
    }

    private void enableMetricsReporter(List<String> metricsReporterList) {

        if(metricsReporterList != null && metricsReporterList.size() > 0){

            metricsReporterList.forEach(reporterIdentifier -> {
                if(reporterIdentifier.equals(MetricsReporterIdentifier.METRICS_REPORTER_CSV.value))
                    WldtMetricsManager.getInstance().enableCsvReporter();
                if(reporterIdentifier.equals(MetricsReporterIdentifier.METRICS_REPORTER_GRAPHITE.value))
                    WldtMetricsManager.getInstance().enableGraphiteReporter(
                            this.wldtConfiguration.getGraphiteReporterAddress(),
                            this.wldtConfiguration.getGraphiteReporterPort(),
                            this.wldtConfiguration.getGraphitePrefix());
            });

        }

    }

    public void addNewWorker(WldtWorker wldtWorker) throws WldtConfigurationException {
        if(wldtWorker != null && this.workerList != null && this.workerList.size() < THREAD_POOL_SIZE) {
            this.workerList.add(wldtWorker);
            logger.debug("{} New Worker ({}) Added to the List ! List Size: {}", TAG, wldtWorker.getClass().getName(), this.workerList.size());
        }
        else
            throw new WldtConfigurationException("Invalid Worker/WorkerList or Worker List Limit Reached !");
    }

    public void clearWorkersList() throws WldtConfigurationException{
        if(this.workerList != null){
            this.workerList.clear();
        }
        else
            throw new WldtConfigurationException("Error Cleaning Workers ! Worker List is Null !");
    }

    public void startWorkers() throws WldtConfigurationException {

        if(this.workerList == null || this.workerList.isEmpty())
            throw new WldtConfigurationException("Empty enabled protocol list !");

        //Init the thread pool
        executor = Executors.newFixedThreadPool(workerList.size());

        this.workerList.forEach(wldtWorker -> {
            logger.info("Executing worker: {}", wldtWorker.getClass());
            executor.execute(wldtWorker);
        });

        executor.shutdown();

        while (!executor.isTerminated()) {}

    }

    public void stopWorkers(){

        this.executor.shutdownNow();
        this.executor = null;

        //Notify workers
        for(WldtWorker worker : this.workerList)
            worker.onStop();
    }

    public String getWldtId() {
        return wldtId;
    }

    public void setWldtId(String wldtId) {
        this.wldtId = wldtId;
    }

    public WldtConfiguration getWldtConfiguration() {
        return wldtConfiguration;
    }

    public void setWldtConfiguration(WldtConfiguration wldtConfiguration) {
        this.wldtConfiguration = wldtConfiguration;
    }

    public ModelEngine getModelEngine() {
        return modelEngine;
    }

}
