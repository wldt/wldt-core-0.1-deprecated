package it.unimore.dipi.iot.wldt.process;

import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.worker.philips.PhilipsHueLightWorkerConfiguration;
import it.unimore.dipi.iot.wldt.worker.philips.PhilipsHueLightWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin - Java Framework
 */
public class WldtPhilipsHueLightProcess {

    private static final String TAG = "[WLDT-WldtPhilipsHueLightProcess]";

    private static final Logger logger = LoggerFactory.getLogger(WldtPhilipsHueLightProcess.class);

    public static void main(String[] args)  {

        try{

            logger.info("{} Initializing WLDT-Engine ... ", TAG);

            //Manual creation of the WldtConfiguration
            WldtConfiguration wldtConfiguration = new WldtConfiguration();
            wldtConfiguration.setDeviceNameSpace("it.unimore.dipi.things");
            wldtConfiguration.setWldtBaseIdentifier("wldt");
            wldtConfiguration.setWldtStartupTimeSeconds(10);
            wldtConfiguration.setApplicationMetricsEnabled(false);

            PhilipsHueLightWorkerConfiguration philipsHueLightWorkerConfiguration = new PhilipsHueLightWorkerConfiguration();
            philipsHueLightWorkerConfiguration.setBridgeAddress("192.168.1.230");
            philipsHueLightWorkerConfiguration.setUsername("hf0Y561H6gp54UhhahAu0xN6MnYGPRp5ujq6FdGM");

            WldtEngine wldtEngine = new WldtEngine(wldtConfiguration);
            wldtEngine.addNewWorker(new PhilipsHueLightWorker(wldtEngine.getWldtId(), philipsHueLightWorkerConfiguration));
            wldtEngine.startWorkers();

        }catch (Exception | WldtConfigurationException e){
            e.printStackTrace();
        }
    }

}
