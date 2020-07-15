package it.unimore.dipi.iot.wldt.worker.philips;

import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import it.unimore.dipi.iot.wldt.utils.CoreInterfaces;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class PhilipsHueLightWorker extends WldtWorker<PhilipsHueLightWorkerConfiguration, String, String> {

    private static final Logger logger = LoggerFactory.getLogger(PhilipsHueLightWorker.class);

    private String wldtId = null;

    private PhilipsHueLightCoapManager philipsHueLightCoapManager = null;

    public PhilipsHueLightWorker(String wldtId, PhilipsHueLightWorkerConfiguration philipsHueLightWorkerConfiguration) {
        super(philipsHueLightWorkerConfiguration);
        this.wldtId = wldtId;
    }

    @Override
    public void startWorkerJob() throws WldtConfigurationException, WldtRuntimeException {

        try{

            if(this.getWldtWorkerConfiguration().getBridgeAddress() != null && this.getWldtWorkerConfiguration().getUsername() != null){

                String bridgeIp = this.getWldtWorkerConfiguration().getBridgeAddress();
                String username = this.getWldtWorkerConfiguration().getUsername();

                List<PhilipsHueLightDescriptor> lightList = PhilipsHueBridgeConnector.getInstance().getBridgeLightList(bridgeIp, username);
                ArrayList<PhilipsHueLightCoapResourceDescriptor> philipsHueLightCoapResourceDescriptorList = new ArrayList<>();

                for(PhilipsHueLightDescriptor philipsHueLightDescriptor : lightList){

                    logger.info("Adding a new Philips Hue Light CoAP Resource: {}", philipsHueLightDescriptor.getName());

                    PhilipsHueLightCoapResourceDescriptor coapResourceDescriptor = new PhilipsHueLightCoapResourceDescriptor();
                    coapResourceDescriptor.setCoreInterface(CoreInterfaces.CORE_A.getValue());
                    coapResourceDescriptor.setId(philipsHueLightDescriptor.getId());
                    coapResourceDescriptor.setObservable(false);
                    coapResourceDescriptor.setResourceType(philipsHueLightDescriptor.getType());
                    coapResourceDescriptor.setTitle(philipsHueLightDescriptor.getName());

                    philipsHueLightCoapResourceDescriptorList.add(coapResourceDescriptor);
                }

                this.philipsHueLightCoapManager = new PhilipsHueLightCoapManager(bridgeIp, username, philipsHueLightCoapResourceDescriptorList);
                this.philipsHueLightCoapManager.activate();

            }
            else
                throw new WldtConfigurationException("Wrong Configuration parameters !");

        }catch (Exception e){
            e.printStackTrace();
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

}
