package it.unimore.dipi.iot.wldt.engine;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAdapter;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAdapterListener;
import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
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

    private Map<String, PhysicalAssetDescription> physicalAdaptersPhysicalAssetDescriptionMap;

    private Map<String, Boolean> physicalAdapterBoundStatusMap = null;

    private WldtConfiguration wldtConfiguration;

    private IDigitalTwinState digitalTwinState = null;

    private ModelEngine modelEngine = null;

    private List<LifeCycleListener> lifeCycleListenerList = null;

    private Thread modelEngineThread = null;

    private ShadowingModelFunction shadowingModelFunction = null;

    public WldtEngine(ShadowingModelFunction shadowingModelFunction, WldtConfiguration wldtConfiguration) throws WldtConfigurationException, ModelException, EventBusException, WldtRuntimeException {
        this.wldtConfiguration = wldtConfiguration;
        init(shadowingModelFunction);
    }

    public WldtEngine(ShadowingModelFunction shadowingModelFunction) throws WldtConfigurationException, ModelException, EventBusException, WldtRuntimeException {
        this.wldtConfiguration = (WldtConfiguration) WldtUtils.readConfigurationFile(WLDT_CONFIGURATION_FOLDER, WLDT_CONFIGURATION_FILE, WldtConfiguration.class);
        logger.info("{} WLDT Configuration Loaded ! Conf: {}", TAG, wldtConfiguration);
        init(shadowingModelFunction);
    }

    private void init(ShadowingModelFunction shadowingModelFunction) throws WldtConfigurationException, ModelException, EventBusException, WldtRuntimeException {

        this.wldtId = WldtUtils.generateRandomWldtId(this.wldtConfiguration.getDeviceNameSpace(), this.wldtConfiguration.getWldtBaseIdentifier());

        if(shadowingModelFunction == null)
            throw new WldtRuntimeException("Error ! Shadowing Function = NULL !");

        //Init Life Cycle Listeners & Status Map
        this.lifeCycleListenerList = new ArrayList<>();
        this.physicalAdapterBoundStatusMap = new HashMap<>();

        //Initialize the Digital Twin State
        this.digitalTwinState = new DefaultDigitalTwinState();

        //Setup EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Create List of Physical Adapter
        this.physicalAdapterList = new ArrayList<>();

        //Create a Map to hold the last PhysicalAssetDescription for each active adapter
        this.physicalAdaptersPhysicalAssetDescriptionMap = new HashMap<>();

        //executor = Executors.newFixedThreadPool((wldtConfiguration.getThreadPoolSize() > 0) ? wldtConfiguration.getThreadPoolSize() : THREAD_POOL_SIZE);

        if(this.wldtConfiguration.getApplicationMetricsEnabled() && this.wldtConfiguration.getMetricsReporterList() != null && this.wldtConfiguration.getMetricsReporterList().size() > 0) {
            enableMetricsReporter(this.wldtConfiguration.getMetricsReporterList());
            WldtMetricsManager.getInstance().startMonitoring(this.wldtConfiguration.getApplicationMetricsReportingPeriodSeconds());
        }

        //Set ShadowingListener, Init Model Engine & Add to the List of Workers
        this.shadowingModelFunction = shadowingModelFunction;
        this.shadowingModelFunction.setShadowingModelListener(this);
        this.modelEngine = new ModelEngine(this.digitalTwinState, this.shadowingModelFunction);

        //Save the Model Engine as Digital Twin Life Cycle Listener
        addLifeCycleListener(this.modelEngine);

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
            listener.onDigitalTwinBound(this.physicalAdaptersPhysicalAssetDescriptionMap);
    }

    private void notifyLifeCycleOnUnBound(String errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalTwinUnBound(this.physicalAdaptersPhysicalAssetDescriptionMap, errorMessage);
    }

    private void notifyLifeCycleOnAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterBound(adapterId, physicalAssetDescription);
    }

    private void notifyLifeCycleOnAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterBindingUpdate(adapterId, physicalAssetDescription);
    }

    private void notifyLifeCycleOnAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterUnBound(adapterId, physicalAssetDescription, errorMessage);
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

            //Save BoundStatus to False. It will be changed through a call back by the adapter
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
            removeLifeCycleListener(this.modelEngine);

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
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {

        logger.info("PhysicalAdapter {} BOUND !", adapterId);

        //Store the information that the adapter is correctly bound
        this.physicalAdapterBoundStatusMap.put(adapterId, true);

        //Save the last Physical Asset Description from the adapter
        this.physicalAdaptersPhysicalAssetDescriptionMap.put(adapterId, physicalAssetDescription);

        notifyLifeCycleOnAdapterBound(adapterId, physicalAssetDescription);

        if(isDtBound()) {
            logger.info("Digital Twin BOUND !");
            notifyLifeCycleOnBound();
        }
    }

    @Override
    public void onPhysicalBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {

        //Update the last Physical Asset Description from the adapter
        this.physicalAdaptersPhysicalAssetDescriptionMap.put(adapterId, physicalAssetDescription);

        //Notify Biding Change
        notifyLifeCycleOnAdapterBindingUpdate(adapterId, physicalAssetDescription);
    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {

        //Set the current adapter to unbound
        logger.info("PhysicalAdapter {} UN-BOUND ! Reason: {}", adapterId, errorMessage);

        //Store the information that the adapter is UnBound
        this.physicalAdapterBoundStatusMap.put(adapterId, false);

        //Retrieve the last physical asset description of the associated physical asset
        PhysicalAssetDescription currentPhysicalAssetDescription = this.physicalAdaptersPhysicalAssetDescriptionMap.get(adapterId);

        //Notify the adapter unbound status
        notifyLifeCycleOnAdapterUnBound(adapterId, currentPhysicalAssetDescription, errorMessage);

        //Check if the DT is still bound
        if(!isDtBound()) {
            logger.info("Digital Twin UN-BOUND !");
            notifyLifeCycleOnUnBound(String.format("Adapter %s UnBound - Error ?: %b", adapterId, errorMessage));
        }
    }
}
