package it.unimore.wldt.test.coap.smartobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.dipi.iot.wldt.utils.CoreInterfaces;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmActuator extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(AlarmActuator.class);
    private static final Number SENSOR_VERSION = 0.1;
    private static final String OBJECT_TITLE = "AlarmActuator";
    private static final String RESOURCE_TYPE = "iot:actuator:alarm";
    private String deviceId;

    private boolean alarmStatus;

    private ObjectMapper objectMapper;

    public AlarmActuator(String deviceId, String name) {

        super(name);
        this.deviceId = deviceId;
        this.alarmStatus = false;

        //Jackson Object Mapper + Ignore Null Fields in order to properly generate the SenML Payload
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        setObservable(true); // enable observing
        setObserveType(CoAP.Type.CON); // configure the notification type to CONs

        getAttributes().setTitle(OBJECT_TITLE);
        getAttributes().setObservable(); // mark observable in the Link-Format

        //Specify Resource Attributes
        getAttributes().addAttribute("rt", RESOURCE_TYPE);
        getAttributes().addAttribute("if", CoreInterfaces.CORE_A.getValue());

    }

    private Optional<String> getJsonSenmlResponse(){

        try{

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBaseName(String.format("%s:%s", this.deviceId, this.getName()));
            senMLRecord.setVersion(SENSOR_VERSION);
            senMLRecord.setBooleanValue(isAlarmStatus());
            senMLRecord.setTime(System.currentTimeMillis());

            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        }catch (Exception e){
            logger.error("Error Generating SenML Record ! Msg: {}", e.getLocalizedMessage());
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
                exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON){

            Optional<String> senmlPayload = getJsonSenmlResponse();

            if(senmlPayload.isPresent())
                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
        //Otherwise respond with the default textplain payload
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(this.alarmStatus), MediaTypeRegistry.TEXT_PLAIN);


    }

    @Override
    public void handlePOST(CoapExchange exchange) {

        //According to CoRE Interface a POST request has an empty body and change the current status
        try{

            logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(exchange.advanced().getRequest()));
            logger.info("Received POST Request with body: {}", exchange.getRequestPayload());

            //Empty request
            if(exchange.getRequestPayload() == null){

                //Update internal status
                this.alarmStatus = (alarmStatus == true) ? false : true;

                logger.info("Resource Status Updated: {}", this.alarmStatus);

                exchange.respond(CoAP.ResponseCode.CHANGED);

                //Notify Observers
                changed();
            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch (Exception e){
            logger.error("Error Handling POST -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        super.handleDELETE(exchange);
    }

    public boolean isAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(boolean alarmStatus) {
        this.alarmStatus = alarmStatus;
    }
}
