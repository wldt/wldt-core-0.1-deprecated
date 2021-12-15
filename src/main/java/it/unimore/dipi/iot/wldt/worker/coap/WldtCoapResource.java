package it.unimore.dipi.iot.wldt.worker.coap;

import com.codahale.metrics.Timer;
import it.unimore.dipi.iot.wldt.exception.ProcessingPipelineException;
import it.unimore.dipi.iot.wldt.metrics.WldtMetricsManager;
import it.unimore.dipi.iot.wldt.exception.WldtCoapResourceException;
import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipeline;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipelineListener;
import it.unimore.dipi.iot.wldt.worker.dummy.WldtDummyWorker;
import it.unimore.dipi.iot.wldt.worker.mqtt.MqttPipelineData;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtCoapResource extends CoapResource {

    private static final String TAG = "[WLDT-CoAP-Resource]";

    private static final Logger logger = LoggerFactory.getLogger(WldtCoapResource.class);

    private Random random;

    private final CoapClient coapClient;

    private WldtCoapResourceDescriptor wldtCoapResourceDescriptor;

    private Map<String, CoapObserveRelation> observingMap;

    private Map<String, CoapCachedData> responseCache;

    private boolean coapCacheEnabled = false;

    private Coap2CoapManager coap2CoapManager;

    public WldtCoapResource(WldtCoapResourceDescriptor wldtCoapResourceDescriptor, boolean coapCacheEnabled, Coap2CoapManager coap2CoapManager) throws WldtCoapResourceException {
        this(wldtCoapResourceDescriptor, coap2CoapManager);
        this.coapCacheEnabled = coapCacheEnabled;
    }

    public WldtCoapResource(WldtCoapResourceDescriptor wldtCoapResourceDescriptor, Coap2CoapManager coap2CoapManager) throws WldtCoapResourceException {

        super(UUID.randomUUID().toString());

        if(wldtCoapResourceDescriptor == null)
            throw new WldtCoapResourceException("WldtCoapResourceDescriptor = null !");

        this.wldtCoapResourceDescriptor = wldtCoapResourceDescriptor;
        String deviceEndpoint = generateDeviceCoapEndpoint();

        this.coap2CoapManager = coap2CoapManager;

        this.coapClient = new CoapClient(generateDeviceCoapEndpoint());

        this.random = new Random();

        observingMap = new HashMap<>();

        logger.debug("{} Resource Descriptor Configured ! Endpoint: {}", TAG, deviceEndpoint);

        if(wldtCoapResourceDescriptor.getId() != null)
            setName(wldtCoapResourceDescriptor.getId());

        if(wldtCoapResourceDescriptor.getTitle() != null)
            getAttributes().setTitle(wldtCoapResourceDescriptor.getTitle());

        if(wldtCoapResourceDescriptor.getObservable()) {
            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs
            getAttributes().setObservable(); // mark observable in the Link-Format
        }

        if(wldtCoapResourceDescriptor.getResourceType() != null)
            getAttributes().addAttribute("rt",wldtCoapResourceDescriptor.getResourceType());

        if(wldtCoapResourceDescriptor.getCoreInterface() != null)
            getAttributes().addAttribute("if", wldtCoapResourceDescriptor.getCoreInterface());

        initDataCache();
    }

    private void initDataCache() {
        this.responseCache = new HashMap<String, CoapCachedData>();
    }

    private String generateDeviceCoapEndpoint(){
        return String.format("coap://%s:%s%s",
                this.wldtCoapResourceDescriptor.getDeviceAddress(),
                this.wldtCoapResourceDescriptor.getDevicePort(),
                this.wldtCoapResourceDescriptor.getUri());
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        //super.handleGET(exchange);
        //logger.debug("{} Handle Get with request: \n{}", TAG, Utils.prettyPrint(exchange.advanced().getRequest()));

        //if(!observingMap.containsKey(exchange.advanced().getRequest().getToken().toString())
        //        && exchange.getRequestOptions().hasObserve()
        //        && exchange.getRequestOptions().getObserve() == 0)
        //    startObserving(exchange);
        //else


        //TODO Improve this block !
        if(exchange.getRequestCode().equals(CoAP.Code.GET) && this.coapCacheEnabled) {

            CoapCachedData cachedData = this.responseCache.get(exchange.advanced().getRequest().getURI());

            if (cachedData != null) {

                if(cachedData.isDataFresh()){
                    logger.debug("{} Using Cached Data for {}", TAG, exchange.advanced().getRequest().getURI());
                    sendResponseToExternalClient(cachedData.getCoapResponse(), exchange);
                }
                else {
                    logger.debug("{} Cleaning Old Cached Data for {}", TAG, exchange.advanced().getRequest().getURI());
                    this.responseCache.remove(exchange.advanced().getRequest().getURI());
                    proxyIncomingRequest(exchange);
                }
            }
            else
                proxyIncomingRequest(exchange);
        }
        else
            proxyIncomingRequest(exchange);
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        proxyIncomingRequest(exchange);
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        proxyIncomingRequest(exchange);
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        proxyIncomingRequest(exchange);
    }

    @Override
    public void handleFETCH(CoapExchange exchange) {
        proxyIncomingRequest(exchange);
    }

    @Override
    public void handlePATCH(CoapExchange exchange) {
        proxyIncomingRequest(exchange);
    }

    @Override
    public void handleIPATCH(CoapExchange exchange) {
        proxyIncomingRequest(exchange);
    }

    private void proxyIncomingRequest(CoapExchange exchange) {

        if(exchange == null){
            logger.error("INCOMING REQUEST EXCHANGE = NULL !");
            return;
        }

        if(exchange.advanced() != null && exchange.advanced().getRequest() != null && exchange.advanced().getRequest().getPayloadSize() > 0)
            WldtMetricsManager.getInstance().measureCoapIncomingPayloadSizeMetric(exchange.advanced().getRequest().getPayloadSize());

        Timer.Context context = getForwardMetricsTimerForRequest(exchange.getRequestCode());
        Timer.Context overallForwardContext = WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_OVERALL_FORWARD_TIME);

        try {

            //logger.info("{} {} Handling Received Request: {}", TAG, wldtCoapResourceDescriptor.getId(), exchange.advanced().getRequest().getCode());

            logger.debug("{} Handling Received Request: \n{}", TAG, Utils.prettyPrint(exchange.advanced().getRequest()));

            CoapResponse physicalTwinCoapResp = sendRequestToPhysicalTwin(exchange);

            //Save data in cache
            //TODO Check Null Pointers
            if(this.coapCacheEnabled && exchange.advanced().getRequest().getCode().equals(CoAP.Code.GET) && physicalTwinCoapResp.getOptions().getMaxAge() > 0)
                addResponseToCache(exchange.advanced().getRequest().getURI(), physicalTwinCoapResp);

            sendResponseToExternalClient(physicalTwinCoapResp, exchange);

        } catch (Exception e) {
            logger.error("Error handling incoming request: {} -> Exception: {}", exchange.getRequestCode(), e.getLocalizedMessage());
            e.printStackTrace();
        }
        finally {
            if(context != null)
                context.stop();

            if(overallForwardContext != null)
                overallForwardContext.stop();
        }
    }

    private void addResponseToCache(String uri, CoapResponse coapResponse){
        try{
            if(coapResponse != null){
                logger.debug("{} Adding Response to cache for URI: {}", TAG, uri);
                this.responseCache.put(uri, new CoapCachedData(System.currentTimeMillis(), uri, coapResponse));
            }
        }catch (Exception e){
            logger.error("{} Error Adding Response to Cache ! Error: {}", TAG, e.getLocalizedMessage());
        }
    }

    private void sendResponseToExternalClient(CoapResponse physicalTwinCoapResp, CoapExchange exchange){

        Timer.Context context = getResponseMetricsTimerForRequest(exchange.getRequestCode());
        Timer.Context overallForwardContext = WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_OVERALL_RESPONSE_TIME);

        try{

            if(physicalTwinCoapResp != null){

                physicalTwinCoapResp.advanced().setMID(exchange.advanced().getRequest().getMID());
                physicalTwinCoapResp.advanced().setToken(exchange.advanced().getRequest().getToken());

                Response clientResponse = physicalTwinCoapResp.advanced();

                if(this.coap2CoapManager.getCoap2CoapWorker().hasProcessingPipeline(Coap2CoapWorker.DEFAULT_PROCESSING_PIPELINE)){

                    String resourceUri = exchange.advanced().getRequest().getURI();
                    byte[] payload = clientResponse.getPayload();
                    int contentFormat = clientResponse.getOptions().getContentFormat();

                    logger.info("Executing Processing Pipeline for resource Uri: {} ...", resourceUri);

                    this.coap2CoapManager.getCoap2CoapWorker().executeProcessingPipeline(Coap2CoapWorker.DEFAULT_PROCESSING_PIPELINE, new CoapPipelineData(
                            resourceUri,
                            payload,
                            contentFormat,
                            wldtCoapResourceDescriptor), new ProcessingPipelineListener() {

                        @Override
                        public void onPipelineDone(Optional<PipelineData> result) {

                            if(result.isPresent() && result.get() instanceof CoapPipelineData){
                                try {

                                    CoapPipelineData resultPipelineData = (CoapPipelineData)result.get();
                                    clientResponse.setPayload(resultPipelineData.getPayload());
                                    clientResponse.setOptions(clientResponse.getOptions().setContentFormat(resultPipelineData.getContentFormat()));

                                    exchange.respond(clientResponse);

                                }catch (Exception e){
                                    e.printStackTrace();
                                    exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
                                }
                            }
                            else {
                                logger.warn("CoAP Processing Pipeline produced an empty result for target resource URI: {} !", resourceUri);
                                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
                            }
                        }

                        @Override
                        public void onPipelineError() {
                            logger.error("Error CoAP Processing Pipeline for URI: {} ! ", resourceUri);
                            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
                        }

                    });
                }
                else
                   exchange.respond(clientResponse);

                logger.debug("{} Response Forwarded to the CLIENT: \n{}", TAG, Utils.prettyPrint(clientResponse));
            }
            else {
                logger.error("Error Physical Twin CoAP Response = null !");
            }

        }catch (Exception e){
            e.printStackTrace();
            logger.error("Error sending response to the external client for incoming request: {} -> Exception: {}", exchange.getRequestCode(), e.getLocalizedMessage());
        }
        finally {
            if(context != null)
                context.stop();
            if(overallForwardContext != null)
                overallForwardContext.stop();
        }
    }

    private CoapResponse sendRequestToPhysicalTwin(CoapExchange exchange){

        try{

            Request request = new Request(exchange.getRequestCode());

            //Set Token and MID
            int mid = random.nextInt((CoapUtils.COAP_MID_MAX_VALUE - CoapUtils.COAP_MID_MIN_VALUE) + 1) + CoapUtils.COAP_MID_MIN_VALUE;
            request.setMID(mid);

            byte[] token = RandomStringUtils.random(CoapUtils.COAP_TOKEN_LENGTH,true, true).getBytes();
            request.setToken(token);

            //Set Option Set
            request.setOptions(exchange.getRequestOptions());

            if(exchange.advanced().getRequest().getPayload() != null && exchange.advanced().getRequest().getPayload().length > 0)
                request.setPayload(exchange.advanced().getRequest().getPayload());

            logger.debug("{} Sending Request to PHYSICAL-TWIN: \n{}", TAG, Utils.prettyPrint(request));

            CoapResponse physicalTwinCoapResp = null;

            physicalTwinCoapResp = coapClient.advanced(request);

            if(physicalTwinCoapResp != null){

                logger.debug("{} Response Received from PHYSICAL-TWIN: \n{}", TAG, Utils.prettyPrint(physicalTwinCoapResp.advanced()));

                if(physicalTwinCoapResp.getPayload() != null && physicalTwinCoapResp.getPayload().length > 0)
                    WldtMetricsManager.getInstance().measureCoapOutgoingPayloadSizeMetric(physicalTwinCoapResp.getPayload().length);
            }
            else
                logger.error("Null Response received from the Physical Object !");

            return physicalTwinCoapResp;

        }catch (Exception e){
            logger.error("Error Sending the Message to the Physical Object ! Incoming request: {} -> Exception: {}", exchange.getRequestCode(), e.getLocalizedMessage());
            e.printStackTrace();
            return null;
        }

    }

    private Timer.Context getForwardMetricsTimerForRequest(CoAP.Code requestCode) {

        if(requestCode.equals(CoAP.Code.GET))
           return WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_GET_FORWARD_TIME);

        if(requestCode.equals(CoAP.Code.POST))
            return WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_POST_FORWARD_TIME);

        if(requestCode.equals(CoAP.Code.PUT))
            return WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_PUT_FORWARD_TIME);

        if(requestCode.equals(CoAP.Code.DELETE))
            return WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_DELETE_FORWARD_TIME);

        return null;
    }

    private Timer.Context getResponseMetricsTimerForRequest(CoAP.Code requestCode) {

        if(requestCode.equals(CoAP.Code.GET))
            return WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_GET_RESPONSE_TIME);

        if(requestCode.equals(CoAP.Code.POST))
            return WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_POST_RESPONSE_TIME);

        if(requestCode.equals(CoAP.Code.PUT))
            return WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_PUT_RESPONSE_TIME);

        if(requestCode.equals(CoAP.Code.DELETE))
            return WldtMetricsManager.getInstance().getCoapModuleTimerContext(WldtMetricsManager.COAP_DELETE_RESPONSE_TIME);

        return null;
    }

    /*
    private void startObserving(final CoapExchange exchange) {

        try{

            logger.debug("{} OBSERVING Request Received: {}", TAG, exchange.advanced().getRequest());

            Request request = new Request(exchange.getRequestCode());

            //Set Token and MID
            request.setMID(exchange.advanced().getRequest().getMID());
            request.setToken(exchange.advanced().getRequest().getToken());
            //request.setMID(random.nextInt((CoapUtils.COAP_MID_MAX_VALUE - CoapUtils.COAP_MID_MIN_VALUE) + 1) + CoapUtils.COAP_MID_MIN_VALUE);
            //byte[] token = RandomStringUtils.random(CoapUtils.COAP_TOKEN_LENGTH,true, true).getBytes();
            //request.setToken(token);

            //Set Option Set
            request.setOptions(exchange.getRequestOptions());

            CoapObserveRelation relation = coapClient.observe(request, new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    logger.debug("{} OBSERVED Response: \n{}", TAG, Utils.prettyPrint(response));
                    exchange.respond(response.advanced());
                    changed();
                }

                public void onError() {
                    System.err.println("OBSERVING FAILED");
                }
            });

            observingMap.put(exchange.advanced().getRequest().getToken().toString(), relation);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    */

    public boolean getCoapCacheEnabled() {
        return coapCacheEnabled;
    }

    public void setCoapCacheEnabled(boolean coapCacheEnabled) {
        this.coapCacheEnabled = coapCacheEnabled;
    }
}
