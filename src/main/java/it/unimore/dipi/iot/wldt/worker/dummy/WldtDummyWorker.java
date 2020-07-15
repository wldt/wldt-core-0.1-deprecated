package it.unimore.dipi.iot.wldt.worker.dummy;

import com.codahale.metrics.Timer;
import it.unimore.dipi.iot.wldt.cache.IWldtCache;
import it.unimore.dipi.iot.wldt.metrics.WldtMetricsManager;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipelineListener;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Random;

public class WldtDummyWorker extends WldtWorker<DummyWorkerConfiguration, String, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(WldtDummyWorker.class);

    private static final String METRIC_BASE_IDENTIFIER = "dummy_worker";

    private static final String WORKER_EXECUTION_TIME_METRICS_FIELD = "execution_time";

    private static final String WORKER_VALUE_METRICS_FIELD = "execution_value";

    private static final String CACHE_VALUE_KEY = "physical_obj_value";

    public static final String DEFAULT_PROCESSING_PIPELINE = "default_processing_pipeline";

    private Random random = null;

    private String wldtId = null;

    private int RUN_COUNT_LIMIT = 10000;

    public WldtDummyWorker(String wldtId, DummyWorkerConfiguration dummyWorkerConfiguration) {
        super(dummyWorkerConfiguration);
        this.random = new Random();
        this.wldtId = wldtId;
    }

    public WldtDummyWorker(String wldtId, DummyWorkerConfiguration dummyWorkerConfiguration, IWldtCache<String, Integer> wldtCache) {
        super(dummyWorkerConfiguration, wldtCache);
        this.random = new Random();
        this.wldtId = wldtId;
    }

    @Override
    public void startWorkerJob() throws WldtConfigurationException, WldtRuntimeException {

        try{
            for(int i = 0; i < RUN_COUNT_LIMIT; i++)
                emulateExternalGetRequest(i);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void emulateExternalGetRequest(int roundIndex) {

        Timer.Context metricsContext = WldtMetricsManager.getInstance().getTimer(String.format("%s.%s", METRIC_BASE_IDENTIFIER, this.wldtId), WORKER_EXECUTION_TIME_METRICS_FIELD);

        try{

            logger.info("Round [{}]: Dummy Worker Incoming Get Request .... ", roundIndex);

            int physicalObjectValue = 0;

            //Handle Cache
            if(this.workerCache != null && this.workerCache.getData(CACHE_VALUE_KEY).isPresent()) {
                physicalObjectValue = this.workerCache.getData(CACHE_VALUE_KEY).get();
                logger.info("Round [{}]: Cached Physical Object Value: {} ", roundIndex, physicalObjectValue);
            }
            else{
                physicalObjectValue = retrieveValueFromPhysicalObject();
                logger.info("Round [{}]: Physical Object Value: {} ", roundIndex, physicalObjectValue);
                if(this.workerCache != null)
                    this.workerCache.putData(CACHE_VALUE_KEY, physicalObjectValue);
            }

            //Check Processing Pipeline
            if(this.hasProcessingPipeline(WldtDummyWorker.DEFAULT_PROCESSING_PIPELINE)) {
                this.executeProcessingPipeline(DEFAULT_PROCESSING_PIPELINE, new DummyPipelineData(physicalObjectValue), new ProcessingPipelineListener() {
                   @Override
                   public void onPipelineDone(Optional<PipelineData> result) {
                       if(result != null && result.isPresent())
                           logger.info("Processing Pipeline Executed ! Result: {}", ((DummyPipelineData)result.get()).getValue());
                       else
                           logger.error("Processing pipeline result = null !");
                   }

                   @Override
                   public void onPipelineError() {
                       logger.error("Processing pipeline Error !");
                   }
               });
            }

            WldtMetricsManager.getInstance().measureValue(String.format("%s.%s", METRIC_BASE_IDENTIFIER, this.wldtId), WORKER_VALUE_METRICS_FIELD, roundIndex);

            Thread.sleep(random.nextInt(3000) + 1000);

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(metricsContext != null)
                metricsContext.stop();
        }
    }

    private int retrieveValueFromPhysicalObject(){
        try{
            Thread.sleep(random.nextInt(1000) + 100);
            return random.nextInt(3000);
        }catch (Exception e){
            logger.error("Error getting random mockup value ! Error: {}", e.getLocalizedMessage());
            return 0;
        }
    }

}
