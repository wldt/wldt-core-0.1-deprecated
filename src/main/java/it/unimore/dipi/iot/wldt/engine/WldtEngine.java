package it.unimore.dipi.iot.wldt.engine;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAdapter;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAdapterListener;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetState;
import it.unimore.dipi.iot.wldt.event.DefaultEventLogger;
import it.unimore.dipi.iot.wldt.event.EventBus;
import it.unimore.dipi.iot.wldt.metrics.MetricsReporterIdentifier;
import it.unimore.dipi.iot.wldt.metrics.WldtMetricsManager;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.model.ModelEngine;
import it.unimore.dipi.iot.wldt.model.ShadowingModelFunction;
import it.unimore.dipi.iot.wldt.model.ShadowingModelListener;
import it.unimore.dipi.iot.wldt.state.DefaultDigitalTwinState;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
import it.unimore.dipi.iot.wldt.utils.WldtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtEngine implements ShadowingModelListener, PhysicalAdapterListener {

    private static final Logger logger = LoggerFactory.getLogger(WldtEngine.class);

    private static final int PHYSICAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT = 5;

    private static final String TAG = "[WLDT-Engine]";

    public static final String WLDT_CONFIGURATION_FOLDER = "conf";

    private static final String WLDT_CONFIGURATION_FILE = "wldt.yaml";

    private String wldtId;

    private ExecutorService physicalAdapterExecutor = null;

    private List<PhysicalAdapter<?>> physicalAdapterList = null;

    private Map<String, Boolean> physicalAdapterBoundStatusMap = null;

    private WldtConfiguration wldtConfiguration;

    private IDigitalTwinState digitalTwinState = null;

    private ModelEngine modelEngine = null;

    private List<LifeCycleListener> lifeCycleListenerList = null;

    private Thread modelEngineThread = null;

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

        //Init Life Cycle Listeners & Status Map
        this.lifeCycleListenerList = new ArrayList<>();
        this.physicalAdapterBoundStatusMap = new HashMap<>();

        //Initialize the Digital Twin State
        this.digitalTwinState = new DefaultDigitalTwinState();

        //Setup EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Create List of Physical Adapter
        this.physicalAdapterList = new ArrayList<>();

        //executor = Executors.newFixedThreadPool((wldtConfiguration.getThreadPoolSize() > 0) ? wldtConfiguration.getThreadPoolSize() : THREAD_POOL_SIZE);

        if(this.wldtConfiguration.getApplicationMetricsEnabled() && this.wldtConfiguration.getMetricsReporterList() != null && this.wldtConfiguration.getMetricsReporterList().size() > 0) {
            enableMetricsReporter(this.wldtConfiguration.getMetricsReporterList());
            WldtMetricsManager.getInstance().startMonitoring(this.wldtConfiguration.getApplicationMetricsReportingPeriodSeconds());
        }

        //Set ShadowingListener, Init Model Engine & Add to the List of Workers
        shadowingModelFunction.setShadowingModelListener(this);
        this.modelEngine = new ModelEngine(this.digitalTwinState, shadowingModelFunction);
        executeModelEngine();
    }

    private void executeModelEngine(){
        modelEngineThread = new Thread(this.modelEngine);
        modelEngineThread.start();
    }

    public void addLifeCycleListener(LifeCycleListener listener){
        if(listener != null && this.lifeCycleListenerList != null && !this.lifeCycleListenerList.contains(listener))
            this.lifeCycleListenerList.add(listener);
    }

    public void removeLifeCycleListener(LifeCycleListener listener){
        if(listener != null && this.lifeCycleListenerList != null)
            this.lifeCycleListenerList.remove(listener);
    }

    private void notifyLifeCycleOnCreate(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onCreate();
    }

    private void notifyLifeCycleOnStart(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onStart();
    }

    private void notifyLifeCycleOnBound(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onBound();
    }

    private void notifyLifeCycleOnUnBound(Optional<String> errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onUnBound(errorMessage);
    }

    private void notifyLifeCycleOnAdapterBound(String adapterId){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onAdapterBound(adapterId);
    }

    private void notifyLifeCycleOnAdapterUnBound(String adapterId, Optional<String> errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onAdapterUnBound(adapterId, errorMessage);
    }

    private void notifyLifeCycleOnSync(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onSync();
    }

    private void notifyLifeCycleOnUnSync(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onUnSync();
    }

    private void notifyLifeCycleOnStop(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onStop();
    }

    private void notifyLifeCycleOnDestroy(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDestroy();
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

    public void addPhysicalAdapter(PhysicalAdapter<?> physicalAdapter) throws WldtConfigurationException {

        if(physicalAdapter != null
                && this.physicalAdapterList != null
                && !this.physicalAdapterList.contains(physicalAdapter)
                && this.physicalAdapterList.size() < PHYSICAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT) {
            physicalAdapter.setPhysicalAdapterListener(this);
            this.physicalAdapterList.add(physicalAdapter);

            //Save BoundStatus to False. It will be changed through a call back by the adpter
            this.physicalAdapterBoundStatusMap.put(physicalAdapter.getId(), false);

            logger.debug("{} New PhysicalAdapter ({}) Added to the Worker List ! Worker List Size: {}", TAG, physicalAdapter.getClass().getName(), this.physicalAdapterList.size());
        }
        else
            throw new WldtConfigurationException("Invalid PhysicalAdapter, Already added or List Limit Reached !");
    }

    public void clearPhysicalAdapterList() throws WldtConfigurationException{
        if(this.physicalAdapterList != null){
            this.physicalAdapterList.clear();
        }
        else
            throw new WldtConfigurationException("Error Cleaning Physical Adapters ! List is Null !");
    }

    public void startLifeCycle() throws WldtConfigurationException {

        if(this.physicalAdapterList == null || this.physicalAdapterList.isEmpty())
            throw new WldtConfigurationException("Empty PhysicalAdapter List !");

        notifyLifeCycleOnCreate();

        //Init PhysicalAdapter Executor
        physicalAdapterExecutor = Executors.newFixedThreadPool(physicalAdapterList.size());

        this.physicalAdapterList.forEach(physicalAdapter -> {
            logger.info("Executing PhysicalAdapter: {}", physicalAdapter.getClass());
            physicalAdapterExecutor.execute(physicalAdapter);
        });

        //When all PhysicalAdapters have been started the DT moves to the Start State
        notifyLifeCycleOnStart();

        physicalAdapterExecutor.shutdown();

        while (!physicalAdapterExecutor.isTerminated()) {}

    }

    public void stopLifeCycle(){
        try{

            //Stop and Notify Model Engine
            this.modelEngineThread.interrupt();
            this.modelEngineThread = null;
            this.modelEngine.onWorkerStop();

            //Stop and Notify Physical Adapters
            this.physicalAdapterExecutor.shutdownNow();
            this.physicalAdapterExecutor = null;
            for(PhysicalAdapter<?> physicalAdapter : this.physicalAdapterList)
                physicalAdapter.onWorkerStop();

            notifyLifeCycleOnStop();
            notifyLifeCycleOnDestroy();

        } catch (Exception e){
            logger.error("ERROR Stopping DT LifeCycle ! Error: {}", e.getLocalizedMessage());
        }

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

    /**
     * Check if all the registered Physical Adapters are correctly bound
     * @return
     */
    private boolean isDtBound(){
        for(boolean status : this.physicalAdapterBoundStatusMap.values())
            if(!status)
                return false;
        return true;
    }

    @Override
    public void onShadowingSync() {
        notifyLifeCycleOnSync();
    }

    @Override
    public void onShadowingOutOfSync() {
        notifyLifeCycleOnUnSync();
    }

    @Override
    public void onBound(String adapterId, PhysicalAssetState physicalAssetState) {
        logger.info("PhysicalAdapter {} BOUND !", adapterId);
        this.physicalAdapterBoundStatusMap.put(adapterId, true);
        notifyLifeCycleOnAdapterBound(adapterId);

        if(isDtBound()) {
            logger.info("Digital Twin BOUND !");
            notifyLifeCycleOnBound();
        }
    }

    @Override
    public void onBindingUpdate(String adapterId, PhysicalAssetState physicalAssetState) {
        //TODO HANDLE !
    }

    @Override
    public void onUnBound(String adapterId,  PhysicalAssetState physicalAssetState, Optional<String> errorMessage) {

        //If the DT is currently bound
        if(isDtBound()) {
            logger.info("PhysicalAdapter {} UN-BOUND ! Reason: {}", adapterId, errorMessage);
            this.physicalAdapterBoundStatusMap.put(adapterId, false);
            notifyLifeCycleOnAdapterUnBound(adapterId, errorMessage);
            logger.info("Digital Twin UN-BOUND !");
            notifyLifeCycleOnUnBound(Optional.of(String.format("Adapter %s UnBound - Error ?: %b", adapterId, errorMessage.isPresent())));
        }
        else{
            logger.info("PhysicalAdapter {} UN-BOUND ! Reason: {}", adapterId, errorMessage);
            this.physicalAdapterBoundStatusMap.put(adapterId, false);
            notifyLifeCycleOnAdapterUnBound(adapterId, errorMessage);
        }
    }
}
