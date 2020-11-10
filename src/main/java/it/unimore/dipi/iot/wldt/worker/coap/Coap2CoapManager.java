package it.unimore.dipi.iot.wldt.worker.coap;

import com.codahale.metrics.Timer;
import it.unimore.dipi.iot.wldt.metrics.WldtMetricsManager;
import it.unimore.dipi.iot.wldt.exception.WldtCoapManagerMissingDeviceInfoException;
import it.unimore.dipi.iot.wldt.exception.WldtCoapModuleException;
import it.unimore.dipi.iot.wldt.exception.WldtCoapResourceDiscoveryException;
import it.unimore.dipi.iot.wldt.exception.WldtCoapResourceException;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.*;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class Coap2CoapManager {

    private static final String CALIFORNIUM_CONF_FILE = "conf/Californium.properties";

    private static final String WELL_KNOWN_RESOURCE_ID = ".well-known/core";

    private static final Logger logger = LoggerFactory.getLogger(Coap2CoapManager.class);

    private static final String TAG = "[WLDT-CoAPManager]";

    private boolean coapCacheEnabled;

    private String deviceAddress;

    private int devicePort;

    private List<WldtCoapResourceDescriptor> coapResourceList;

    private CoapServer coapServer = null;

    private Random random = null;

    private Coap2CoapManager() {
        this.random = new Random();
    }

    private Coap2CoapWorker coap2CoapWorker;

    public Coap2CoapManager(String deviceAddress, int devicePort, boolean coapCacheEnabled, Coap2CoapWorker coap2CoapWorker) {

        this();

        this.coap2CoapWorker = coap2CoapWorker;
        this.deviceAddress = deviceAddress;
        this.devicePort = devicePort;
        this.coapCacheEnabled = coapCacheEnabled;

        initCoapConfiguration();

        logger.debug("{} Manager created for device {}:{} ... ", TAG, this.deviceAddress, this.devicePort);
    }

    public void resetCoapModule(){

        logger.debug("{} Cleaning up WLDT CoAP Module ....", TAG);

        if(this.coapServer != null) {
            this.coapServer.destroy();
            this.coapServer = null;
            logger.debug("{} CoAP Server destroyed", TAG);
        }

        if(this.coapResourceList != null) {
            this.coapResourceList.clear();
            this.coapResourceList = null;
            logger.debug("{} CoAP Resource List cleaned", TAG);
        }
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

    public void activateCoapModule() throws WldtCoapModuleException {

        if(this.coapResourceList == null || this.coapResourceList.size() == 0)
            throw new WldtCoapModuleException("Impossible to start CoAP Module ! CoAP Resource List is Empty or Null, run a discovery or check device info");

        if(this.coapServer != null)
            throw new WldtCoapModuleException("CoAP Module already active, reset before discovery and the new activation !");

        Timer.Context coapServerContext = WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_SERVER_SETUP_TIME);
        try{
            this.coapServer = new CoapServer();
        }finally {
            if(coapServerContext != null)
                coapServerContext.stop();
        }

        Timer.Context coapResourceListContext = WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_WLDT_RESOURCE_LIST_CREATION_TIME);
        try{

            this.coapResourceList.forEach(wldtCoapResourceDescriptor -> {
                Timer.Context coapResourcesServerContext = WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_WLDT_RESOURCE_CREATION_TIME);
                try {
                    this.coapServer.add(new WldtCoapResource(wldtCoapResourceDescriptor, this.coapCacheEnabled, this));
                } catch (WldtCoapResourceException e) {
                    logger.error("{} Error adding resource with descriptor: {} Reason: {}", TAG, wldtCoapResourceDescriptor, e.getLocalizedMessage());
                }
                finally {
                    if(coapResourcesServerContext != null)
                        coapResourcesServerContext.stop();
                }
            });

        }finally {
            if(coapResourceListContext != null)
                coapResourceListContext.stop();
        }

        this.coapServer.start();
    }

    public List<WldtCoapResourceDescriptor> discoverCoapResources() throws WldtCoapManagerMissingDeviceInfoException, ConnectorException, IOException, WldtCoapResourceDiscoveryException {

        Timer.Context context = WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_RESOURCE_DISCOVERY_TIME);

        try{

            logger.debug("{} Starting CoAP Resource Discovery ... ", TAG);

            String discoveryEndpoint = getDeviceWellKnownCoreUrl();

            logger.info("Sending Discovery Request to: {}", discoveryEndpoint);

            CoapClient coapClient = new CoapClient(discoveryEndpoint);
            Request request = new Request(CoAP.Code.GET);
            request.setMID(random.nextInt((CoapUtils.COAP_MID_MAX_VALUE - CoapUtils.COAP_MID_MIN_VALUE) + 1) + CoapUtils.COAP_MID_MIN_VALUE);

            byte[] token = RandomStringUtils.random(CoapUtils.COAP_TOKEN_LENGTH,true, true).getBytes();
            request.setToken(token);

            //Synchronously send the GET message (blocking call)
            CoapResponse coapResp = null;

            coapResp = coapClient.advanced(request);

            ArrayList<WldtCoapResourceDescriptor> resultList = new ArrayList<>();

            if(coapResp != null){

                if(coapResp.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_LINK_FORMAT){

                    Set<WebLink> links = LinkFormat.parse(coapResp.getResponseText());
                    links.forEach(link -> {
                        try {
                            if(link != null && !link.getURI().contains(WELL_KNOWN_RESOURCE_ID)){
                                WldtCoapResourceDescriptor newCoapResource = new WldtCoapResourceDescriptor(link, deviceAddress, devicePort);
                                resultList.add(newCoapResource);
                                logger.debug("{} New Resource Discovered ({}) -> {}", TAG, link.getURI(), newCoapResource);
                            }
                        } catch (WldtCoapResourceDiscoveryException e) {
                            logger.error("{} Error creating CoAP Resource from Californium WebLink Object ! Error: {}", TAG, e.getLocalizedMessage());
                        }
                    });

                    this.coapResourceList = resultList;

                    logger.info("{} DISCOVERED RESOURCE LIST SIZE: {}", TAG, this.coapResourceList.size());

                    return this.coapResourceList;

                } else {
                    String errorMsg = String.format("%s not found in the response !", MediaTypeRegistry.APPLICATION_LINK_FORMAT);
                    logger.error("{} {}", TAG, errorMsg);
                    throw new WldtCoapResourceDiscoveryException(errorMsg);
                }
            }
            else{
                logger.error("{} CoAP Resource Discovery received Null Response from the Device !", TAG);
                throw new WldtCoapResourceDiscoveryException("Null response received from the device !");
            }

        } finally {
            if(context != null)
                context.stop();
        }
    }

    private String getDeviceWellKnownCoreUrl() throws WldtCoapManagerMissingDeviceInfoException {

        if(this.deviceAddress == null || this.devicePort == 0)
            throw new WldtCoapManagerMissingDeviceInfoException("Device Information are now available or incorrect !");

        return String.format("coap://%s:%d/.well-known/core", this.deviceAddress, this.devicePort);
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public int getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(int devicePort) {
        this.devicePort = devicePort;
    }

    public List<WldtCoapResourceDescriptor> getCoapResourceList() {
        return coapResourceList;
    }

    public void setCoapResourceList(List<WldtCoapResourceDescriptor> coapResourceList) {
        this.coapResourceList = coapResourceList;
    }

    public Coap2CoapWorker getCoap2CoapWorker() {
        return coap2CoapWorker;
    }

    public void setCoap2CoapWorker(Coap2CoapWorker coap2CoapWorker) {
        this.coap2CoapWorker = coap2CoapWorker;
    }

    public CoapServer getCoapServer() {
        return coapServer;
    }

    public void setCoapServer(CoapServer coapServer) {
        this.coapServer = coapServer;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WldtCoapManager{");
        sb.append("deviceAddress='").append(deviceAddress).append('\'');
        sb.append(", devicePort=").append(devicePort);
        sb.append('}');
        return sb.toString();
    }
}
