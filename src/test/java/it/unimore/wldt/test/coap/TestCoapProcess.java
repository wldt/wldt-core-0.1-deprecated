package it.unimore.wldt.test.coap;

import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.process.WldtCoapProcess;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipeline;
import it.unimore.dipi.iot.wldt.worker.coap.Coap2CoapConfiguration;
import it.unimore.dipi.iot.wldt.worker.coap.Coap2CoapWorker;
import it.unimore.dipi.iot.wldt.worker.dummy.DummyProcessingStep;
import it.unimore.dipi.iot.wldt.worker.dummy.WldtDummyWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/07/2020
 * Project: CoAP Digital Twin Example - White Label Digital Twin - Java Framework
 */
public class TestCoapProcess {

    private static final String TAG = "[WLDT-Process]";

    private static final Logger logger = LoggerFactory.getLogger(WldtCoapProcess.class);

    public static void main(String[] args)  {

        try{

            logger.info("{} Initializing WLDT-Engine ... ", TAG);

            //Manual creation of the WldtConfiguration
            WldtConfiguration wldtConfiguration = new WldtConfiguration();
            wldtConfiguration.setDeviceNameSpace("it.unimore.dipi.things");
            wldtConfiguration.setWldtBaseIdentifier("wldt");
            wldtConfiguration.setWldtStartupTimeSeconds(10);
            wldtConfiguration.setApplicationMetricsEnabled(false);

            Coap2CoapWorker coap2CoapWorker = new Coap2CoapWorker(getCoapProtocolConfiguration());

            //Set a Processing Pipeline
            coap2CoapWorker.addProcessingPipeline(Coap2CoapWorker.DEFAULT_PROCESSING_PIPELINE, new ProcessingPipeline(new TestCoapProcessingStep()));

            WldtEngine wldtEngine = new WldtEngine(wldtConfiguration);
            wldtEngine.addNewWorker(coap2CoapWorker);
            wldtEngine.startWorkers();

        }catch (Exception | WldtConfigurationException e){
            e.printStackTrace();
        }
    }

    /**
     * Example of the CoAP-to-CoAP Worker Configuration
     * @return
     */
    private static Coap2CoapConfiguration getCoapProtocolConfiguration(){

        Coap2CoapConfiguration coap2CoapConfiguration = new Coap2CoapConfiguration();
        coap2CoapConfiguration.setResourceDiscovery(true);
        coap2CoapConfiguration.setDeviceAddress("127.0.0.1");
        coap2CoapConfiguration.setDevicePort(5683);

        return coap2CoapConfiguration;
    }

}
