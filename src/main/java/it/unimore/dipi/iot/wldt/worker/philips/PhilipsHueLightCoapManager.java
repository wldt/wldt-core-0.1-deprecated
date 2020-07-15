package it.unimore.dipi.iot.wldt.worker.philips;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.List;
import java.util.Random;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class PhilipsHueLightCoapManager {

    private static final String CALIFORNIUM_CONF_FILE = "conf/Californium.properties";

    private static final String WELL_KNOWN_RESOURCE_ID = ".well-known/core";

    private static final Logger logger = LoggerFactory.getLogger(PhilipsHueLightCoapManager.class);

    private static final String TAG = "[WLDT-PhilipsHueCoapManager]";

    private final String bridgeIp;

    private final String bridgeUsername;

    private List<PhilipsHueLightCoapResourceDescriptor> coapResourceList;

    private CoapServer coapServer = null;

    private Random random = null;

    public PhilipsHueLightCoapManager(String bridgeIp, String bridgeUsername, List<PhilipsHueLightCoapResourceDescriptor> coapResourceList) {
        this.random = new Random();
        this.coapResourceList = coapResourceList;
        this.bridgeIp = bridgeIp;
        this.bridgeUsername = bridgeUsername;
        initCoapConfiguration();
    }

    private void initCoapConfiguration(){
        try{
            NetworkConfig config = NetworkConfig.createStandardWithFile(new File(CALIFORNIUM_CONF_FILE));
            NetworkConfig.setStandard(config);
            logger.info("Californium Configuration Correctly Loaded ! ");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void activate() throws PhilipsHueWorkerException {

        try{

            if(this.coapResourceList == null || this.coapResourceList.size() == 0)
                throw new PhilipsHueWorkerException("Impossible to start CoAP Module ! CoAP Resource List is Empty or Null, run a discovery or check device info");

            if(this.coapServer != null)
                throw new PhilipsHueWorkerException("CoAP Module already active, reset before discovery and the new activation !");

            this.coapServer = new CoapServer();

            this.coapResourceList.forEach(wldtCoapResourceDescriptor -> {
                try {
                    this.coapServer.add(new PhilipsHueLightCoapResource(bridgeIp, bridgeUsername, wldtCoapResourceDescriptor));
                } catch (PhilipsHueWorkerException e) {
                    logger.error("{} Error adding resource with descriptor: {} Reason: {}", TAG, wldtCoapResourceDescriptor, e.getLocalizedMessage());
                }
            });

            this.coapServer.start();

        }catch (Exception e){
            throw new PhilipsHueWorkerException(e.getLocalizedMessage());
        }
    }

    public List<PhilipsHueLightCoapResourceDescriptor> getCoapResourceList() {
        return coapResourceList;
    }

    public void setCoapResourceList(List<PhilipsHueLightCoapResourceDescriptor> coapResourceList) {
        this.coapResourceList = coapResourceList;
    }

    public CoapServer getCoapServer() {
        return coapServer;
    }

    public void setCoapServer(CoapServer coapServer) {
        this.coapServer = coapServer;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PhilipsHueCoapManager{");
        sb.append("coapResourceList=").append(coapResourceList);
        sb.append('}');
        return sb.toString();
    }
}
