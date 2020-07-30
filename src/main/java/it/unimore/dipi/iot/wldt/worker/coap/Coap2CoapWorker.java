package it.unimore.dipi.iot.wldt.worker.coap;

import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.exception.WldtCoapManagerMissingDeviceInfoException;
import it.unimore.dipi.iot.wldt.exception.WldtCoapModuleException;
import it.unimore.dipi.iot.wldt.exception.WldtCoapResourceDiscoveryException;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import it.unimore.dipi.iot.wldt.utils.WldtUtils;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class Coap2CoapWorker extends WldtWorker<Coap2CoapConfiguration, String, CoapCachedData> {

    private static final Logger logger = LoggerFactory.getLogger(Coap2CoapWorker.class);

    private static final String TAG = "[WLDT-COAP-WORKER]";

    private static final String CONF_FILE_NAME = "coap.yaml";

    private Coap2CoapManager coap2CoapManager = null;

    public Coap2CoapWorker(Coap2CoapConfiguration coap2CoapConfiguration) {
        super(coap2CoapConfiguration);
    }

    public Coap2CoapWorker() throws WldtConfigurationException {
        super();
        this.setWldtWorkerConfiguration((Coap2CoapConfiguration) WldtUtils.readConfigurationFile(WldtEngine.WLDT_CONFIGURATION_FOLDER, CONF_FILE_NAME, Coap2CoapConfiguration.class));

        logger.info("CoAP Module Configuration Loaded {} {}", TAG, this.getWldtWorkerConfiguration());
    }

    @Override
    public void startWorkerJob() throws WldtConfigurationException {

        if(this.getWldtWorkerConfiguration() == null
            || this.getWldtWorkerConfiguration().getDeviceAddress() == null
            || (!this.getWldtWorkerConfiguration().getResourceDiscovery() && (this.getWldtWorkerConfiguration().getResourceList() == null || this.getWldtWorkerConfiguration().getResourceList().size() == 0))
        )
            throw new WldtConfigurationException("WldtCoapWorker -> Provided Configuration = NULL or with wrong or missing parameters !");

        startCoapProtocolManagement();
    }

    public void startCoapProtocolManagement(){

        logger.info("{} STARTING COAP PROTOCOL MANAGEMENT ...", TAG);

        //Check if the CoAP Manager is already active
        if(coap2CoapManager == null) {

            //Create the CoAP Manager
            coap2CoapManager = new Coap2CoapManager(this.getWldtWorkerConfiguration().getDeviceAddress(),this.getWldtWorkerConfiguration().getDevicePort(), this.getWldtWorkerConfiguration().getCacheEnabled());

            logger.debug("{} COAP MANAGER Initialized for device at: {}:{}", TAG, coap2CoapManager.getDeviceAddress(), coap2CoapManager.getDevicePort());

            //Start CoAP Resource Discovery
            try {

                coap2CoapManager.discoverCoapResources();
                coap2CoapManager.activateCoapModule();

                logger.info("{} COAP PROTOCOL MANAGEMENT [STARTED]", TAG);

            } catch (WldtCoapManagerMissingDeviceInfoException | ConnectorException | IOException | WldtCoapResourceDiscoveryException | WldtCoapModuleException e) {
                e.printStackTrace();
            }

        }else
            logger.error("CoAP Manager already associated to an existing CoAP Device ({}:{}) ! No Action !", this.coap2CoapManager.getDeviceAddress(), this.coap2CoapManager.getDevicePort());

    }
}
