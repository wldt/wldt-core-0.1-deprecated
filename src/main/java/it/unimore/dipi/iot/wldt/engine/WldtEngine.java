package it.unimore.dipi.iot.wldt.engine;

import it.unimore.dipi.iot.wldt.adapter.*;
import it.unimore.dipi.iot.wldt.event.DefaultWldtEventLogger;
import it.unimore.dipi.iot.wldt.event.WldtEventBus;
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
public class WldtEngine implements ShadowingModelListener, PhysicalAdapterListener, DigitalAdapterListener {

    private static final Logger logger = LoggerFactory.getLogger(WldtEngine.class);

    private static final int PHYSICAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT = 5;

    private static final int DIGITAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT = 5;

    private static final String TAG = "[WLDT-Engine]";

    public static final String WLDT_CONFIGURATION_FOLDER = "conf";

    private static final String WLDT_CONFIGURATION_FILE = "wldt.yaml";

    private String wldtId;

    private ExecutorService physicalAdapterExecutor = null;

    private ExecutorService digitalAdapterExecutor = null;

    private List<PhysicalAdapter<?>> physicalAdapterList = null;

    private List<DigitalAdapter<?>> digitalAdapterList = null;

    private Map<String, PhysicalAssetDescription> physicalAdaptersPhysicalAssetDescriptionMap;

    /**
     * Data Structure to keep track of the binding status of Physical Adapters
     */
    private Map<String, Boolean> physicalAdaptersBoundStatusMap = null;

    /**
     * Data Structure to keep track of the binding status of Digital Adapters
     */
    private Map<String, Boolean> digitalAdaptersBoundStatusMap = null;

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
        this.physicalAdaptersBoundStatusMap = new HashMap<>();
        this.digitalAdaptersBoundStatusMap = new HashMap<>();

        //Initialize the Digital Twin State
        this.digitalTwinState = new DefaultDigitalTwinState();

        //Setup EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Create List of Physical and Digistal Adapters
        this.physicalAdapterList = new ArrayList<>();
        this.digitalAdapterList = new ArrayList<>();

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

    private void notifyLifeCycleOnPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterBound(adapterId, physicalAssetDescription);
    }

    private void notifyLifeCycleOnPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterBindingUpdate(adapterId, physicalAssetDescription);
    }

    private void notifyLifeCycleOnPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterUnBound(adapterId, physicalAssetDescription, errorMessage);
    }

    private void notifyLifeCycleOnDigitalAdapterBound(String adapterId){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalAdapterBound(adapterId);
    }

    private void notifyLifeCycleOnDigitalAdapterUnBound(String adapterId, String errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalAdapterUnBound(adapterId, errorMessage);
    }

    private void notifyLifeCycleOnSync(IDigitalTwinState digitalTwinState){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onSync(digitalTwinState);
    }

    private void notifyLifeCycleOnUnSync(IDigitalTwinState digitalTwinState){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onUnSync(digitalTwinState);
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

    /**
     * Add a new Physical Adapter to the WLDT Engine in order to be executed through a dedicated Thread.
     * The method validates the request checking if the adapter is already in the list and if there is enough
     * thread to handle it within the thread pool
     *
     * @param physicalAdapter
     * @throws WldtConfigurationException
     */
    public void addPhysicalAdapter(PhysicalAdapter<?> physicalAdapter) throws WldtConfigurationException {

        if(physicalAdapter != null
                && this.physicalAdapterList != null
                && !this.physicalAdapterList.contains(physicalAdapter)
                && this.physicalAdapterList.size() < PHYSICAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT) {
            physicalAdapter.setPhysicalAdapterListener(this);
            this.physicalAdapterList.add(physicalAdapter);

            //Save BoundStatus to False. It will be changed through a call back by the adapter
            this.physicalAdaptersBoundStatusMap.put(physicalAdapter.getId(), false);

            logger.debug("{} New PhysicalAdapter ({}) Added to the Worker List ! Physical Adapters - Worker List Size: {}", TAG, physicalAdapter.getClass().getName(), this.physicalAdapterList.size());
        }
        else
            throw new WldtConfigurationException("Invalid PhysicalAdapter, Already added or List Limit Reached !");
    }

    /**
     * Clear the list of configured Physical Adapters
     *
     * @throws WldtConfigurationException
     */
    public void clearPhysicalAdapterList() throws WldtConfigurationException{
        if(this.physicalAdapterList != null){
            this.physicalAdapterList.clear();
        }
        else
            throw new WldtConfigurationException("Error Cleaning Physical Adapters ! List is Null !");
    }

    /**
     * Add a new Digital Adapter to the WLDT Engine in order to be executed through a dedicated Thread.
     * The method validates the request checking if the adapter is already in the list and if there is enough
     * thread to handle it within the thread pool
     *
     * @param digitalAdapter
     * @throws WldtConfigurationException
     */
    public void addDigitalAdapter(DigitalAdapter<?> digitalAdapter) throws WldtConfigurationException {

        if(digitalAdapter != null
                && this.digitalAdapterList != null
                && !this.digitalAdapterList.contains(digitalAdapter)
                && this.digitalAdapterList.size() < DIGITAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT) {

            digitalAdapter.setDigitalAdapterListener(this);
            this.digitalAdapterList.add(digitalAdapter);

            //Save BoundStatus to False. It will be changed through a call back by the adapter
            this.digitalAdaptersBoundStatusMap.put(digitalAdapter.getId(), false);

            //Save the Model Engine as Digital Twin Life Cycle Listener
            addLifeCycleListener(digitalAdapter);

            logger.debug("{} New DigitalAdapter ({}) Added to the Worker List ! Digital Adapters - Worker List Size: {}", TAG, digitalAdapter.getClass().getName(), this.physicalAdapterList.size());
        }
        else
            throw new WldtConfigurationException("Invalid PhysicalAdapter, Already added or List Limit Reached !");
    }

    /**
     * Clear the list of configured Digital Adapters
     *
     * @throws WldtConfigurationException
     */
    public void clearDigitalAdapterList() throws WldtConfigurationException{
        if(this.digitalAdapterList != null){

            for(DigitalAdapter<?> digitalAdapter : this.digitalAdapterList)
                removeLifeCycleListener(digitalAdapter);

            this.digitalAdapterList.clear();
        }
        else
            throw new WldtConfigurationException("Error Cleaning Physical Adapters ! List is Null !");
    }

    public void startLifeCycle() throws WldtConfigurationException {

        //In order to start its LifeCycle the Digital Twin need at least one Physical and one Digital Adapter in order
        //to properly bridge the physical and the digital world
        //TODO Check -> Does it make sense to force to have at least one Digital Adapter in order to start the Life Cycle ?
        if(this.physicalAdapterList == null || this.physicalAdapterList.isEmpty() || this.digitalAdapterList == null || this.digitalAdapterList.isEmpty())
            throw new WldtConfigurationException("Empty PhysicalAdapter o DigitalAdapter List !");

        notifyLifeCycleOnCreate();

        //Init PhysicalAdapter Executor
        physicalAdapterExecutor = Executors.newFixedThreadPool(physicalAdapterList.size());

        this.physicalAdapterList.forEach(physicalAdapter -> {
            logger.info("Executing PhysicalAdapter: {}", physicalAdapter.getClass());
            physicalAdapterExecutor.execute(physicalAdapter);
        });

        //Init DigitalAdapter Executor
        digitalAdapterExecutor = Executors.newFixedThreadPool(digitalAdapterList.size());

        this.digitalAdapterList.forEach(digitalAdapter -> {
            logger.info("Executing DigitalAdapter: {}", digitalAdapter.getClass());
            digitalAdapterExecutor.execute(digitalAdapter);
        });

        //When all Physical and Digital Adapters have been started the DT moves to the Start State
        notifyLifeCycleOnStart();

        physicalAdapterExecutor.shutdown();

        while (!physicalAdapterExecutor.isTerminated() && ! digitalAdapterExecutor.isTerminated()) {}

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

            //Stop and Notify Digital Adapters
            this.digitalAdapterExecutor.shutdownNow();
            this.digitalAdapterExecutor = null;
            for(DigitalAdapter<?> digitalAdapter : this.digitalAdapterList)
                digitalAdapter.onWorkerStop();

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
        for(boolean status : this.physicalAdaptersBoundStatusMap.values())
            if(!status)
                return false;
        return true;
    }

    @Override
    public void onShadowingSync(IDigitalTwinState digitalTwinState) {
        notifyLifeCycleOnSync(digitalTwinState);
    }

    @Override
    public void onShadowingOutOfSync(IDigitalTwinState digitalTwinState) {
        notifyLifeCycleOnUnSync(digitalTwinState);
    }

    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {

        logger.info("PhysicalAdapter {} BOUND !", adapterId);

        //Store the information that the adapter is correctly bound
        this.physicalAdaptersBoundStatusMap.put(adapterId, true);

        //Save the last Physical Asset Description from the adapter
        this.physicalAdaptersPhysicalAssetDescriptionMap.put(adapterId, physicalAssetDescription);

        notifyLifeCycleOnPhysicalAdapterBound(adapterId, physicalAssetDescription);

        if(isDtBound()) {
            logger.info("Digital Twin BOUND !");
            notifyLifeCycleOnBound();
        }
    }

    @Override
    public void onPhysicalBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {

        logger.info("PhysicalAdapter {} Binding Update ! New PA-Descriptor: {}", adapterId, physicalAssetDescription);

        //Update the last Physical Asset Description from the adapter
        this.physicalAdaptersPhysicalAssetDescriptionMap.put(adapterId, physicalAssetDescription);

        //Notify Biding Change
        notifyLifeCycleOnPhysicalAdapterBindingUpdate(adapterId, physicalAssetDescription);
    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {

        //Set the current adapter to unbound
        logger.info("PhysicalAdapter {} UN-BOUND ! Error: {}", adapterId, errorMessage);

        //Store the information that the adapter is UnBound
        this.physicalAdaptersBoundStatusMap.put(adapterId, false);

        //Retrieve the last physical asset description of the associated physical asset
        PhysicalAssetDescription currentPhysicalAssetDescription = this.physicalAdaptersPhysicalAssetDescriptionMap.get(adapterId);

        //Notify the adapter unbound status
        notifyLifeCycleOnPhysicalAdapterUnBound(adapterId, currentPhysicalAssetDescription, errorMessage);

        //Check if the DT is still bound
        if(!isDtBound()) {
            logger.info("Digital Twin UN-BOUND !");
            notifyLifeCycleOnUnBound(String.format("Adapter %s UnBound - Error ?: %b", adapterId, errorMessage));
        }
    }

    @Override
    public void onDigitalAdapterBound(String adapterId) {

        logger.info("DigitalAdapter {} BOUND !", adapterId);

        //Store the information that the adapter is correctly bound
        this.digitalAdaptersBoundStatusMap.put(adapterId, true);

        //Notify the adapter bound status
        notifyLifeCycleOnDigitalAdapterBound(adapterId);
    }

    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {

        //Set the current adapter to unbound
        logger.info("DigitalAdapter {} UN-BOUND ! Error: {}", adapterId, errorMessage);

        //Store the information that the adapter is UnBound
        this.digitalAdaptersBoundStatusMap.put(adapterId, false);

        //Notify the adapter unbound status
        notifyLifeCycleOnDigitalAdapterUnBound(adapterId, errorMessage);
    }
}
