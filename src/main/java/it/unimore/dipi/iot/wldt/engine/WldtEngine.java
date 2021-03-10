package it.unimore.dipi.iot.wldt.engine;

import it.unimore.dipi.iot.wldt.metrics.MetricsReporterIdentifier;
import it.unimore.dipi.iot.wldt.metrics.WldtMetricsManager;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.worker.WorkerIdentifier;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import it.unimore.dipi.iot.wldt.worker.coap.Coap2CoapWorker;
import it.unimore.dipi.iot.wldt.worker.mqtt.Mqtt2MqttWorker;
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

    private List<WldtWorker<?,?,?>> workerList = null;

    private WldtConfiguration wldtConfiguration;

    public WldtEngine(WldtConfiguration wldtConfiguration) throws WldtConfigurationException {
        this.wldtConfiguration = wldtConfiguration;
        init();
    }

    public WldtEngine() throws WldtConfigurationException {
        this.wldtConfiguration = (WldtConfiguration) WldtUtils.readConfigurationFile(WLDT_CONFIGURATION_FOLDER, WLDT_CONFIGURATION_FILE, WldtConfiguration.class);
        logger.info("{} WLDT Configuration Loaded ! Conf: {}", TAG, wldtConfiguration);
        init();
    }

    private void init() throws WldtConfigurationException {

        this.wldtId = WldtUtils.generateRandomWldtId(this.wldtConfiguration.getDeviceNameSpace(), this.wldtConfiguration.getWldtBaseIdentifier());

        this.workerList = new ArrayList<>();

        executor = Executors.newFixedThreadPool((wldtConfiguration.getThreadPoolSize() > 0) ? wldtConfiguration.getThreadPoolSize() : THREAD_POOL_SIZE);

        if(this.wldtConfiguration.getApplicationMetricsEnabled() && this.wldtConfiguration.getMetricsReporterList() != null && this.wldtConfiguration.getMetricsReporterList().size() > 0) {

            enableMetricsReporter(this.wldtConfiguration.getMetricsReporterList());
            WldtMetricsManager.getInstance().startMonitoring(this.wldtConfiguration.getApplicationMetricsReportingPeriodSeconds());
        }

        loadConfigurationFileProtocolModules();
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
            logger.debug("{} New Worker Added to the List ! List Size: {}", TAG, this.workerList.size());
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

        this.workerList.forEach(wldtWorker -> {
            logger.info("Executing worker: {}", wldtWorker.getClass());
            executor.execute(wldtWorker);
        });

        executor.shutdown();

        while (!executor.isTerminated()) {}

    }

    //TODO Improve Stop Management for Executor Service
    public void stopWorkers(){
        this.executor.shutdownNow();
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void loadConfigurationFileProtocolModules() throws WldtConfigurationException {

        if(this.wldtConfiguration.getActiveProtocolList() == null || this.wldtConfiguration.getActiveProtocolList().size() == 0)
            logger.debug("{} Provided Protocol Modules List is EMPTY !", TAG);
        else {
            for(String protocolId: this.wldtConfiguration.getActiveProtocolList()){
                if(protocolId.equals(WorkerIdentifier.MQTT_TO_MQTT_MODULE.value))
                    this.addNewWorker(new Mqtt2MqttWorker(this.getWldtId()));
                if(protocolId.equals(WorkerIdentifier.COAP_TO_COAP_MODULE.value))
                    this.addNewWorker(new Coap2CoapWorker());
            }
        }
    }

    /*
    private void readConfigurationFile() throws WldtConfigurationException {
        try{

            //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            //File file = new File(classLoader.getResource(WLDT_CONFIGURATION_FILE).getFile());
            File file = new File(String.format("%s/%s", WLDT_CONFIGURATION_FOLDER, WLDT_CONFIGURATION_FILE));

            ObjectMapper om = new ObjectMapper(new YAMLFactory());

            wldtConfiguration = om.readValue(file, WldtConfiguration.class);

            logger.info("{} WLDT Configuration Loaded ! Conf: {}", TAG, wldtConfiguration);

        }catch (Exception e){
            e.printStackTrace();
            String errorMessage = String.format("ERROR LOADING CONFIGURATION FILE ! Error: %s", e.getLocalizedMessage());
            logger.error("{} {}", TAG, errorMessage);
            throw new WldtConfigurationException(errorMessage);
        }
    }
    */

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

}
