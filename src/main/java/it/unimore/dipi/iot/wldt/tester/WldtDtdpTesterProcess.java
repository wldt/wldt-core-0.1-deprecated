package it.unimore.dipi.iot.wldt.tester;

import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin - Java Framework
 */
public class WldtDtdpTesterProcess {

    private static final String TAG = "[WLDT-DTDP-Process]";

    private static final Logger logger = LoggerFactory.getLogger(WldtDtdpTesterProcess.class);

    private static final int DTDP_TEST_RUN = 1000;

    public static void main(String[] args)  {

        try{

            logger.info("{} Initializing WLDT-Engine ... ", TAG);

            WldtEngine wldtEngine = new WldtEngine();

            logger.info("{} WLDT Configuration Loaded and Engine Ready", TAG);

            if(wldtEngine.getWldtConfiguration().getWldtStartupTimeSeconds() > 0){
                logger.info("{} Waiting {} seconds before starting ... ", TAG, wldtEngine.getWldtConfiguration().getWldtStartupTimeSeconds());
                Thread.sleep(wldtEngine.getWldtConfiguration().getWldtStartupTimeSeconds() * 1000);
            }

            /*
            for(int i=0; i<DTDP_TEST_RUN; i++){

                logger.info("{} Starting DTDP Discovery {} ... ", TAG, i);

                WldtDtdpDiscoveryModule wldtDtdpDiscoveryModule = new WldtDtdpDiscoveryModule(
                        wldtEngine.getWldtConfiguration().getDtdpServerAddress(),
                        wldtEngine.getWldtConfiguration().getDtdpServerPort());

                wldtDtdpDiscoveryModule.startDtdpDiscovery(new DtdpProtocolDiscoveryListener() {

                    @Override
                    public void onDtdpProtocolDiscovered(DTDPProtocol dtdpProtocol) {

                        logger.info("{} New DTDP Protocol Discovered ! Protocol Name: {} ", TAG, dtdpProtocol.getName());
                        logger.info("{} New DTDP Protocol Info: {} ", TAG, dtdpProtocol);

                        //Add MQTT Worker to WLDT
                        if(dtdpProtocol.getName().equals(DTDPMqttProtocol.DTDP_PROTOCOL_MQTT)){
                            try {
                                wldtEngine.addNewWorker(new WldtMqttWorker(wldtEngine.getWldtId(),
                                        new DTDPMqttProtocol(dtdpProtocol),
                                        wldtEngine.getWldtConfiguration()));
                            } catch (WldtConfigurationException e) {
                                logger.error("{} Error WldtMqttWorker to the list ! Error: {}", TAG, e.getLocalizedMessage());
                            }
                        }

                        //Add COAP Worker to WLDT
                        if(dtdpProtocol.getName().equals(DTDPCoapProtocol.DTDP_PROTOCOL_COAP)){
                            try {
                                wldtEngine.addNewWorker(new WldtCoapWorker(wldtEngine.getSmartObjectAddress(),
                                        wldtEngine.getSmartObjectPort(),
                                        new DTDPCoapProtocol(dtdpProtocol),
                                        wldtEngine.getWldtConfiguration().getCoapCacheEnabled()));

                            } catch (WldtConfigurationException e) {
                                logger.error("{} Error WldtCoapWorker to the list ! Error: {}", TAG, e.getLocalizedMessage());
                            }
                        }
                    }

                    @Override
                    public void onDtdpDiscoveryCompleted() {
                        //wldtEngine.startWorkers();
                        try{
                            logger.info("{} DTDP Discovery Completed ! ", TAG);
                            wldtEngine.clearWorkersList();
                        }catch (WldtConfigurationException e){
                            e.printStackTrace();
                        }
                    }
                });

                logger.info("{} Sleeping for 1 Second ... ", TAG);
                Thread.sleep(1000);
            }
             */

        }catch (Exception | WldtConfigurationException e){
            e.printStackTrace();
        }
    }
}
