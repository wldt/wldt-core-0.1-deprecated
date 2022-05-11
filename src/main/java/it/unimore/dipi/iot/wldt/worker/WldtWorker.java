package it.unimore.dipi.iot.wldt.worker;

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
        try{
            onWorkerCreated();
        }catch (Exception e){
            logger.error("WLDT WORKER onCreated() ERROR: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void run() {
        try {
            onWorkerStart();
        } catch (Exception e) {
            logger.error("WLDT WORKER onWorkerStart ERROR: {}", e.getLocalizedMessage());
            try{
                onWorkerStop();
            }catch (Exception stopException){
                logger.error("WLDT WORKER ERROR onWorkerStop() ERROR: {}", stopException.getLocalizedMessage());
            }
        }
    }

    abstract public void onWorkerCreated() throws WldtRuntimeException;

    abstract public void onWorkerStop() throws WldtRuntimeException;

    abstract public void onWorkerStart() throws WldtRuntimeException;
}
