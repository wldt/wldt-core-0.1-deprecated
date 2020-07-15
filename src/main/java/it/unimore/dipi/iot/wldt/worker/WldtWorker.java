package it.unimore.dipi.iot.wldt.worker;

import it.unimore.dipi.iot.wldt.cache.IWldtCache;
import it.unimore.dipi.iot.wldt.exception.ProcessingPipelineException;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import it.unimore.dipi.iot.wldt.processing.IProcessingPipeline;
import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipelineListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public abstract class WldtWorker<T, K, V> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WldtWorker.class);

    private T wldtWorkerConfiguration;

    protected IWldtCache<K,V> workerCache = null;

    private Map<String, IProcessingPipeline> processingPipelineMap = null;

    public WldtWorker(){
    }

    public WldtWorker(IWldtCache<K,V>  workerCache){
        this.workerCache = workerCache;
    }

    public WldtWorker(T wldtWorkerConfiguration, IWldtCache<K,V>  workerCache){
        this.wldtWorkerConfiguration = wldtWorkerConfiguration;
        this.workerCache = workerCache;
    }

    public WldtWorker(T wldtWorkerConfiguration){
        this.wldtWorkerConfiguration = wldtWorkerConfiguration;
    }

    @Override
    public void run() {

        try {
            initCache();
            startWorkerJob();
        } catch (WldtConfigurationException | WldtRuntimeException e) {
            logger.error("WLDT WORKER ERROR: {}", e.getLocalizedMessage());
        }
    }

    private void initCache() {
        try{

            if(this.workerCache != null) {
                this.workerCache.initCache();
                logger.info("Worker Cache properly initialized !");
            }
            else
                logger.warn("Worker Cache = null ! Cache init skipped");

        }catch (Exception e){
            logger.error("Error Initializing Worker Cache ! Error: {}", e.getLocalizedMessage());
        }
    }

    public T getWldtWorkerConfiguration() {
        return wldtWorkerConfiguration;
    }

    public void setWldtWorkerConfiguration(T wldtWorkerConfiguration) {
        this.wldtWorkerConfiguration = wldtWorkerConfiguration;
    }

    public boolean hasProcessingPipeline(String processingPipelineId) {
        if(this.processingPipelineMap != null)
            return this.processingPipelineMap.containsKey(processingPipelineId);
        else
            return false;
    }

    public void addProcessingPipeline(String processingPipelineId, IProcessingPipeline newProcessingPipeline) throws ProcessingPipelineException {

        try{

            if(this.processingPipelineMap == null)
                this.processingPipelineMap = new HashMap<>();

            this.processingPipelineMap.put(processingPipelineId, newProcessingPipeline);

        }catch (Exception e){
            logger.error("Error adding processing pipeline ({}) ! Error: {}", processingPipelineId, e.getLocalizedMessage());
            throw new ProcessingPipelineException(e.getLocalizedMessage());
        }
    }

    public void removeProcessingPipeline(String processingPipelineId, IProcessingPipeline newProcessingPipeline) throws ProcessingPipelineException {
        try{
            if(this.processingPipelineMap != null)
                this.processingPipelineMap.remove(processingPipelineId);
        }catch (Exception e){
            logger.error("Error removing processing pipeline ({}) ! Error: {}", processingPipelineId, e.getLocalizedMessage());
            throw new ProcessingPipelineException(e.getLocalizedMessage());
        }
    }

    public void executeProcessingPipeline(String processingPipelineId, PipelineData initialData, ProcessingPipelineListener listener) throws ProcessingPipelineException {

        if(processingPipelineId != null
                && this.processingPipelineMap != null
                && this.processingPipelineMap.containsKey(processingPipelineId)
                && this.processingPipelineMap.get(processingPipelineId) != null){
            this.processingPipelineMap.get(processingPipelineId).start(initialData, listener);
        }
        else
            throw new ProcessingPipelineException("PipelineId or ProcessingPipeline = Null or Not Found !");
    }

    abstract public void startWorkerJob() throws WldtConfigurationException, WldtRuntimeException;

}
