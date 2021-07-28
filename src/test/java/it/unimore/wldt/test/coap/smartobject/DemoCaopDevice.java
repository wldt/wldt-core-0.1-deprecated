package it.unimore.wldt.test.coap.smartobject;

import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoCaopDevice extends CoapServer {

    private final static Logger logger = LoggerFactory.getLogger(DemoCaopDevice.class);

    private String deviceId;

    public DemoCaopDevice(String deviceId){

        this.deviceId = String.format("dipi:iot:%s", deviceId);

        //Create Resources
        AlarmActuator alarmActuator = new AlarmActuator(deviceId,"alarm0001");

        logger.info("Defining and adding resources ...");

        //Add resources to the sd
        this.add(alarmActuator);

    }

    public static void main (String args[]){

        DemoCaopDevice demoCaopDevice = new DemoCaopDevice("demo-coap-device");
        logger.info("Starting Coap Server...");
        demoCaopDevice.start();
        logger.info("Coap Server Started ! Available resources: ");
        demoCaopDevice.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
        });

    }

}
