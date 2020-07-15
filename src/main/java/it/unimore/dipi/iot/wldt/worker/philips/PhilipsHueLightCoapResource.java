package it.unimore.dipi.iot.wldt.worker.philips;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.dipi.iot.wldt.utils.SenML;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class PhilipsHueLightCoapResource extends CoapResource {

    private static final String TAG = "[WLDT-PhilipsCoapResource]";

    private static final Logger logger = LoggerFactory.getLogger(PhilipsHueLightCoapResource.class);

    private final String bridgeUsername;

    private PhilipsHueLightCoapResourceDescriptor philipsHueLightCoapResourceDescriptor;

    private String bridgeIp = null;

    private ObjectMapper objectMapper = null;

    public PhilipsHueLightCoapResource(String bridgeIp, String bridgeUsername, PhilipsHueLightCoapResourceDescriptor philipsHueLightCoapResourceDescriptor) throws PhilipsHueWorkerException {

        super(UUID.randomUUID().toString());

        if(philipsHueLightCoapResourceDescriptor == null)
            throw new PhilipsHueWorkerException("WldtCoapResourceDescriptor = null !");

        this.philipsHueLightCoapResourceDescriptor = philipsHueLightCoapResourceDescriptor;

        this.bridgeIp = bridgeIp;

        this.bridgeUsername = bridgeUsername;

        if(philipsHueLightCoapResourceDescriptor.getId() != null)
            setName(philipsHueLightCoapResourceDescriptor.getId());

        if(philipsHueLightCoapResourceDescriptor.getTitle() != null)
            getAttributes().setTitle(philipsHueLightCoapResourceDescriptor.getTitle());

        if(philipsHueLightCoapResourceDescriptor.getObservable()) {
            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs
            getAttributes().setObservable(); // mark observable in the Link-Format
        }

        if(philipsHueLightCoapResourceDescriptor.getResourceType() != null)
            getAttributes().addAttribute("rt",philipsHueLightCoapResourceDescriptor.getResourceType());

        if(philipsHueLightCoapResourceDescriptor.getCoreInterface() != null)
            getAttributes().addAttribute("if", philipsHueLightCoapResourceDescriptor.getCoreInterface());

        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        try{

            PhilipsHueLightDescriptor lightDescriptor = PhilipsHueBridgeConnector.getInstance().getBridgeLight(bridgeIp, bridgeUsername, philipsHueLightCoapResourceDescriptor.getId());

            if(lightDescriptor == null)
                exchange.respond(CoAP.ResponseCode.NOT_FOUND);
            else
                exchange.respond(CoAP.ResponseCode.CONTENT, getSenmlJsonResponse(lightDescriptor), MediaTypeRegistry.APPLICATION_JSON);

        }catch (Exception e){
            logger.error("Error handling Get ! Msg: {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }

    private String getSenmlJsonResponse(PhilipsHueLightDescriptor lightDescriptor) {

        try{

            SenML senmlData = new SenML();
            senmlData.setBaseName(String.format("%s:%s", lightDescriptor.getProductId(), lightDescriptor.getUniqueId()));
            senmlData.setBooleanValue(lightDescriptor.isOn());
            senmlData.setUpdateTime(System.currentTimeMillis());

            return  objectMapper.writeValueAsString(senmlData);

        }catch (Exception e){
            logger.error("Error generating SENML Response ! Msg: {}", e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public void handlePOST(CoapExchange exchange) {

        try{

            logger.debug("Received POST Request with body: {}", exchange.getRequestPayload());

            PhilipsHueLightDescriptor lightDescriptor = PhilipsHueBridgeConnector.getInstance().getBridgeLight(bridgeIp, bridgeUsername, philipsHueLightCoapResourceDescriptor.getId());
            boolean result = PhilipsHueBridgeConnector.getInstance().changeLightState(bridgeIp, bridgeUsername, philipsHueLightCoapResourceDescriptor.getId(), !lightDescriptor.isOn());

            if(result)
                exchange.respond(CoAP.ResponseCode.CHANGED);
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);

        }catch (Exception e){
            logger.error("Error handling POST ! Msg: {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        super.handlePUT(exchange);
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        super.handleDELETE(exchange);
    }

    @Override
    public void handleFETCH(CoapExchange exchange) {
        super.handleFETCH(exchange);
    }

    @Override
    public void handlePATCH(CoapExchange exchange) {
        super.handlePATCH(exchange);
    }

    @Override
    public void handleIPATCH(CoapExchange exchange) {
        super.handleIPATCH(exchange);
    }

}
