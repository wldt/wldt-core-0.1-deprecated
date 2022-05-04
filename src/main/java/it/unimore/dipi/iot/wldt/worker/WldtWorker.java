package it.unimore.dipi.iot.wldt.worker;

import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public abstract class WldtWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WldtWorker.class);

    public WldtWorker(){
    }

    @Override
    public void run() {
        try {
            onStart();
            executeWorkerJob();
        } catch (Exception e) {
            logger.error("WLDT WORKER ERROR: {}", e.getLocalizedMessage());
            onStop();
        }
    }

    abstract public void onStart();

    abstract public void onStop();

    abstract public void executeWorkerJob() throws WldtRuntimeException;
}
